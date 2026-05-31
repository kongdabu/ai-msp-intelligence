import { InsightDetail, INSIGHT_TYPE_LABELS, INSIGHT_TYPE_COLORS, COMPETITOR_LABELS } from '../../types'
import { X, Star, CheckSquare, Square, ExternalLink, Bookmark } from 'lucide-react'
import { useState, useEffect } from 'react'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import { useToggleBookmark } from '../../hooks/useInsights'

interface Props {
  insight: InsightDetail | null
  onClose: () => void
}

export default function InsightPanel({ insight, onClose }: Props) {
  const [checked, setChecked] = useState<Set<number>>(new Set())
  const [note, setNote] = useState('')
  const { mutate: toggleBookmark, isPending } = useToggleBookmark()

  // 패널에 표시된 인사이트가 바뀌면 메모 입력값 동기화
  useEffect(() => {
    setNote(insight?.bookmarkNote ?? '')
  }, [insight?.id, insight?.bookmarkNote])

  if (!insight) return null

  const toggleCheck = (i: number) => {
    const next = new Set(checked)
    next.has(i) ? next.delete(i) : next.add(i)
    setChecked(next)
  }

  // 저장 토글: 저장 시 현재 메모 함께 반영, 해제 시 메모 제거
  const handleToggleBookmark = () => {
    if (isPending) return
    toggleBookmark({
      id: insight.id,
      bookmarked: !insight.bookmarked,
      note: !insight.bookmarked ? note.trim() || undefined : undefined,
    })
  }

  // 메모만 저장 (저장 상태로 전환하며 메모 갱신)
  const handleSaveNote = () => {
    if (isPending) return
    toggleBookmark({ id: insight.id, bookmarked: true, note: note.trim() || undefined })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div
        className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between p-6 border-b border-gray-200">
          <div className="flex-1 pr-4">
            <div className="flex items-center gap-2 mb-2 flex-wrap">
              <span className={`badge ${INSIGHT_TYPE_COLORS[insight.insightType]}`}>
                {INSIGHT_TYPE_LABELS[insight.insightType]}
              </span>
              <span className="badge bg-gray-100 text-gray-700">
                {COMPETITOR_LABELS[insight.competitor]}
              </span>
              <div className="flex items-center gap-0.5 ml-1">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star
                    key={i}
                    size={14}
                    className={i < insight.impactScore ? 'fill-amber-400 text-amber-400' : 'text-gray-200'}
                  />
                ))}
              </div>
            </div>
            <h2 className="text-lg font-bold text-gray-900">{insight.title}</h2>
            {insight.generatedAt && (
              <p className="text-xs text-gray-400 mt-1">
                {format(new Date(insight.generatedAt), 'yyyy년 M월 d일 HH:mm', { locale: ko })} 생성
              </p>
            )}
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <button
              onClick={handleToggleBookmark}
              disabled={isPending}
              aria-label={insight.bookmarked ? '저장 해제' : '저장'}
              title={insight.bookmarked ? '저장 해제' : '나중에 다시 보기'}
              className={`p-1.5 rounded-md transition-colors disabled:opacity-40 ${
                insight.bookmarked ? 'text-blue-500 hover:bg-blue-50' : 'text-gray-400 hover:text-blue-500 hover:bg-gray-100'
              }`}
            >
              <Bookmark size={18} className={insight.bookmarked ? 'fill-blue-500' : ''} />
            </button>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600 p-1.5">
              <X size={20} />
            </button>
          </div>
        </div>

        {/* 본문 */}
        <div className="p-6 space-y-5">
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-2">상세 분석</h4>
            <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
              {insight.content}
            </p>
          </div>

          {/* 액션 아이템 */}
          {insight.actionItems?.length > 0 && (
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-2">액션 아이템</h4>
              <ul className="space-y-2">
                {insight.actionItems.map((item, i) => (
                  <li
                    key={i}
                    className="flex items-start gap-2 cursor-pointer"
                    onClick={() => toggleCheck(i)}
                  >
                    {checked.has(i) ? (
                      <CheckSquare size={16} className="text-blue-500 mt-0.5 shrink-0" />
                    ) : (
                      <Square size={16} className="text-gray-300 mt-0.5 shrink-0" />
                    )}
                    <span className={`text-sm ${checked.has(i) ? 'line-through text-gray-400' : 'text-gray-700'}`}>
                      {item}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* 근거 기사 */}
          {insight.sourceArticles?.length > 0 && (
            <div>
              <h4 className="text-sm font-semibold text-gray-700 mb-2">
                근거 기사 ({insight.sourceArticles.length}건)
              </h4>
              <ul className="space-y-2">
                {insight.sourceArticles.slice(0, 5).map((article) => (
                  <li key={article.id} className="flex items-start gap-2">
                    <ExternalLink size={14} className="text-blue-400 mt-0.5 shrink-0" />
                    <a
                      href={article.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-blue-600 hover:underline line-clamp-1"
                    >
                      {article.title}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* 리마인드 메모 (저장한 인사이트 나중에 다시 보기용) */}
          <div className="pt-1">
            <div className="flex items-center justify-between mb-2">
              <h4 className="text-sm font-semibold text-gray-700 flex items-center gap-1.5">
                <Bookmark size={14} className="text-blue-500" />
                리마인드 메모
              </h4>
              {insight.bookmarked && insight.bookmarkedAt && (
                <span className="text-xs text-gray-400">
                  {format(new Date(insight.bookmarkedAt), 'M월 d일 HH:mm', { locale: ko })} 저장됨
                </span>
              )}
            </div>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              maxLength={500}
              rows={2}
              placeholder="나중에 다시 볼 이유나 확인할 내용을 적어두세요. (선택)"
              className="w-full text-sm border border-gray-200 rounded-md px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-blue-200 focus:border-blue-400"
            />
            <div className="flex justify-end mt-2">
              <button
                onClick={handleSaveNote}
                disabled={isPending}
                className="btn-primary text-xs disabled:opacity-50"
              >
                {isPending ? '저장 중...' : insight.bookmarked ? '메모 저장' : '저장하고 메모 남기기'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
