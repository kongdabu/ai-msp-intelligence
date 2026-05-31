import { useState } from 'react'
import { useBookmarkedInsights, useInsight } from '../hooks/useInsights'
import InsightCard from '../components/insight/InsightCard'
import InsightPanel from '../components/insight/InsightPanel'
import { Insight } from '../types'
import { Bookmark } from 'lucide-react'

export default function Saved() {
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const { data, isLoading } = useBookmarkedInsights({ page, size: 20 })
  const { data: detail } = useInsight(selectedId)

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center gap-2">
        <Bookmark className="text-blue-500" size={20} />
        <h1 className="text-lg font-bold text-gray-900">저장한 인사이트</h1>
      </div>
      <p className="text-sm text-gray-500">
        나중에 다시 확인하려고 저장(북마크)한 인사이트 목록입니다.
        {data && (
          <>
            {' '}총 <span className="font-semibold text-gray-900">{data.totalElements}</span>건
          </>
        )}
      </p>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card animate-pulse h-48" />
          ))}
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <Bookmark size={36} className="mx-auto mb-3 text-gray-300" />
          <p className="text-lg font-medium">저장한 인사이트가 없습니다</p>
          <p className="text-sm mt-1">인사이트 카드의 북마크 아이콘을 눌러 저장해보세요.</p>
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
