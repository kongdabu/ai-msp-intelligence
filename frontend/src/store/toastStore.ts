import { create } from 'zustand'

export type ToastType = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  message: string
  type: ToastType
}

interface ToastStore {
  toasts: Toast[]
  showToast: (message: string, type?: ToastType) => void
  removeToast: (id: number) => void
}

// 전역 토스트 메시지 스토어 — 북마크 등 액션 결과 알림용
export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  showToast: (message, type = 'success') => {
    const id = Date.now() + Math.random()
    set((state) => ({ toasts: [...state.toasts, { id, message, type }] }))
    // 2.5초 후 자동 제거
    setTimeout(() => {
      set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }))
    }, 2500)
  },
  removeToast: (id) =>
    set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
}))
