import { create } from 'zustand'
import axios from 'axios'

const ADMIN_TOKEN_KEY = 'ai_msp_admin_token'

interface AuthStore {
  adminToken: string | null
  isAdmin: boolean
  setAdminToken: (token: string) => void
  clearAdminToken: () => void
}

const initialToken = localStorage.getItem(ADMIN_TOKEN_KEY)

// Axios 인터셉터 설정 (모든 요청에 X-API-Token 헤더 자동 첨부)
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem(ADMIN_TOKEN_KEY)
  if (token) {
    config.headers['X-API-Token'] = token
  }
  return config
})

export const useAuthStore = create<AuthStore>((set) => ({
  adminToken: initialToken,
  isAdmin: !!initialToken,
  setAdminToken: (token: string) => {
    const trimmed = token.trim()
    localStorage.setItem(ADMIN_TOKEN_KEY, trimmed)
    set({ adminToken: trimmed, isAdmin: true })
  },
  clearAdminToken: () => {
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    set({ adminToken: null, isAdmin: false })
  },
}))
