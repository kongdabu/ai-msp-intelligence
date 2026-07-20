import { useState } from 'react'
import { CalendarDays, Sparkles } from 'lucide-react'
import TrendNewsCard from '../components/trend/TrendNewsCard'
import TrendNewsPanel from '../components/trend/TrendNewsPanel'
import { useGenerateTrends, useTrend, useTrends } from '../hooks/useTrends'

const MONTHLY_PERIOD_TEXT = '최근 30일 수집 기사 기준'

export default function Trends() {
  const [selectedTrendId, setSelectedTrendId] = useState<number | null>(null)
  const { data: trends, isLoading } = useTrends()
  const { data: trendDetail } = useTrend(selectedTrendId)
  const { mutate: generate, isPending } = useGenerateTrends()

  return (
    <div className="mx-auto max-w-7xl space-y-7 p-4 sm:p-6 lg:p-8">
      <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-semibold text-orange-600">Monthly Trend News</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">반복되는 시장 신호를 읽는 3가지 흐름</h2>
          <p className="mt-2 flex items-center gap-1.5 text-sm text-slate-500"><CalendarDays size={15} /> {MONTHLY_PERIOD_TEXT}</p>
        </div>
        <button type="button" onClick={() => generate()} disabled={isPending} className="inline-flex w-fit items-center gap-2 rounded-xl bg-slate-950 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60">
          <Sparkles size={16} /> {isPending ? 'Trend News 생성 중...' : 'Top 3 초안 생성'}
        </button>
      </section>

      <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm leading-6 text-amber-900">
        최소 3개 근거 기사에서 확인된 공통 흐름만 초안으로 저장합니다. 게시 전에는 근거 기사와 전략 해석을 검토하세요.
      </div>

      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-3 animate-pulse">
          {Array.from({ length: 3 }).map((_, index) => <div key={index} className="h-72 rounded-2xl bg-slate-200" />)}
        </div>
      ) : trends?.length ? (
        <section className="grid gap-4 md:grid-cols-3" aria-label="생성된 Trend News">
          {trends.map((trend, index) => <TrendNewsCard key={trend.id} trend={trend} featured={index === 0} onSelect={setSelectedTrendId} />)}
        </section>
      ) : (
        <section className="rounded-3xl border border-dashed border-slate-300 bg-white px-6 py-16 text-center">
          <Sparkles size={28} className="mx-auto text-orange-500" />
          <h2 className="mt-3 text-lg font-bold text-slate-900">생성된 Trend News가 없습니다</h2>
          <p className="mt-1 text-sm text-slate-500">최근 30일간 수집된 기사를 분석해 Top 3 초안을 만들어보세요.</p>
        </section>
      )}

      {selectedTrendId && <TrendNewsPanel trend={trendDetail ?? null} onClose={() => setSelectedTrendId(null)} />}
    </div>
  )
}
