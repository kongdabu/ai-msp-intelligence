import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query'
import axios from 'axios'
import { Article, PageResponse } from '../types'

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

// 기사 저장/해제 및 메모 갱신
export function useToggleArticleBookmark() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, bookmarked, note }: ToggleArticleBookmarkInput) => {
      const { data } = await axios.put(`/api/articles/${id}/bookmark`, { bookmarked, note })
      return data as Article
    },
    onSuccess: (_data, variables) => {
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

