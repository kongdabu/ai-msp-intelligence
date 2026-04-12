import { Insight, COMPETITOR_LABELS, INSIGHT_TYPE_LABELS, INSIGHT_TYPE_COLORS, COMPETITOR_COLORS } from '../../types'
import { Star } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { ko } from 'date-fns/locale'

interface Props {
  insight: Insight
  onClick: (insight: Insight) => void
}

export default function InsightCard({ insight, onClick }: Props) {
  const timeAgo = insight.generatedAt
    ? formatDistanceToNow(new Date(insight.generatedAt), { addSuffix: true, locale: ko })
    : ''

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

      <div className="text-xs text-gray-400 mt-1">
        근거 기사 {insight.sourceArticleCount}건
      </div>
    </div>
  )
}
