import { useDashboard } from '../hooks/useDashboard'
import { useGenerateInsights } from '../hooks/useInsights'
import CompetitorDonut from '../components/dashboard/CompetitorDonut'
import TrendChart from '../components/dashboard/TrendChart'
import KeywordCloud from '../components/dashboard/KeywordCloud'
import InsightCard from '../components/insight/InsightCard'
import { Insight } from '../types'
import { useState, useEffect } from 'react'
import InsightPanel from '../components/insight/InsightPanel'
import { useInsight } from '../hooks/useInsights'
import { Newspaper, Lightbulb, Zap, Database, Sparkles, CheckCheck } from 'lucide-react'
import { Insight } from '../types'

export default function Dashboard() {
  const { data, isLoading } = useDashboard()
  const [selectedInsightId, setSelectedInsightId] = useState<number | null>(null)
  const { data: insightDetail } = useInsight(selectedInsightId)
  const [generateResult, setGenerateResult] = useState<{ count: number } | null>(null)

  const { mutate: generate, isPending } = useGenerateInsights({
    onSuccess: (insights: Insight[]) => {
      setGenerateResult({ count: insights.length })
    },
  })

  useEffect(() => {
    if (!generateResult) return
    const timer = setTimeout(() => setGenerateResult(null), 6000)
    return () => clearTimeout(timer)
  }, [generateResult])

  if (isLoading) {
    return (
      <div className="p-6 space-y-6 animate-pulse">
        <div className="grid grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-24 bg-gray-200 rounded-lg" />
          ))}
        </div>
      </div>
    )
  }

  if (!data) return null

  const kpis = [
    { label: '오늘 수집 기사', value: data.todayArticleCount, icon: Newspaper, color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: '미처리 인사이트', value: data.unprocessedInsightCount, icon: Lightbulb, color: 'text-amber-600', bg: 'bg-amber-50' },
    { label: '고영향도 인사이트', value: data.highImpactInsightCount, icon: Zap, color: 'text-red-600', bg: 'bg-red-50' },
    { label: '활성 소스', value: data.activeSourceCount, icon: Database, color: 'text-green-600', bg: 'bg-green-50' },
  ]

  return (
    <div className="p-6 space-y-6">
      {/* KPI 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {kpis.map(({ label, value, icon: Icon, color, bg }) => (
          <div key={label} className="card flex items-center gap-4">
            <div className={`${bg} p-3 rounded-lg`}>
              <Icon size={22} className={color} />
            </div>
            <div>
              <div className="text-2xl font-bold text-gray-900">{value.toLocaleString()}</div>
              <div className="text-xs text-gray-500 mt-0.5">{label}</div>
            </div>
          </div>
        ))}
      </div>

      {/* 차트 영역 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">경쟁사별 기사 분포</h3>
          <CompetitorDonut data={data.competitorDistribution ?? {}} />
        </div>
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">7일 카테고리 트렌드</h3>
          <TrendChart data={data.categoryTrends ?? []} />
        </div>
      </div>

      {/* 인사이트 + 기사 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 오늘의 인사이트 */}
        <div className="lg:col-span-2 card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-700">오늘의 AI 인사이트</h3>
            <button
              onClick={() => generate()}
              disabled={isPending}
              className="flex items-center gap-1 text-xs btn-primary"
            >
              <Sparkles size={14} />
              {isPending ? '생성 중...' : '인사이트 생성'}
            </button>
          </div>
          {generateResult && (
            <div className="flex items-center gap-2 px-3 py-2 mb-3 bg-green-50 border border-green-200 rounded-lg text-xs text-green-800">
              <CheckCheck size={14} className="shrink-0" />
              <span>
                {generateResult.count > 0
                  ? <>인사이트 <strong>{generateResult.count}건</strong> 생성 완료. 목록이 갱신되었습니다.</>
                  : '처리할 신규 기사가 없거나 인사이트를 생성하지 못했습니다.'}
              </span>
              <button onClick={() => setGenerateResult(null)} className="ml-auto text-green-600 hover:text-green-800">✕</button>
            </div>
          )}
          {data.latestInsights?.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-8">인사이트가 없습니다. 위 버튼을 눌러 생성해보세요.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {data.latestInsights?.slice(0, 4).map((insight) => (
                <InsightCard
                  key={insight.id}
                  insight={insight}
                  onClick={(ins: Insight) => setSelectedInsightId(ins.id)}
                />
              ))}
            </div>
          )}
        </div>

        {/* 최신 기사 */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">최신 기사</h3>
          <div className="space-y-3">
            {data.latestArticles?.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-4">기사가 없습니다</p>
            ) : (
              data.latestArticles?.map((article) => (
                <a
                  key={article.id}
                  href={article.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="block hover:bg-gray-50 -mx-2 px-2 py-2 rounded-md"
                >
                  <p className="text-sm text-gray-800 font-medium line-clamp-2">{article.title ?? '(제목 없음)'}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{article.sourceName ?? ''}</p>
                </a>
              ))
            )}
          </div>
        </div>
      </div>

      {/* 키워드 클라우드 */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">주요 키워드</h3>
        <KeywordCloud insights={data.latestInsights ?? []} />
      </div>

      {/* 인사이트 상세 모달 */}
      {selectedInsightId && (
        <InsightPanel
          insight={insightDetail ?? null}
          onClose={() => setSelectedInsightId(null)}
        />
      )}
    </div>
  )
}
