import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { PageResponse, StrategyReport } from '../types'

export function useStrategyReports(page = 0, size = 10) {
  return useQuery<PageResponse<StrategyReport>>({
    queryKey: ['strategy-reports', page, size],
    queryFn: async () => {
      const { data } = await axios.get('/api/strategy-reports', {
        params: { page, size },
      })
      return data
    },
    refetchInterval: 1000 * 60 * 5,
  })
}

export function useGenerateStrategyReport() {
  const queryClient = useQueryClient()
  return useMutation<StrategyReport, Error, void>({
    mutationFn: async () => {
      const { data } = await axios.post('/api/strategy-reports/generate')
      return data
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['strategy-reports'] })
    },
  })
}
