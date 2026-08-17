import { useEffect, useState } from 'react'
import { useSystemConfig, useUpdateSystemConfig } from '../hooks/useSystemConfig'
import { useAuthStore } from '../store/authStore'
import AdminAuthModal from '../components/common/AdminAuthModal'
import { SystemConfig } from '../types'
import { Save, CheckCircle, AlertCircle, Lock } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Settings() {
  const { data: config, isLoading } = useSystemConfig()
  const updateConfig = useUpdateSystemConfig()
  const { isAdmin } = useAuthStore()
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)

  const [form, setForm] = useState<SystemConfig>({
    maxArticlesForInsight: 50,
    maxInsightsPerGeneration: 8,
    minRelevanceScoreForInsight: 65,
  })
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (config) setForm(config)
  }, [config])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!isAdmin) {
      setIsAuthModalOpen(true)
      return
    }
    updateConfig.mutate(form, {
      onSuccess: () => {
        setSaved(true)
        setTimeout(() => setSaved(false), 3000)
      },
    })
  }

  if (isLoading) {
    return (
      <div className="p-6 flex justify-center items-center h-40">
        <div className="text-gray-400 text-sm">설정 불러오는 중...</div>
      </div>
    )
  }

  return (
    <div className="p-4 sm:p-6 max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">시스템 설정</h1>
        {!isAdmin && (
          <button
            type="button"
            onClick={() => setIsAuthModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-indigo-200 bg-indigo-50 text-indigo-700 text-xs font-semibold hover:bg-indigo-100 transition-colors cursor-pointer"
          >
            <Lock size={13} />
            관리자 인증하고 수정하기
          </button>
        )}
      </div>

      {!isAdmin && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-900 flex items-start gap-2.5">
          <Lock size={16} className="text-amber-600 shrink-0 mt-0.5" />
          <div>
            <p className="font-bold">현재 조회 전용 모드입니다.</p>
            <p className="mt-0.5 text-amber-800">
              시스템 파라미터를 변경하려면 우측 상단 또는 위의 '관리자 인증'을 통해 잠금을 해제해 주세요.
            </p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <Link to="/settings/watch-list" className="flex items-center justify-between rounded-lg border border-blue-100 bg-blue-50 p-4 text-sm text-blue-800 hover:bg-blue-100"><span><span className="font-semibold">Radar 감시 대상 관리</span><span className="mt-1 block text-xs text-blue-700">사업자별 수집·분석 대상 여부와 우선순위를 관리합니다.</span></span><span aria-hidden="true">→</span></Link>
        {/* 인사이트 생성 설정 */}
        <div className="bg-white border border-gray-200 rounded-lg p-5">
          <h2 className="text-base font-semibold text-gray-800 mb-4">인사이트 생성 설정</h2>
          <div className="space-y-4">

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                최대 입력 기사 수
              </label>
              <div className="flex items-center gap-3">
                <input
                  type="number"
                  min={1}
                  max={200}
                  disabled={!isAdmin}
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:text-gray-500"
                  value={form.maxArticlesForInsight}
                  onChange={(e) => setForm({ ...form, maxArticlesForInsight: Number(e.target.value) })}
                />
                <span className="text-sm text-gray-500">건 (인사이트 생성 시 Gemini에 전달할 기사 최대 수)</span>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                최대 인사이트 생성 수
              </label>
              <div className="flex items-center gap-3">
                <input
                  type="number"
                  min={1}
                  max={20}
                  disabled={!isAdmin}
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:text-gray-500"
                  value={form.maxInsightsPerGeneration}
                  onChange={(e) => setForm({ ...form, maxInsightsPerGeneration: Number(e.target.value) })}
                />
                <span className="text-sm text-gray-500">건 (1회 생성 시 최대 인사이트 수)</span>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                기사 최소 관련도 점수
              </label>
              <div className="flex items-center gap-3">
                <input
                  type="number"
                  min={50}
                  max={100}
                  disabled={!isAdmin}
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:text-gray-500"
                  value={form.minRelevanceScoreForInsight}
                  onChange={(e) => setForm({ ...form, minRelevanceScoreForInsight: Number(e.target.value) })}
                />
                <span className="text-sm text-gray-500">점 이상 기사만 인사이트 근거로 연결 (50~100)</span>
              </div>
            </div>

          </div>
        </div>

        {/* 저장 버튼 */}
        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={!isAdmin || updateConfig.isPending}
            className={`flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors cursor-pointer ${
              isAdmin
                ? 'bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50'
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
            }`}
          >
            <Save size={16} />
            {updateConfig.isPending ? '저장 중...' : isAdmin ? '설정 저장' : '🔒 저장 (관리자 전용)'}
          </button>

          {saved && (
            <div className="flex items-center gap-1.5 text-sm text-green-600">
              <CheckCircle size={16} />
              저장되었습니다.
            </div>
          )}

          {updateConfig.isError && (
            <div className="flex items-center gap-1.5 text-sm text-red-600">
              <AlertCircle size={16} />
              저장에 실패했습니다.
            </div>
          )}
        </div>
      </form>

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />
    </div>
  )
}
