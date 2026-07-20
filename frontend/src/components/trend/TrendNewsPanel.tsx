import { CheckCircle2, ExternalLink, FileText, Flame, X } from 'lucide-react'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import { TrendNewsDetail } from '../../types'

interface TrendNewsPanelProps {
  trend: TrendNewsDetail | null
  onClose: () => void
}

export default function TrendNewsPanel({ trend, onClose }: TrendNewsPanelProps) {
  if (!trend) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 p-4" onClick={onClose}>
      <section className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white shadow-2xl" onClick={(event) => event.stopPropagation()} aria-modal="true" role="dialog" aria-labelledby="trend-news-title">
        <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/95 p-5 backdrop-blur sm:p-7">
          <div className="flex items-start justify-between gap-4">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <span className="inline-flex items-center gap-1 rounded-full bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700"><Flame size={13} /> Hot Trend {trend.trendScore}</span>
                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">초안 · 검토 필요</span>
              </div>
              <h2 id="trend-news-title" className="mt-3 text-xl font-bold leading-8 text-slate-950 sm:text-2xl">{trend.title}</h2>
              <p className="mt-2 text-sm leading-6 text-slate-600">{trend.summary}</p>
            </div>
            <button type="button" onClick={onClose} className="shrink-0 rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700" aria-label="Trend News 닫기"><X size={20} /></button>
          </div>
        </header>

        <div className="space-y-7 p-5 sm:p-7">
          <div className="flex flex-wrap gap-2">
            {trend.keywords.map((keyword) => <span key={keyword} className="rounded-md bg-slate-100 px-2 py-1 text-xs text-slate-600">#{keyword}</span>)}
          </div>

          <article className="whitespace-pre-line text-sm leading-7 text-slate-700">{trend.content}</article>

          {trend.actionItems.length > 0 && (
            <section>
              <h3 className="flex items-center gap-1.5 text-sm font-bold text-slate-900"><CheckCircle2 size={16} className="text-emerald-600" /> 검토할 액션</h3>
              <ul className="mt-3 space-y-2">
                {trend.actionItems.map((item) => <li key={item} className="rounded-xl bg-emerald-50 px-3 py-2.5 text-sm leading-6 text-emerald-900">{item}</li>)}
              </ul>
            </section>
          )}

          <section className="border-t border-slate-200 pt-6">
            <h3 className="flex items-center gap-1.5 text-sm font-bold text-slate-900"><FileText size={16} className="text-blue-600" /> 근거 기사 {trend.sourceArticles.length}건</h3>
            <ul className="mt-3 space-y-2">
              {trend.sourceArticles.map((article) => (
                <li key={article.id}>
                  <a href={article.url} target="_blank" rel="noopener noreferrer" className="group flex items-start gap-2 rounded-xl border border-slate-200 p-3 transition hover:border-blue-200 hover:bg-blue-50">
                    <ExternalLink size={15} className="mt-0.5 shrink-0 text-blue-500" />
                    <span>
                      <span className="flex flex-wrap items-center gap-1.5 text-sm font-medium text-slate-800 group-hover:text-blue-700">
                        <span className="rounded bg-blue-100 px-1.5 py-0.5 text-[11px] font-semibold text-blue-700">기사 #{article.id}</span>
                        <span>{article.title}</span>
                      </span>
                      <span className="mt-1 block text-xs text-slate-500">{article.sourceName} · {article.publishedAt ? format(new Date(article.publishedAt), 'M월 d일', { locale: ko }) : '발행일 미상'} · 관련도 {article.relevanceScore ?? '-'}</span>
                    </span>
                  </a>
                </li>
              ))}
            </ul>
          </section>
        </div>
      </section>
    </div>
  )
}
