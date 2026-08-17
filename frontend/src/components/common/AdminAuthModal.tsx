import { useState } from 'react'
import { Lock, Unlock, X, KeyRound, AlertCircle, CheckCircle2 } from 'lucide-react'
import axios from 'axios'
import { useAuthStore } from '../../store/authStore'
import { useToastStore } from '../../store/toastStore'

interface AdminAuthModalProps {
  isOpen: boolean
  onClose: () => void
}

export default function AdminAuthModal({ isOpen, onClose }: AdminAuthModalProps) {
  const [tokenInput, setTokenInput] = useState('')
  const [isVerifying, setIsVerifying] = useState(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  const { isAdmin, setAdminToken, clearAdminToken } = useAuthStore()
  const { showToast } = useToastStore()

  if (!isOpen) return null

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!tokenInput.trim()) {
      setErrorMsg('관리자 토큰을 입력해 주세요.')
      return
    }

    setIsVerifying(true)
    setErrorMsg(null)

    try {
      // 입력한 토큰으로 유효성 검증 API 호출
      await axios.post(
        '/api/admin/verify-token',
        {},
        {
          headers: {
            'X-API-Token': tokenInput.trim(),
          },
        }
      )

      setAdminToken(tokenInput.trim())
      showToast('관리자 인증이 완료되었습니다. 모든 실행 권한이 활성화됩니다.', 'success')
      setTokenInput('')
      onClose()
    } catch (err: any) {
      if (err.response?.status === 401) {
        setErrorMsg('토큰이 일치하지 않습니다. 올바른 관리자 토큰을 입력하세요.')
      } else {
        setErrorMsg(err.response?.data?.message || '인증 중 오류가 발생했습니다. 다시 시도해 주세요.')
      }
    } finally {
      setIsVerifying(false)
    }
  }

  const handleLogout = () => {
    clearAdminToken()
    showToast('관리자 모드가 해제되었습니다. (조회 전용 모드)', 'info')
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-fade-in">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        {/* 모달 헤더 */}
        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
          <div className="flex items-center gap-2.5">
            <div className={`rounded-xl p-2 ${isAdmin ? 'bg-emerald-50 text-emerald-600' : 'bg-indigo-50 text-indigo-600'}`}>
              {isAdmin ? <Unlock size={20} /> : <Lock size={20} />}
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-base">
                {isAdmin ? '관리자 모드 활성 중' : '관리자 모드 잠금 해제'}
              </h3>
              <p className="text-xs text-slate-500">
                {isAdmin ? '수동 생성 및 시스템 설정 변경 권한 보유' : '수동 생성 및 수집 실행을 위한 토큰 입력'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* 모달 본문 */}
        {isAdmin ? (
          <div className="py-5 space-y-4">
            <div className="rounded-xl border border-emerald-200 bg-emerald-50/70 p-4 text-xs text-emerald-900 flex items-start gap-2.5">
              <CheckCircle2 size={16} className="text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-emerald-950">현재 관리자 권한으로 로그인되어 있습니다.</p>
                <p className="mt-1 text-emerald-800 leading-relaxed">
                  데일리 브리핑 생성, 기사 수집, AI 분석 및 Watch List 설정을 자유롭게 실행할 수 있습니다.
                </p>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
              >
                닫기
              </button>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-xl bg-rose-600 px-4 py-2 text-xs font-semibold text-white hover:bg-rose-700 transition-colors"
              >
                관리자 모드 해제 (로그아웃)
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleVerify} className="py-5 space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <KeyRound size={14} className="text-slate-500" />
                관리자 토큰 (API_SECRET_TOKEN)
              </label>
              <input
                type="password"
                value={tokenInput}
                onChange={(e) => setTokenInput(e.target.value)}
                placeholder="환경변수에 설정된 비밀 토큰을 입력하세요"
                className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                autoFocus
              />
              <p className="text-[11px] text-slate-400 leading-relaxed">
                * 인증 후 브라우저에 안전하게 보관되어 수동 실행 시 자동으로 인증 헤더가 전달됩니다.
              </p>
            </div>

            {errorMsg && (
              <div className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs text-rose-800 flex items-center gap-2">
                <AlertCircle size={15} className="shrink-0 text-rose-600" />
                <span>{errorMsg}</span>
              </div>
            )}

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={isVerifying}
                className="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors"
              >
                {isVerifying ? '인증 확인 중...' : '관리자 모드 해제'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
