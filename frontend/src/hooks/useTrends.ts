import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { TrendNews, TrendNewsDetail } from '../types'
import { useToastStore } from '../store/toastStore'

const FIVE_MINUTES = 5 * 60 * 1000

export function useTrends() {
  return useQuery<TrendNews[]>({
    queryKey: ['trends'],
    queryFn: async () => {
      const { data } = await axios.get('/api/trends')
      return data
    },
    staleTime: FIVE_MINUTES,
  })
}

export function useTrend(id: number | null) {
  return useQuery<TrendNewsDetail>({
    queryKey: ['trend', id],
    queryFn: async () => {
      const { data } = await axios.get(`/api/trends/${id}`)
      return data
    },
    enabled: id !== null,
    staleTime: FIVE_MINUTES,
  })
}

export function useGenerateTrends() {
  const queryClient = useQueryClient()
  const showToast = useToastStore((state) => state.showToast)

  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/trends/generate')
      return data as TrendNews[]
    },
    onSuccess: (trends) => {
      queryClient.invalidateQueries({ queryKey: ['trends'] })
      showToast(
        trends.length > 0 ? `Trend News 초안 ${trends.length}건을 생성했습니다.` : '생성 가능한 공통 트렌드를 찾지 못했습니다.',
        trends.length > 0 ? 'success' : 'info',
      )
    },
    onError: () => {
      showToast('Trend News 생성에 실패했습니다. 잠시 후 다시 시도해주세요.', 'error')
    },
  })
}
