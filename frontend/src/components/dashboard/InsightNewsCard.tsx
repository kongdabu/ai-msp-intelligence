import { Bookmark, ChevronRight, CircleAlert, Sparkles, Star } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { ko } from 'date-fns/locale'
import {
  COMPETITOR_COLORS,
  COMPETITOR_LABELS,
  Insight,
  INSIGHT_TYPE_COLORS,
  INSIGHT_TYPE_LABELS,
} from '../../types'
import { useToggleBookmark } from '../../hooks/useInsights'

interface InsightNewsCardProps {
  insight: Insight
  featured?: boolean
  onSelect: (id: number) => void
}

function ImpactStars({ score }: { score: number }) {
  return (
    <div className="flex items-center gap-0.5" aria-label={`영향도 ${score}점`}>
      {Array.from({ length: 5 }).map((_, index) => (
        <Star
          key={index}
          size={12}
          className={index < score ? 'fill-amber-400 text-amber-400' : 'text-white/25'}
        />
      ))}
    </div>
  )
}

export default function InsightNewsCard({ insight, featured = false, onSelect }: InsightNewsCardProps) {
  const { mutate: toggleBookmark, isPending } = useToggleBookmark()
  const timeAgo = insight.generatedAt
    ? formatDistanceToNow(new Date(insight.generatedAt), { addSuffix: true, locale: ko })
    : '방금 생성됨'

  const handleBookmark = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation()
    if (isPending) return
    toggleBookmark({
      id: insight.id,
      bookmarked: !insight.bookmarked,
      note: insight.bookmarkNote ?? undefined,
    })
  }

  if (featured) {
    return (
      <article className="relative overflow-hidden rounded-3xl bg-slate-950 p-5 text-white shadow-xl shadow-slate-900/15 sm:p-7">
        <div className="absolute -right-16 -top-20 h-56 w-56 rounded-full bg-cyan-400/20 blur-3xl" aria-hidden="true" />
        <div className="absolute -bottom-24 left-1/3 h-52 w-52 rounded-full bg-indigo-500/30 blur-3xl" aria-hidden="true" />
        <div className="relative flex h-full min-h-[270px] flex-col">
          <div className="flex items-start justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <span className="inline-flex items-center gap-1 rounded-full bg-white/10 px-2.5 py-1 text-xs font-semibold text-cyan-100 ring-1 ring-inset ring-white/15">
                <Sparkles size={13} /> 오늘의 핵심 브리핑
              </span>
              <span className="rounded-full bg-white/10 px-2.5 py-1 text-xs font-medium text-slate-200">
                {INSIGHT_TYPE_LABELS[insight.insightType]}
              </span>
            </div>
            <button
              type="button"
              onClick={handleBookmark}
              disabled={isPending}
              aria-label={insight.bookmarked ? '저장 해제' : '인사이트 저장'}
              className="rounded-full bg-white/10 p-2 text-white transition hover:bg-white/20 disabled:opacity-40"
            >
              <Bookmark size={17} className={insight.bookmarked ? 'fill-cyan-300 text-cyan-300' : ''} />
            </button>
          </div>

          <button
            type="button"
            onClick={() => onSelect(insight.id)}
            className="mt-7 text-left outline-none focus-visible:ring-2 focus-visible:ring-cyan-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950"
            aria-label={`${insight.title} 상세 보기`}
          >
            <p className="mb-3 text-xs font-medium text-cyan-200">
              {COMPETITOR_LABELS[insight.competitor]} · {timeAgo}
            </p>
            <h2 className="max-w-3xl text-xl font-bold leading-snug tracking-tight sm:text-2xl">{insight.title}</h2>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-300 line-clamp-2">{insight.content}</p>
          </button>

          <div className="mt-auto flex flex-wrap items-center justify-between gap-3 pt-6">
            <div className="flex items-center gap-2 text-xs text-slate-300">
              <ImpactStars score={insight.impactScore} />
              <span>영향도 {insight.impactScore}/5 · 근거 {insight.sourceArticleCount}건</span>
            </div>
            <button
              type="button"
              onClick={() => onSelect(insight.id)}
              className="inline-flex items-center gap-1 text-sm font-semibold text-cyan-200 transition hover:text-white"
            >
              자세히 보기 <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </article>
    )
  }

  return (
    <article className="group relative flex min-h-[238px] flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-slate-300 hover:shadow-lg hover:shadow-slate-200/70">
      <div className="absolute inset-x-0 top-0 h-1" style={{ backgroundColor: COMPETITOR_COLORS[insight.competitor] }} />
      <div className="flex items-start justify-between gap-2">
        <div className="flex flex-wrap items-center gap-1.5 pr-2">
          <span className={`badge ${INSIGHT_TYPE_COLORS[insight.insightType]}`}>
            {INSIGHT_TYPE_LABELS[insight.insightType]}
          </span>
          <span className="text-xs font-medium text-slate-500">{COMPETITOR_LABELS[insight.competitor]}</span>
        </div>
        <button
          type="button"
          onClick={handleBookmark}
          disabled={isPending}
          aria-label={insight.bookmarked ? '저장 해제' : '인사이트 저장'}
          className="rounded-full p-1 text-slate-300 transition hover:bg-slate-100 hover:text-slate-700 disabled:opacity-40"
        >
          <Bookmark size={17} className={insight.bookmarked ? 'fill-blue-500 text-blue-500' : ''} />
        </button>
      </div>

      <button
        type="button"
        onClick={() => onSelect(insight.id)}
        className="mt-4 text-left outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2"
        aria-label={`${insight.title} 상세 보기`}
      >
        <h3 className="text-base font-bold leading-6 text-slate-900 line-clamp-2">{insight.title}</h3>
        <p className="mt-2 text-sm leading-5 text-slate-600 line-clamp-3">{insight.content}</p>
      </button>

      <div className="mt-auto pt-4">
        {insight.actionItems?.[0] && (
          <div className="flex items-start gap-2 rounded-xl bg-slate-50 px-3 py-2 text-xs leading-5 text-slate-600">
            <CircleAlert size={14} className="mt-0.5 shrink-0 text-slate-400" />
            <span className="line-clamp-1">{insight.actionItems[0]}</span>
          </div>
        )}
        <div className="mt-3 flex items-center justify-between text-xs text-slate-400">
          <span>{timeAgo}</span>
          <span>근거 {insight.sourceArticleCount}건</span>
        </div>
      </div>
    </article>
  )
}
