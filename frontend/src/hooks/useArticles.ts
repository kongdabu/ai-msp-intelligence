import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
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
          dateFrom: params.dateFrom || undefined,
          dateTo: params.dateTo || undefined,
        },
      })
      return data
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

export function useTriggerCrawl() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/articles/crawl')
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['articles'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
