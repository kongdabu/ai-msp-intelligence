import { ArrowUpRight, FileText, Flame, Layers3 } from 'lucide-react'
import { TrendNews } from '../../types'

interface TrendNewsCardProps {
  trend: TrendNews
  featured?: boolean
  onSelect: (id: number) => void
}

export default function TrendNewsCard({ trend, featured = false, onSelect }: TrendNewsCardProps) {
  const score = trend.trendScore ?? 0

  return (
    <article className={`relative overflow-hidden rounded-2xl border p-5 transition duration-200 hover:-translate-y-1 hover:shadow-lg ${
      featured
        ? 'border-slate-800 bg-slate-950 text-white shadow-slate-900/20'
        : 'border-slate-200 bg-white text-slate-900 shadow-sm'
    }`}>
      {featured && <div className="absolute -right-16 -top-20 h-56 w-56 rounded-full bg-orange-400/20 blur-3xl" aria-hidden="true" />}
      <div className="relative flex min-h-[235px] flex-col">
        <div className="flex items-center justify-between gap-3">
          <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${
            featured ? 'bg-white/10 text-orange-200 ring-1 ring-white/15' : 'bg-orange-50 text-orange-700'
          }`}>
            <Flame size={14} /> Hot Trend #{score}
          </span>
          <span className={`rounded-full px-2 py-1 text-[11px] font-semibold ${
            featured ? 'bg-white/10 text-slate-300' : 'bg-slate-100 text-slate-500'
          }`}>
            초안
          </span>
        </div>

        <button
          type="button"
          onClick={() => onSelect(trend.id)}
          className={`mt-5 text-left outline-none focus-visible:ring-2 focus-visible:ring-orange-400 focus-visible:ring-offset-2 ${
            featured ? 'focus-visible:ring-offset-slate-950' : 'focus-visible:ring-offset-white'
          }`}
          aria-label={`${trend.title} 상세 보기`}
        >
          <h2 className={`text-lg font-bold leading-7 ${featured ? 'text-white' : 'text-slate-900'} line-clamp-2`}>{trend.title}</h2>
          <p className={`mt-3 text-sm leading-6 ${featured ? 'text-slate-300' : 'text-slate-600'} line-clamp-3`}>{trend.summary}</p>
        </button>

        <div className="mt-auto pt-5">
          <div className="flex flex-wrap gap-1.5">
            {trend.keywords.slice(0, 4).map((keyword) => (
              <span key={keyword} className={`rounded-md px-2 py-1 text-xs ${featured ? 'bg-white/10 text-slate-200' : 'bg-slate-100 text-slate-600'}`}>
                #{keyword}
              </span>
            ))}
          </div>
          <div className={`mt-4 flex items-center justify-between text-xs ${featured ? 'text-slate-400' : 'text-slate-400'}`}>
            <span className="inline-flex items-center gap-1"><Layers3 size={13} /> 근거 {trend.sourceArticleCount}건</span>
            <button type="button" onClick={() => onSelect(trend.id)} className={`inline-flex items-center gap-1 font-semibold ${featured ? 'text-orange-200 hover:text-white' : 'text-blue-600 hover:text-blue-800'}`}>
              읽기 <ArrowUpRight size={14} />
            </button>
          </div>
        </div>
      </div>
      {!featured && <FileText className="pointer-events-none absolute -bottom-5 -right-5 text-slate-100" size={100} aria-hidden="true" />}
    </article>
  )
}
