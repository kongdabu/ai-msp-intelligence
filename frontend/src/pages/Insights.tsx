import { useState } from 'react'
import { useInsights, useInsight, useGenerateInsights } from '../hooks/useInsights'
import InsightCard from '../components/insight/InsightCard'
import InsightPanel from '../components/insight/InsightPanel'
import { Insight, InsightType } from '../types'
import { Sparkles } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import AdminAuthModal from '../components/common/AdminAuthModal'

const TABS: { value: InsightType | ''; label: string }[] = [
  { value: '', label: '전체' },
  { value: 'OPPORTUNITY', label: '기회' },
  { value: 'THREAT', label: '위협' },
  { value: 'TREND', label: '트렌드' },
  { value: 'STRATEGY', label: '전략' },
]

export default function Insights() {
  const [activeTab, setActiveTab] = useState<InsightType | ''>('')
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)
  const { isAdmin } = useAuthStore()

  const { data, isLoading } = useInsights({ type: activeTab || undefined, page, size: 20 })
  const { data: detail } = useInsight(selectedId)
  const { mutate: generate, isPending } = useGenerateInsights()

  const handleGenerateClick = () => {
    if (!isAdmin) {
      setIsAuthModalOpen(true)
      return
    }
    generate()
  }

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between gap-3">
        <div className="overflow-x-auto">
          <div className="flex gap-1 bg-gray-100 rounded-lg p-1 w-max">
            {TABS.map(({ value, label }) => (
              <button
                key={value}
                onClick={() => { setActiveTab(value); setPage(0) }}
                className={`px-3 sm:px-4 py-1.5 rounded-md text-xs sm:text-sm font-medium whitespace-nowrap transition-colors cursor-pointer ${
                  activeTab === value
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
        <button
          onClick={handleGenerateClick}
          disabled={isPending}
          className={`flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-xs sm:text-sm font-medium transition-colors shrink-0 cursor-pointer ${
            isAdmin
              ? 'bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50'
              : 'bg-gray-100 border border-gray-300 text-gray-700 hover:bg-gray-200'
          }`}
          title={isAdmin ? '인사이트 AI 생성' : '관리자 인증 필요'}
        >
          <Sparkles size={14} />
          {isAdmin ? (
            <>
              <span className="hidden sm:inline">{isPending ? '생성 중...' : '인사이트 생성'}</span>
              <span className="sm:hidden">{isPending ? '...' : '생성'}</span>
            </>
          ) : (
            <span>🔒 인사이트 생성 (관리자)</span>
          )}
        </button>
      </div>

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />

      {data && (
        <div className="text-sm text-gray-500">
          총 <span className="font-semibold text-gray-900">{data.totalElements}</span>건
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card animate-pulse h-48" />
          ))}
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-lg font-medium">인사이트가 없습니다</p>
          <p className="text-sm mt-1">위 버튼으로 인사이트를 생성해보세요.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {data?.content.map((insight) => (
            <InsightCard
              key={insight.id}
              insight={insight}
              onClick={(ins: Insight) => setSelectedId(ins.id)}
            />
          ))}
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-2">
          <button
            onClick={() => setPage(p => p - 1)}
            disabled={page === 0}
            className="px-3 py-1.5 text-sm rounded-md border border-gray-200 disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            이전
          </button>
          <span className="text-sm text-gray-600">
            {page + 1} / {data.totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={page >= data.totalPages - 1}
            className="px-3 py-1.5 text-sm rounded-md border border-gray-200 disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            다음
          </button>
        </div>
      )}

      {selectedId && (
        <InsightPanel insight={detail ?? null} onClose={() => setSelectedId(null)} />
      )}
    </div>
  )
}
