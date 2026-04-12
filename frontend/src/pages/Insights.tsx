import { useState } from 'react'
import { useInsights, useInsight, useGenerateInsights } from '../hooks/useInsights'
import InsightCard from '../components/insight/InsightCard'
import InsightPanel from '../components/insight/InsightPanel'
import { Insight, InsightType } from '../types'
import { Sparkles } from 'lucide-react'

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

  const { data, isLoading } = useInsights({ type: activeTab || undefined, page, size: 12 })
  const { data: detail } = useInsight(selectedId)
  const { mutate: generate, isPending } = useGenerateInsights()

  return (
    <div className="p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex gap-1 bg-gray-100 rounded-lg p-1">
          {TABS.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => { setActiveTab(value); setPage(0) }}
              className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors ${
                activeTab === value
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
        <button
          onClick={() => generate()}
          disabled={isPending}
          className="flex items-center gap-2 btn-primary"
        >
          <Sparkles size={16} />
          {isPending ? '생성 중...' : '인사이트 생성'}
        </button>
      </div>

      {/* 결과 수 */}
      {data && (
        <div className="text-sm text-gray-500">
          총 <span className="font-semibold text-gray-900">{data.totalElements}</span>건
        </div>
      )}

      {/* 로딩 */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {data?.content.map((insight) => (
            <InsightCard
              key={insight.id}
              insight={insight}
              onClick={(ins: Insight) => setSelectedId(ins.id)}
            />
          ))}
        </div>
      )}

      {/* 상세 모달 */}
      {selectedId && (
        <InsightPanel insight={detail ?? null} onClose={() => setSelectedId(null)} />
      )}
    </div>
  )
}
