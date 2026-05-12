import { useState, useEffect } from 'react'
import { BriefcaseBusiness, ExternalLink, X, CalendarDays, Building2, RefreshCw } from 'lucide-react'
import { useArticles, useTriggerJobPostingCrawl } from '../hooks/useArticles'
import { Article, Competitor, COMPETITOR_LABELS, COMPETITOR_COLORS } from '../types'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

const TABS: { value: Competitor | 'ALL'; label: string }[] = [
  { value: 'ALL',    label: '전체' },
  { value: 'LG_CNS', label: 'LG CNS' },
  { value: 'SK_AX',  label: 'SK AX' },
  { value: 'BESPIN', label: '베스핀글로벌' },
  { value: 'PWC',    label: 'PwC' },
]

function JobCard({ article, onClick, selected }: {
  article: Article
  onClick: () => void
  selected: boolean
}) {
  const color = COMPETITOR_COLORS[article.competitor] ?? '#6b7280'
  const label = COMPETITOR_LABELS[article.competitor] ?? article.competitor

  return (
    <button
      onClick={onClick}
      className={`w-full text-left p-4 rounded-lg border transition-all hover:shadow-md ${
        selected ? 'border-blue-500 bg-blue-50' : 'border-gray-200 bg-white hover:border-blue-300'
      }`}
    >
      {/* 경쟁사 배지 */}
      <div className="flex items-center gap-2 mb-2">
        <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: color }} />
        <span className="text-xs font-semibold" style={{ color }}>{label}</span>
      </div>

      <p className="text-sm font-semibold text-gray-900 line-clamp-2 mb-2">{article.title}</p>

      {article.originalContent && (
        <p className="text-xs text-gray-500 line-clamp-2 mb-2">{article.originalContent}</p>
      )}

      <div className="flex items-center gap-1 text-xs text-gray-400">
        <CalendarDays size={11} />
        {article.publishedAt
          ? format(new Date(article.publishedAt), 'yyyy.MM.dd', { locale: ko })
          : '-'}
      </div>
    </button>
  )
}

function DetailPanel({ article, onClose }: { article: Article; onClose: () => void }) {
  const color = COMPETITOR_COLORS[article.competitor] ?? '#6b7280'
  const label = COMPETITOR_LABELS[article.competitor] ?? article.competitor

  return (
    <div className="p-5 space-y-4">
      <div className="flex items-start justify-between">
        <span
          className="text-xs px-2 py-0.5 rounded-full font-semibold text-white"
          style={{ backgroundColor: color }}
        >
          {label}
        </span>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
          <X size={18} />
        </button>
      </div>

      <h2 className="text-base font-bold text-gray-900 leading-snug">{article.title}</h2>

      <div className="flex flex-wrap gap-3 text-sm text-gray-500">
        <span className="flex items-center gap-1.5">
          <Building2 size={14} className="text-gray-400" />{article.sourceName}
        </span>
        <span className="flex items-center gap-1.5">
          <CalendarDays size={14} className="text-gray-400" />
          {article.publishedAt
            ? format(new Date(article.publishedAt), 'yyyy년 MM월 dd일', { locale: ko })
            : '-'}
        </span>
      </div>

      <a
        href={article.url}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
      >
        <ExternalLink size={14} /> 채용공고 보기
      </a>

      {article.originalContent && (
        <div className="bg-gray-50 rounded-lg p-4">
          <p className="text-xs font-semibold text-gray-500 mb-1.5">직무 설명</p>
          <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {article.originalContent}
          </p>
        </div>
      )}

      {article.summary && (
        <div className="bg-blue-50 rounded-lg p-4">
          <p className="text-xs font-semibold text-blue-700 mb-1.5">AI 요약</p>
          <p className="text-sm text-gray-700 leading-relaxed">{article.summary}</p>
        </div>
      )}
    </div>
  )
}

export default function JobPostings() {
  const [activeTab, setActiveTab] = useState<Competitor | 'ALL'>('ALL')
  const [page, setPage]           = useState(0)
  const [selected, setSelected]   = useState<Article | null>(null)
  const [crawlMsg, setCrawlMsg] = useState<string | null>(null)
  const { mutate: crawl, isPending: isCrawling } = useTriggerJobPostingCrawl({
    onSuccess: (data) => setCrawlMsg(`신규 ${data.crawledCount}건 수집 완료`),
  })

  useEffect(() => { setPage(0); setSelected(null) }, [activeTab])

  const { data, isLoading } = useArticles({
    sourceType: 'JOB_POSTING',
    competitor: activeTab === 'ALL' ? undefined : activeTab,
    page,
    size: 20,
  })

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <BriefcaseBusiness size={22} className="text-blue-600" />
          <h1 className="text-xl font-bold text-gray-900">경쟁사 채용공고</h1>
          <span className="text-sm text-gray-400">채용 포지션으로 읽는 경쟁사 전략 방향</span>
        </div>
        <button
          onClick={() => crawl()}
          disabled={isCrawling}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <RefreshCw size={14} className={isCrawling ? 'animate-spin' : ''} />
          {isCrawling ? '수집 중...' : '채용동향 수집'}
        </button>
      </div>

      {crawlMsg && (
        <div className="bg-green-50 border border-green-200 text-green-800 text-sm rounded-lg px-4 py-3 flex justify-between">
          ✅ {crawlMsg}
          <button onClick={() => setCrawlMsg(null)} className="text-green-600">✕</button>
        </div>
      )}

      {/* 경쟁사 탭 */}
      <div className="flex gap-1 border-b border-gray-200 overflow-x-auto">
        {TABS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => setActiveTab(value)}
            className={`px-4 py-2 text-sm font-medium whitespace-nowrap rounded-t-lg transition-colors shrink-0 ${
              activeTab === value
                ? 'bg-white border border-b-white border-gray-200 -mb-px text-blue-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {label}
            {activeTab === value && data && (
              <span className="ml-1.5 text-xs bg-blue-100 text-blue-600 px-1.5 py-0.5 rounded-full">
                {data.totalElements}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* 건수 */}
      {data && activeTab !== 'ALL' && (
        <p className="text-sm text-gray-500">
          <span className="font-semibold" style={{ color: COMPETITOR_COLORS[activeTab as Competitor] }}>
            {COMPETITOR_LABELS[activeTab as Competitor]}
          </span>
          {' '}채용공고 총{' '}
          <span className="font-semibold text-gray-900">{data.totalElements}</span>건
        </p>
      )}

      {/* 목록 + 상세 패널 */}
      <div className={`transition-all ${selected ? 'md:mr-96' : ''}`}>
        {isLoading ? (
          <div className="text-center py-16 text-gray-400">불러오는 중...</div>
        ) : data?.content.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <BriefcaseBusiness size={40} className="mx-auto mb-3 text-gray-300" />
            <p className="text-sm">수집된 채용공고가 없습니다.</p>
            <p className="text-xs mt-1">사람인 API 키 설정 후 크롤링 시 표시됩니다.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {data?.content.map(article => (
                <JobCard
                  key={article.id}
                  article={article}
                  selected={selected?.id === article.id}
                  onClick={() => setSelected(prev => prev?.id === article.id ? null : article)}
                />
              ))}
            </div>

            {/* 페이지네이션 */}
            {(data?.totalPages ?? 0) > 1 && (
              <div className="flex justify-center gap-2 mt-6">
                <button disabled={page === 0}
                  onClick={() => setPage(p => p - 1)}
                  className="px-3 py-1.5 text-sm border rounded-md disabled:opacity-40 hover:bg-gray-50">
                  이전
                </button>
                <span className="px-3 py-1.5 text-sm text-gray-600">
                  {page + 1} / {data?.totalPages}
                </span>
                <button disabled={page + 1 >= (data?.totalPages ?? 0)}
                  onClick={() => setPage(p => p + 1)}
                  className="px-3 py-1.5 text-sm border rounded-md disabled:opacity-40 hover:bg-gray-50">
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {/* 모바일 오버레이 */}
      {selected && (
        <div className="md:hidden fixed inset-0 bg-white z-40 overflow-y-auto">
          <DetailPanel article={selected} onClose={() => setSelected(null)} />
        </div>
      )}

      {/* 데스크탑 우측 패널 */}
      {selected && (
        <div className="hidden md:block fixed right-0 top-0 h-full w-96 bg-white border-l border-gray-200 shadow-xl z-40 overflow-y-auto">
          <DetailPanel article={selected} onClose={() => setSelected(null)} />
        </div>
      )}
    </div>
  )
}
