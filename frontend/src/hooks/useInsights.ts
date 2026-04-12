import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { Insight, InsightDetail, PageResponse } from '../types'

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

export function useGenerateInsights() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/insights/generate')
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['insights'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
