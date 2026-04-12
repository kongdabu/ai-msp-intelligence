import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { DashboardSummary } from '../types'

export function useDashboard() {
  return useQuery<DashboardSummary>({
    queryKey: ['dashboard'],
    queryFn: async () => {
      const { data } = await axios.get('/api/dashboard/summary')
      return data
    },
    refetchInterval: 1000 * 60 * 5, // 5분마다 자동 갱신
  })
}
