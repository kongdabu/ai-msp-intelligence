import { useEffect, useState } from 'react'
import { useSystemConfig, useUpdateSystemConfig } from '../hooks/useSystemConfig'
import { SystemConfig } from '../types'
import { Save, CheckCircle, AlertCircle } from 'lucide-react'

export default function Settings() {
  const { data: config, isLoading } = useSystemConfig()
  const updateConfig = useUpdateSystemConfig()

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
    <div className="p-4 sm:p-6 max-w-2xl">
      <h1 className="text-xl font-bold text-gray-900 mb-6">시스템 설정</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
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
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                  className="w-28 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
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
            disabled={updateConfig.isPending}
            className="flex items-center gap-2 btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Save size={16} />
            {updateConfig.isPending ? '저장 중...' : '저장'}
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
    </div>
  )
}
