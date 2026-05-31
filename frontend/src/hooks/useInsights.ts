import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query'
import axios from 'axios'
import { Insight, InsightDetail, PageResponse } from '../types'

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

// 인사이트 저장/해제 및 메모 갱신
export function useToggleBookmark() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, bookmarked, note }: ToggleBookmarkInput) => {
      const { data } = await axios.put(`/api/insights/${id}/bookmark`, { bookmarked, note })
      return data as Insight
    },
    onSuccess: (_data, variables) => {
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
