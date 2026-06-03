import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query'
import axios from 'axios'
import { Article, PageResponse } from '../types'
import { useToastStore } from '../store/toastStore'

interface ArticleParams {
  competitor?: string
  category?: string
  sourceType?: string
  keyword?: string
  dateFrom?: string
  dateTo?: string
  page?: number
  size?: number
}

export function useArticles(params: ArticleParams = {}) {
  return useQuery<PageResponse<Article>>({
    queryKey: ['articles', params],
    queryFn: async () => {
      const { data } = await axios.get('/api/articles', {
        params: {
          ...params,
          competitor: params.competitor || undefined,
          category: params.category || undefined,
          sourceType: params.sourceType || undefined,
          keyword: params.keyword || undefined,
          dateFrom: params.dateFrom ? `${params.dateFrom}T00:00:00` : undefined,
          dateTo: params.dateTo ? `${params.dateTo}T23:59:59` : undefined,
        },
      })
      return data
    },
  })
}

interface ArticleListParams {
  competitor?: string
  category?: string
  dateFrom?: string
  dateTo?: string
  limit?: number
}

export function useArticlesList(params: ArticleListParams = {}) {
  return useQuery<Article[]>({
    queryKey: ['articles-list', params],
    queryFn: async () => {
      const { data } = await axios.get('/api/articles/list', {
        params: {
          competitor: params.competitor || undefined,
          category: params.category || undefined,
          dateFrom: params.dateFrom || undefined,
          dateTo: params.dateTo || undefined,
          limit: params.limit,
        },
      })
      return data
    },
    staleTime: 5 * 60 * 1000,      // 5분간 캐시 유지 — 탭 전환 시 불필요한 재요청 방지
    placeholderData: keepPreviousData, // 탭 전환 시 이전 데이터 표시 (스켈레톤 깜빡임 방지)
  })
}

// 저장(북마크)한 기사 목록
export function useBookmarkedArticles(params: { page?: number; size?: number } = {}) {
  return useQuery<PageResponse<Article>>({
    queryKey: ['articles', 'bookmarked', params],
    queryFn: async () => {
      const { data } = await axios.get('/api/articles/bookmarked', { params })
      return data
    },
    staleTime: 5 * 60 * 1000,
    placeholderData: keepPreviousData,
  })
}

interface ToggleArticleBookmarkInput {
  id: number
  bookmarked: boolean
  note?: string
}

// 캐시에 있는 모든 기사 목록/상세에서 해당 기사의 북마크 상태를 즉시 갱신
function patchArticleInCaches(
  queryClient: ReturnType<typeof useQueryClient>,
  id: number,
  patch: Partial<Article>,
) {
  // 페이지네이션 목록 (['articles', ...], ['articles', 'bookmarked', ...])
  queryClient.setQueriesData<PageResponse<Article>>({ queryKey: ['articles'] }, (old) => {
    if (!old?.content) return old
    return { ...old, content: old.content.map((a) => (a.id === id ? { ...a, ...patch } : a)) }
  })
  // 비페이지네이션 목록 (['articles-list', ...])
  queryClient.setQueriesData<Article[]>({ queryKey: ['articles-list'] }, (old) => {
    if (!Array.isArray(old)) return old
    return old.map((a) => (a.id === id ? { ...a, ...patch } : a))
  })
  // 단건 상세 (['article', id])
  queryClient.setQueryData<Article>(['article', id], (old) => (old ? { ...old, ...patch } : old))
}

// 기사 저장/해제 및 메모 갱신 — 낙관적 업데이트로 즉시 반영 + 결과 토스트
export function useToggleArticleBookmark() {
  const queryClient = useQueryClient()
  const showToast = useToastStore((s) => s.showToast)

  return useMutation({
    mutationFn: async ({ id, bookmarked, note }: ToggleArticleBookmarkInput) => {
      const { data } = await axios.put(`/api/articles/${id}/bookmark`, { bookmarked, note })
      return data as Article
    },
    // 서버 응답 전에 UI를 먼저 갱신 (즉시 반영)
    onMutate: async ({ id, bookmarked, note }) => {
      await queryClient.cancelQueries({ queryKey: ['articles'] })
      await queryClient.cancelQueries({ queryKey: ['articles-list'] })
      await queryClient.cancelQueries({ queryKey: ['article', id] })

      const previous = {
        articles: queryClient.getQueriesData<PageResponse<Article>>({ queryKey: ['articles'] }),
        list: queryClient.getQueriesData<Article[]>({ queryKey: ['articles-list'] }),
        detail: queryClient.getQueryData<Article>(['article', id]),
      }

      patchArticleInCaches(queryClient, id, {
        bookmarked,
        bookmarkedAt: bookmarked ? new Date().toISOString() : null,
        bookmarkNote: bookmarked ? note ?? null : null,
      })

      return previous
    },
    onError: (_err, variables, context) => {
      // 실패 시 이전 상태로 롤백
      context?.articles.forEach(([key, data]) => queryClient.setQueryData(key, data))
      context?.list.forEach(([key, data]) => queryClient.setQueryData(key, data))
      if (context?.detail) queryClient.setQueryData(['article', variables.id], context.detail)
      showToast('저장에 실패했습니다. 다시 시도해주세요.', 'error')
    },
    onSuccess: (_data, variables) => {
      showToast(variables.bookmarked ? '저장되었습니다.' : '저장을 해제했습니다.', 'success')
    },
    // 성공/실패와 무관하게 서버 기준으로 최종 동기화
    onSettled: (_data, _err, variables) => {
      queryClient.invalidateQueries({ queryKey: ['articles'] })
      queryClient.invalidateQueries({ queryKey: ['articles-list'] })
      queryClient.invalidateQueries({ queryKey: ['article', variables.id] })
    },
  })
}

export function useArticle(id: number | null) {
  return useQuery<Article>({
    queryKey: ['article', id],
    queryFn: async () => {
      const { data } = await axios.get(`/api/articles/${id}`)
      return data
    },
    enabled: id !== null,
  })
}

interface TriggerCrawlOptions {
  onSuccess?: (data: { crawledCount: number; triggeredAt: string }) => void
}

export function useTriggerCrawl(options?: TriggerCrawlOptions) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/articles/crawl')
      return data as { crawledCount: number; triggeredAt: string }
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['articles'] })
      queryClient.invalidateQueries({ queryKey: ['articles-list'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['sources'] })
      options?.onSuccess?.(data)
    },
  })
}

