import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { RadarOverview, RadarSignal } from '../types'

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

export function useRadarSignals() {
  return useQuery<RadarSignal[]>({
    queryKey: ['radar', 'signals'],
    queryFn: async () => {
      const { data } = await axios.get('/api/radar/signals')
      return data
    },
  })
}
