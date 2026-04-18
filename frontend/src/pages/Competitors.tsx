import { useState, useMemo } from 'react'
import { useArticlesList } from '../hooks/useArticles'
import { Competitor, COMPETITOR_LABELS, CATEGORY_LABELS, Category } from '../types'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts'
import { ExternalLink } from 'lucide-react'

const COMPETITORS: Competitor[] = ['LG_CNS', 'SK_AX', 'BESPIN', 'PWC']

function ArticleSkeleton() {
  return (
    <div className="space-y-4 animate-pulse">
      {[1, 2, 3].map((g) => (
        <div key={g}>
          <div className="h-3 w-24 bg-gray-200 rounded mb-2" />
          <div className="space-y-2 ml-3 border-l-2 border-gray-100 pl-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="space-y-1">
                <div className="h-3 bg-gray-200 rounded w-full" />
                <div className="h-2 bg-gray-100 rounded w-3/4" />
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}

export default function Competitors() {
  const [activeComp, setActiveComp] = useState<Competitor>('LG_CNS')

  const dateFromStr = useMemo(() => {
    const since10 = new Date()
    since10.setDate(since10.getDate() - 10)
    return since10.toISOString().slice(0, 10) + 'T00:00:00'
  }, [])

  const { data: articles = [], isLoading } = useArticlesList({
    competitor: activeComp,
    dateFrom: dateFromStr,
    limit: 50,
  })

  const byDate = articles.reduce<Record<string, typeof articles>>((acc, a) => {
    const date = a.publishedAt ? a.publishedAt.slice(0, 10) : '날짜 없음'
    if (!acc[date]) acc[date] = []
    acc[date].push(a)
    return acc
  }, {})

  const catCount = articles.reduce<Record<string, number>>((acc, a) => {
    if (a.category) acc[a.category] = (acc[a.category] ?? 0) + 1
    return acc
  }, {})
  const catChartData = Object.entries(catCount).map(([cat, count]) => ({
    name: CATEGORY_LABELS[cat as Category] ?? cat,
    count,
  }))

  return (
    <div className="p-4 sm:p-6 space-y-4 sm:space-y-6">
      {/* 탭 */}
      <div className="overflow-x-auto">
        <div className="flex gap-1 bg-gray-100 rounded-lg p-1 w-max">
          {COMPETITORS.map((comp) => (
            <button
              key={comp}
              onClick={() => setActiveComp(comp)}
              className={`px-3 sm:px-5 py-2 rounded-md text-xs sm:text-sm font-medium whitespace-nowrap transition-colors ${
                activeComp === comp
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {COMPETITOR_LABELS[comp]}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 sm:gap-6">
        {/* 타임라인 */}
        <div className="md:col-span-2 card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">
            최근 10일 기사 타임라인 ({articles.length}건)
          </h3>
          <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2">
            {isLoading ? (
              <ArticleSkeleton />
            ) : Object.entries(byDate)
              .sort(([a], [b]) => b.localeCompare(a))
              .map(([date, dateArticles]) => (
                <div key={date}>
                  <div className="text-xs font-semibold text-gray-500 mb-2 sticky top-0 bg-white py-1">
                    {date !== '날짜 없음'
                      ? format(new Date(date), 'M월 d일 (E)', { locale: ko })
                      : date}
                    <span className="ml-2 text-gray-300">({dateArticles.length}건)</span>
                  </div>
                  <div className="space-y-2 ml-3 border-l-2 border-gray-100 pl-3">
                    {dateArticles.map((a) => (
                      <div key={a.id}>
                        <a
                          href={a.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-start gap-1 group"
                        >
                          <ExternalLink size={12} className="text-blue-400 mt-1 shrink-0 opacity-0 group-hover:opacity-100" />
                          <span className="text-sm text-gray-800 hover:text-blue-600 line-clamp-2">
                            {a.title}
                          </span>
                        </a>
                        {a.summary && (
                          <p className="text-xs text-gray-500 mt-0.5 line-clamp-1 ml-4">{a.summary}</p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            {!isLoading && articles.length === 0 && (
              <p className="text-sm text-gray-400 text-center py-8">기사가 없습니다</p>
            )}
          </div>
        </div>

        {/* 오른쪽 */}
        <div className="space-y-4">
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">카테고리 분포</h3>
            {catChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={180}>
                <BarChart data={catChartData} layout="vertical" margin={{ left: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" tick={{ fontSize: 11 }} />
                  <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={65} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#3b82f6" radius={[0, 3, 3, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-sm text-gray-400 text-center py-6">데이터 없음</p>
            )}
          </div>

          <div className="card bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-100">
            <h3 className="text-sm font-semibold text-blue-800 mb-2">
              {COMPETITOR_LABELS[activeComp]} 프로파일
            </h3>
            <p className="text-xs text-gray-600 leading-relaxed">
              최근 10일간 <strong>{articles.length}건</strong>의 기사가 수집되었습니다.
              {catChartData.length > 0 && (
                <> 주요 카테고리는 <strong>{catChartData[0]?.name}</strong>입니다.</>
              )}
            </p>
            <div className="mt-3 text-xs text-gray-500">
              {Object.entries(catCount).map(([cat, cnt]) => (
                <div key={cat} className="flex justify-between py-0.5">
                  <span>{CATEGORY_LABELS[cat as Category] ?? cat}</span>
                  <span className="font-medium">{cnt}건</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
