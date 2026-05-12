import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { BattleCard, BattleCardDetail } from '../types'

const TEN_MINUTES = 10 * 60 * 1000

export function useBattleCards() {
  return useQuery<BattleCard[]>({
    queryKey: ['battlecards'],
    queryFn: async () => {
      const { data } = await axios.get('/api/battlecards')
      return data
    },
    staleTime: TEN_MINUTES,
  })
}

export function useBattleCardsByCompetitor(competitor: string) {
  return useQuery<BattleCard[]>({
    queryKey: ['battlecards', competitor],
    queryFn: async () => {
      const { data } = await axios.get(`/api/battlecards/${competitor}`)
      return data
    },
    staleTime: TEN_MINUTES,
  })
}

export function useBattleCardDetail(id: number | null) {
  return useQuery<BattleCardDetail>({
    queryKey: ['battlecard-detail', id],
    queryFn: async () => {
      const { data } = await axios.get(`/api/battlecards/detail/${id}`)
      return data
    },
    enabled: id !== null,
    staleTime: TEN_MINUTES,
  })
}

interface GenerateOptions {
  onSuccess?: (cards: BattleCard[]) => void
}

export function useGenerateBattleCards(options?: GenerateOptions) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post('/api/battlecards/generate')
      return data as BattleCard[]
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['battlecards'] })
      queryClient.invalidateQueries({ queryKey: ['battlecard-detail'] })
      options?.onSuccess?.(data)
    },
  })
}
