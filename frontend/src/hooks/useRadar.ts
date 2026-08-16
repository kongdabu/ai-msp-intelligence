import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { PageResponse, RadarCollectionStatus, RadarLensCode, RadarOverview, RadarPlayer, RadarPlayerUpdate, RadarSignal } from '../types'

export function useRadarOverview() {
  return useQuery<RadarOverview>({
    queryKey: ['radar', 'overview'],
    queryFn: async () => {
      const { data } = await axios.get('/api/radar/overview')
      return data
    },
    refetchInterval: 1000 * 60 * 5,
  })
}

export interface RadarSignalFilters {
  lens: RadarLensCode | null
  minimumImpactScore: number | null
  page: number
  size?: number
}

export function useRadarSignals(filters: RadarSignalFilters) {
  return useQuery<PageResponse<RadarSignal>>({
    queryKey: ['radar', 'signals', filters],
    queryFn: async () => {
      const { data } = await axios.get('/api/radar/signals', {
        params: {
          lens: filters.lens ?? undefined,
          minimumImpactScore: filters.minimumImpactScore ?? undefined,
          page: filters.page,
          size: filters.size ?? 20,
        },
      })
      return data
    },
  })
}

export function useRadarPlayers() {
  return useQuery<RadarPlayer[]>({
    queryKey: ['radar', 'players'],
    queryFn: async () => {
      const { data } = await axios.get('/api/radar/players')
      return data
    },
  })
}

export function useUpdateRadarPlayer() {
  const queryClient = useQueryClient()
  return useMutation<RadarPlayer, Error, { id: number; input: RadarPlayerUpdate }>({
    mutationFn: async ({ id, input }) => {
      const { data } = await axios.put(`/api/radar/players/${id}`, input)
      return data
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['radar', 'players'] })
      void queryClient.invalidateQueries({ queryKey: ['radar', 'overview'] })
    },
  })
}

export function useRadarCollectionStatus() {
  return useQuery<RadarCollectionStatus>({
    queryKey: ['radar', 'collection-status'],
    queryFn: async () => {
      const { data } = await axios.get('/api/radar/collect/status')
      return data
    },
    refetchInterval: (query) => ['RUNNING', 'CANCELLING'].includes(query.state.data?.status ?? '') ? 3000 : false,
  })
}

export function useStartRadarCollection() {
  const queryClient = useQueryClient()
  return useMutation<RadarCollectionStatus>({
    mutationFn: async () => {
      const { data } = await axios.post('/api/radar/collect')
      return data
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['radar', 'collection-status'] })
      void queryClient.invalidateQueries({ queryKey: ['radar', 'overview'] })
    },
  })
}

export function useCancelRadarCollection() {
  const queryClient = useQueryClient()
  return useMutation<RadarCollectionStatus, Error>({
    mutationFn: async () => {
      const { data } = await axios.delete('/api/radar/collect')
      return data
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['radar', 'collection-status'] })
      void queryClient.invalidateQueries({ queryKey: ['radar', 'overview'] })
    },
  })
}
