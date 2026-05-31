import { Insight, COMPETITOR_LABELS, INSIGHT_TYPE_LABELS, INSIGHT_TYPE_COLORS, COMPETITOR_COLORS } from '../../types'
import { Star, Bookmark } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { ko } from 'date-fns/locale'
import { useToggleBookmark } from '../../hooks/useInsights'

interface Props {
  insight: Insight
  onClick: (insight: Insight) => void
}

export default function InsightCard({ insight, onClick }: Props) {
  const { mutate: toggleBookmark, isPending } = useToggleBookmark()

  const timeAgo = insight.generatedAt
    ? formatDistanceToNow(new Date(insight.generatedAt), { addSuffix: true, locale: ko })
    : ''

  // 카드 클릭(상세 열기)과 분리하기 위해 이벤트 전파 차단
  const handleBookmark = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (isPending) return
    toggleBookmark({ id: insight.id, bookmarked: !insight.bookmarked, note: insight.bookmarkNote ?? undefined })
  }

  return (
    <div
      className="card cursor-pointer hover:shadow-md transition-shadow"
      onClick={() => onClick(insight)}
    >
      {/* 배지 */}
      <div className="flex items-center gap-2 mb-2 flex-wrap">
        <span className={`badge ${INSIGHT_TYPE_COLORS[insight.insightType]}`}>
          {INSIGHT_TYPE_LABELS[insight.insightType]}
        </span>
        <span
          className="badge text-white text-xs"
          style={{ backgroundColor: COMPETITOR_COLORS[insight.competitor] ?? '#6b7280' }}
        >
          {COMPETITOR_LABELS[insight.competitor] ?? insight.competitor}
        </span>
        <button
          type="button"
          onClick={handleBookmark}
          disabled={isPending}
          aria-label={insight.bookmarked ? '저장 해제' : '저장'}
          title={insight.bookmarked ? '저장 해제' : '나중에 다시 보기'}
          className="ml-auto text-gray-300 hover:text-blue-500 transition-colors disabled:opacity-40"
        >
          <Bookmark
            size={16}
            className={insight.bookmarked ? 'fill-blue-500 text-blue-500' : ''}
          />
        </button>
      </div>

      {/* 제목 */}
      <h3 className="font-semibold text-gray-900 text-sm mb-2 line-clamp-2">{insight.title}</h3>

      {/* 내용 */}
      <p className="text-xs text-gray-600 line-clamp-3 mb-3">{insight.content}</p>

      {/* 액션아이템 미리보기 */}
      {insight.actionItems?.length > 0 && (
        <div className="mb-3">
          <p className="text-xs font-medium text-gray-500 mb-1">액션 아이템</p>
          <ul className="space-y-0.5">
            {insight.actionItems.slice(0, 2).map((item, i) => (
              <li key={i} className="text-xs text-gray-600 flex items-start gap-1">
                <span className="mt-0.5 text-blue-500">•</span>
                <span className="line-clamp-1">{item}</span>
              </li>
            ))}
            {insight.actionItems.length > 2 && (
              <li className="text-xs text-gray-400">+{insight.actionItems.length - 2}개 더</li>
            )}
          </ul>
        </div>
      )}

      {/* 하단 */}
      <div className="flex items-center justify-between text-xs text-gray-400 mt-auto">
        <div className="flex items-center gap-1">
          {Array.from({ length: 5 }).map((_, i) => (
            <Star
              key={i}
              size={12}
              className={i < insight.impactScore ? 'fill-amber-400 text-amber-400' : 'text-gray-200'}
            />
          ))}
        </div>
        <span>{timeAgo}</span>
      </div>

      <div className="flex items-center justify-between mt-1">
        <span className="text-xs text-gray-400">근거 기사 {insight.sourceArticleCount}건</span>
        {insight.confidenceScore != null && (
          <div className="flex items-center gap-1">
            <div className={`w-2 h-2 rounded-full ${
              insight.confidenceScore >= 70 ? 'bg-green-400' : 'bg-yellow-400'
            }`} />
            <span className="text-xs text-gray-400">신뢰도 {insight.confidenceScore}</span>
          </div>
        )}
      </div>

      {/* 리마인드 메모 (저장 시 작성) */}
      {insight.bookmarkNote && (
        <div className="mt-2 pt-2 border-t border-gray-100">
          <p className="text-xs text-blue-600 bg-blue-50 rounded px-2 py-1 line-clamp-2">
            📌 {insight.bookmarkNote}
          </p>
        </div>
      )}
    </div>
  )
}
