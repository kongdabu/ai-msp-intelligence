import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { SystemConfig } from '../types'

export function useSystemConfig() {
  return useQuery<SystemConfig>({
    queryKey: ['system-config'],
    queryFn: async () => {
      const { data } = await axios.get('/api/admin/config')
      return data
    },
  })
}

export function useUpdateSystemConfig() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (config: SystemConfig) => {
      const { data } = await axios.put('/api/admin/config', config)
      return data as SystemConfig
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['system-config'] })
    },
  })
}
