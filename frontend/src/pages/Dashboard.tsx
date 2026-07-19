import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowUpRight, CheckCheck, Database, Lightbulb, Newspaper, Sparkles, Zap } from 'lucide-react'
import { useDashboard } from '../hooks/useDashboard'
import { useGenerateInsights, useInsight } from '../hooks/useInsights'
import { Insight } from '../types'
import InsightPanel from '../components/insight/InsightPanel'
import InsightNewsCard from '../components/dashboard/InsightNewsCard'
import CompetitorDonut from '../components/dashboard/CompetitorDonut'
import TrendChart from '../components/dashboard/TrendChart'

const BRIEFING_DATE_FORMATTER = new Intl.DateTimeFormat('ko-KR', {
  month: 'long',
  day: 'numeric',
  weekday: 'short',
})

function DashboardSkeleton() {
  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8 animate-pulse">
      <div className="h-16 w-64 rounded-2xl bg-slate-200" />
      <div className="h-80 rounded-3xl bg-slate-200" />
      <div className="grid gap-4 md:grid-cols-3">
        {Array.from({ length: 3 }).map((_, index) => <div key={index} className="h-60 rounded-2xl bg-slate-200" />)}
      </div>
    </div>
  )
}

export default function Dashboard() {
  const { data, isLoading } = useDashboard()
  const [selectedInsightId, setSelectedInsightId] = useState<number | null>(null)
  const [generateResult, setGenerateResult] = useState<{ count: number } | null>(null)
  const { data: insightDetail } = useInsight(selectedInsightId)
  const { mutate: generate, isPending } = useGenerateInsights({
    onSuccess: (insights: Insight[]) => setGenerateResult({ count: insights.length }),
  })

  const sortedInsights = useMemo(
    () => [...(data?.latestInsights ?? [])].sort((a, b) => b.impactScore - a.impactScore),
    [data?.latestInsights],
  )
  const leadInsight = sortedInsights[0]
  const supportingInsights = sortedInsights.slice(1, 4)

  useEffect(() => {
    if (!generateResult) return
    const timer = window.setTimeout(() => setGenerateResult(null), 6000)
    return () => window.clearTimeout(timer)
  }, [generateResult])

  if (isLoading) return <DashboardSkeleton />
  if (!data) return null

  const stats = [
    { label: '오늘 수집', value: data.todayArticleCount, icon: Newspaper, tone: 'text-blue-600 bg-blue-50' },
    { label: '분석 대기', value: data.unprocessedInsightCount, icon: Lightbulb, tone: 'text-amber-600 bg-amber-50' },
    { label: '고영향 인사이트', value: data.highImpactInsightCount, icon: Zap, tone: 'text-rose-600 bg-rose-50' },
    { label: '활성 소스', value: data.activeSourceCount, icon: Database, tone: 'text-emerald-600 bg-emerald-50' },
  ]

  return (
    <div className="mx-auto max-w-7xl space-y-8 p-4 sm:p-6 lg:p-8">
      <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-semibold text-blue-600">{BRIEFING_DATE_FORMATTER.format(new Date())} 전략 브리핑</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">지금 놓치면 안 될 경쟁 인사이트</h2>
          <p className="mt-2 text-sm text-slate-500">AI MSP 전략에 바로 연결되는 변화와 대응 과제를 한눈에 확인하세요.</p>
        </div>
        <button
          type="button"
          onClick={() => generate()}
          disabled={isPending}
          className="inline-flex w-fit items-center gap-2 rounded-xl bg-slate-950 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <Sparkles size={16} />
          {isPending ? '인사이트 생성 중...' : '새 인사이트 분석'}
        </button>
      </section>

      {generateResult && (
        <div className="flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
          <CheckCheck size={17} className="shrink-0" />
          {generateResult.count > 0 ? `새 인사이트 ${generateResult.count}건을 생성했습니다.` : '처리할 신규 기사가 없거나 새 인사이트를 생성하지 못했습니다.'}
          <button type="button" onClick={() => setGenerateResult(null)} className="ml-auto text-emerald-700 hover:text-emerald-950" aria-label="알림 닫기">×</button>
        </div>
      )}

      {leadInsight ? (
        <section aria-label="오늘의 핵심 인사이트">
          <InsightNewsCard insight={leadInsight} featured onSelect={setSelectedInsightId} />
        </section>
      ) : (
        <section className="rounded-3xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
          <Sparkles size={28} className="mx-auto text-blue-500" />
          <h2 className="mt-3 text-lg font-bold text-slate-900">아직 준비된 인사이트가 없습니다</h2>
          <p className="mt-1 text-sm text-slate-500">새 기사를 분석하면 이곳에서 카드 뉴스로 확인할 수 있습니다.</p>
        </section>
      )}

      <section aria-labelledby="insight-feed-title">
        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Insight feed</p>
            <h2 id="insight-feed-title" className="mt-1 text-lg font-bold text-slate-900">이어서 볼 인사이트</h2>
          </div>
          <Link to="/insights" className="inline-flex items-center gap-1 text-sm font-semibold text-blue-600 hover:text-blue-800">
            전체 보기 <ArrowUpRight size={15} />
          </Link>
        </div>
        {supportingInsights.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-3">
            {supportingInsights.map((insight) => (
              <InsightNewsCard key={insight.id} insight={insight} onSelect={setSelectedInsightId} />
            ))}
          </div>
        ) : (
          <p className="rounded-2xl bg-slate-100 px-4 py-8 text-center text-sm text-slate-500">추가 인사이트가 생성되면 이곳에 표시됩니다.</p>
        )}
      </section>

      <section className="border-t border-slate-200 pt-8" aria-labelledby="signals-title">
        <div className="mb-4">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Signal overview</p>
          <h2 id="signals-title" className="mt-1 text-lg font-bold text-slate-900">인사이트를 뒷받침하는 신호</h2>
        </div>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map(({ label, value, icon: Icon, tone }) => (
            <div key={label} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
              <div className={`inline-flex rounded-xl p-2 ${tone}`}><Icon size={18} /></div>
              <p className="mt-4 text-2xl font-bold tracking-tight text-slate-950">{value.toLocaleString()}</p>
              <p className="mt-1 text-xs font-medium text-slate-500">{label}</p>
            </div>
          ))}
        </div>
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-700">경쟁사별 기사 분포</h3>
            <div className="mt-3"><CompetitorDonut data={data.competitorDistribution ?? {}} /></div>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-700">7일 카테고리 트렌드</h3>
            <div className="mt-3"><TrendChart data={data.categoryTrends ?? []} /></div>
          </div>
        </div>
      </section>

      {selectedInsightId && <InsightPanel insight={insightDetail ?? null} onClose={() => setSelectedInsightId(null)} />}
    </div>
  )
}
