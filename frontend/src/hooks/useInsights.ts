import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query'
import axios from 'axios'
import { Insight, InsightDetail, PageResponse } from '../types'
import { useToastStore } from '../store/toastStore'

const FIVE_MINUTES = 5 * 60 * 1000

interface InsightParams {
  type?: string
  competitor?: string
  page?: number
  size?: number
}

export function useInsights(params: InsightParams = {}) {
  return useQuery<PageResponse<Insight>>({
    queryKey: ['insights', params],
    queryFn: async () => {
      const { data } = await axios.get('/api/insights', {
        params: {
          ...params,
          type: params.type || undefined,
          competitor: params.competitor || undefined,
        },
      })
      return data
    },
    staleTime: FIVE_MINUTES,
    placeholderData: keepPreviousData,
  })
}

export function useInsight(id: number | null) {
  return useQuery<InsightDetail>({
    queryKey: ['insight', id],
    queryFn: async () => {
      const { data } = await axios.get(`/api/insights/${id}`)
      return data
    },
    enabled: id !== null,
  })
}

// 저장(북마크)한 인사이트 목록
export function useBookmarkedInsights(params: { page?: number; size?: number } = {}) {
  return useQuery<PageResponse<Insight>>({
    queryKey: ['insights', 'bookmarked', params],
    queryFn: async () => {
      const { data } = await axios.get('/api/insights/bookmarked', { params })
      return data
    },
    staleTime: FIVE_MINUTES,
    placeholderData: keepPreviousData,
  })
}

interface ToggleBookmarkInput {
  id: number
  bookmarked: boolean
  note?: string
}

// 캐시에 있는 모든 인사이트 목록/상세에서 해당 인사이트의 북마크 상태를 즉시 갱신
function patchInsightInCaches(
  queryClient: ReturnType<typeof useQueryClient>,
  id: number,
  patch: Partial<Insight>,
) {
  // 페이지네이션 목록 (['insights', ...], ['insights', 'bookmarked', ...])
  queryClient.setQueriesData<PageResponse<Insight>>({ queryKey: ['insights'] }, (old) => {
    if (!old?.content) return old
    return { ...old, content: old.content.map((i) => (i.id === id ? { ...i, ...patch } : i)) }
  })
  // 단건 상세 (['insight', id])
  queryClient.setQueryData<InsightDetail>(['insight', id], (old) => (old ? { ...old, ...patch } : old))
}

// 인사이트 저장/해제 및 메모 갱신 — 낙관적 업데이트로 즉시 반영 + 결과 토스트
export function useToggleBookmark() {
  const queryClient = useQueryClient()
  const showToast = useToastStore((s) => s.showToast)

  return useMutation({
    mutationFn: async ({ id, bookmarked, note }: ToggleBookmarkInput) => {
      const { data } = await axios.put(`/api/insights/${id}/bookmark`, { bookmarked, note })
      return data as Insight
    },
    // 서버 응답 전에 UI를 먼저 갱신 (즉시 반영)
    onMutate: async ({ id, bookmarked, note }) => {
      await queryClient.cancelQueries({ queryKey: ['insights'] })
      await queryClient.cancelQueries({ queryKey: ['insight', id] })

      const previous = {
        insights: queryClient.getQueriesData<PageResponse<Insight>>({ queryKey: ['insights'] }),
        detail: queryClient.getQueryData<InsightDetail>(['insight', id]),
      }

      patchInsightInCaches(queryClient, id, {
        bookmarked,
        bookmarkedAt: bookmarked ? new Date().toISOString() : null,
        bookmarkNote: bookmarked ? note ?? null : null,
      })

      return previous
    },
    onError: (_err, variables, context) => {
      // 실패 시 이전 상태로 롤백
      context?.insights.forEach(([key, data]) => queryClient.setQueryData(key, data))
      if (context?.detail) queryClient.setQueryData(['insight', variables.id], context.detail)
      showToast('저장에 실패했습니다. 다시 시도해주세요.', 'error')
    },
    onSuccess: (_data, variables) => {
      showToast(variables.bookmarked ? '저장되었습니다.' : '저장을 해제했습니다.', 'success')
    },
    // 성공/실패와 무관하게 서버 기준으로 최종 동기화
    onSettled: (_data, _err, variables) => {
      queryClient.invalidateQueries({ queryKey: ['insights'] })
      queryClient.invalidateQueries({ queryKey: ['insight', variables.id] })
    },
  })
}

interface GenerateInsightsOptions {
  onSuccess?: (insights: import('../types').Insight[]) => void
}

export function useGenerateInsights(options?: GenerateInsightsOptions) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/insights/generate')
      return data as import('../types').Insight[]
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['insights'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      options?.onSuccess?.(data)
    },
  })
}
