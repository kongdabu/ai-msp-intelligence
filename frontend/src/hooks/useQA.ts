import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { QaSession, QaMessage } from '../types'

export function useSessions() {
  return useQuery<QaSession[]>({
    queryKey: ['qa-sessions'],
    queryFn: async () => {
      const { data } = await axios.get('/api/qa/sessions')
      return data
    },
    staleTime: 30 * 1000,
  })
}

export function useMessages(sessionId: number | null) {
  return useQuery<QaMessage[]>({
    queryKey: ['qa-messages', sessionId],
    queryFn: async () => {
      const { data } = await axios.get(`/api/qa/sessions/${sessionId}/messages`)
      return data
    },
    enabled: sessionId !== null,
  })
}

export function useCreateSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { data } = await axios.post<QaSession>('/api/qa/sessions')
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qa-sessions'] })
    },
  })
}

export function useAskQuestion(sessionId: number | null) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (question: string) => {
      const { data } = await axios.post<QaMessage>(
        `/api/qa/sessions/${sessionId}/messages`,
        { question }
      )
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qa-messages', sessionId] })
      queryClient.invalidateQueries({ queryKey: ['qa-sessions'] })
    },
  })
}
