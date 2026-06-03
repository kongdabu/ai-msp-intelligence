import { useToastStore } from '../../store/toastStore'
import { CheckCircle2, XCircle, Info, X } from 'lucide-react'

const ICONS = {
  success: CheckCircle2,
  error: XCircle,
  info: Info,
}

const STYLES = {
  success: 'border-green-200 bg-green-50 text-green-800',
  error: 'border-red-200 bg-red-50 text-red-800',
  info: 'border-blue-200 bg-blue-50 text-blue-800',
}

const ICON_COLORS = {
  success: 'text-green-500',
  error: 'text-red-500',
  info: 'text-blue-500',
}

// 화면 우하단 토스트 알림 — 액션 결과를 즉시 표시
export default function Toaster() {
  const { toasts, removeToast } = useToastStore()

  if (toasts.length === 0) return null

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-[calc(100vw-2rem)]">
      {toasts.map((toast) => {
        const Icon = ICONS[toast.type]
        return (
          <div
            key={toast.id}
            role="status"
            className={`flex items-center gap-2.5 rounded-lg border px-4 py-3 text-sm font-medium shadow-lg animate-toast-in ${STYLES[toast.type]}`}
          >
            <Icon size={18} className={`shrink-0 ${ICON_COLORS[toast.type]}`} />
            <span className="flex-1">{toast.message}</span>
            <button
              onClick={() => removeToast(toast.id)}
              className="text-gray-400 hover:text-gray-600 shrink-0"
              aria-label="닫기"
            >
              <X size={14} />
            </button>
          </div>
        )
      })}
    </div>
  )
}
