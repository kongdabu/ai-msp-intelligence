import { useState, useEffect } from 'react'
import { Building2, ExternalLink, Search, X, CalendarDays, Banknote, RefreshCw } from 'lucide-react'
import { useArticles, useTriggerProcurementCrawl } from '../hooks/useArticles'
import { Article } from '../types'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

function parseContent(content: string | null) {
  if (!content) return { institution: '', bidDate: '', budget: '' }
  const institution = content.match(/발주기관:\s*([^|]+)/)?.[1]?.trim() ?? ''
  const bidDate     = content.match(/공고일:\s*([^|]+)/)?.[1]?.trim() ?? ''
  const budget      = content.match(/예산:\s*(.+)/)?.[1]?.trim() ?? ''
  return { institution, bidDate, budget }
}

function ProcurementCard({ article, onClick, selected }: {
  article: Article
  onClick: () => void
  selected: boolean
}) {
  const { institution, budget } = parseContent(article.originalContent ?? null)

  return (
    <button
      onClick={onClick}
      className={`w-full text-left p-4 rounded-lg border transition-all hover:shadow-md ${
        selected ? 'border-blue-500 bg-blue-50' : 'border-gray-200 bg-white hover:border-blue-300'
      }`}
    >
      <p className="text-sm font-semibold text-gray-900 line-clamp-2 mb-2">{article.title}</p>
      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
        {institution && (
          <span className="flex items-center gap-1">
            <Building2 size={12} /> {institution}
          </span>
        )}
        {budget && (
          <span className="flex items-center gap-1">
            <Banknote size={12} /> {budget}
          </span>
        )}
        <span className="flex items-center gap-1">
          <CalendarDays size={12} />
          {article.publishedAt
            ? format(new Date(article.publishedAt), 'yyyy.MM.dd', { locale: ko })
            : '-'}
        </span>
      </div>
    </button>
  )
}

function DetailPanel({ article, onClose }: { article: Article; onClose: () => void }) {
  const { institution, bidDate, budget } = parseContent(article.originalContent ?? null)

  return (
    <div className="p-5 space-y-4">
      <div className="flex items-start justify-between">
        <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-medium">나라장터 공고</span>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
          <X size={18} />
        </button>
      </div>

      <h2 className="text-base font-bold text-gray-900 leading-snug">{article.title}</h2>

      <div className="space-y-2 text-sm">
        {institution && (
          <div className="flex items-center gap-2 text-gray-600">
            <Building2 size={15} className="text-gray-400 shrink-0" />
            <span>{institution}</span>
          </div>
        )}
        {bidDate && (
          <div className="flex items-center gap-2 text-gray-600">
            <CalendarDays size={15} className="text-gray-400 shrink-0" />
            <span>{bidDate}</span>
          </div>
        )}
        {budget && (
          <div className="flex items-center gap-2 text-gray-600">
            <Banknote size={15} className="text-gray-400 shrink-0" />
            <span>{budget}</span>
          </div>
        )}
      </div>

      <a
        href={article.url}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
      >
        <ExternalLink size={14} /> 나라장터에서 보기
      </a>

      {article.summary && (
        <div className="bg-blue-50 rounded-lg p-4">
          <p className="text-xs font-semibold text-blue-700 mb-1.5">AI 요약</p>
          <p className="text-sm text-gray-700 leading-relaxed">{article.summary}</p>
        </div>
      )}
    </div>
  )
}

export default function Procurement() {
  const [page, setPage]               = useState(0)
  const [keyword, setKeyword]         = useState('')
  const [dateFrom, setDateFrom]       = useState('')
  const [dateTo, setDateTo]           = useState('')
  const [selected, setSelected]       = useState<Article | null>(null)
  const [inputValue, setInputValue]   = useState('')
  const [crawlMsg, setCrawlMsg] = useState<string | null>(null)
  const { mutate: crawl, isPending: isCrawling } = useTriggerProcurementCrawl({
    onSuccess: (data) => setCrawlMsg(`신규 ${data.crawledCount}건 수집 완료`),
  })

  const { data, isLoading } = useArticles({
    sourceType: 'PROCUREMENT',
    keyword: keyword || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
    page,
    size: 20,
  })

  useEffect(() => { setPage(0) }, [keyword, dateFrom, dateTo])

  const handleSearch = () => setKeyword(inputValue)
  const handleReset  = () => { setKeyword(''); setInputValue(''); setDateFrom(''); setDateTo('') }
  const hasFilter    = keyword || dateFrom || dateTo

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <Building2 size={22} className="text-blue-600" />
          <h1 className="text-xl font-bold text-gray-900">나라장터 공고</h1>
          <span className="text-sm text-gray-400">공공기관 AI·클라우드·MSP 발주 현황</span>
        </div>
        <button
          onClick={() => crawl()}
          disabled={isCrawling}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <RefreshCw size={14} className={isCrawling ? 'animate-spin' : ''} />
          {isCrawling ? '수집 중...' : '발주공고 수집'}
        </button>
      </div>

      {crawlMsg && (
        <div className="bg-green-50 border border-green-200 text-green-800 text-sm rounded-lg px-4 py-3 flex justify-between">
          ✅ {crawlMsg}
          <button onClick={() => setCrawlMsg(null)} className="text-green-600">✕</button>
        </div>
      )}

      {/* 검색 필터 */}
      <div className="bg-white border border-gray-200 rounded-lg p-4 flex flex-wrap gap-3 items-center">
        <div className="flex gap-2 flex-1 min-w-[200px]">
          <input
            type="text"
            placeholder="공고명 검색..."
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            className="flex-1 border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSearch}
            className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            <Search size={15} />
          </button>
        </div>
        <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)}
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <span className="text-gray-400 text-sm">~</span>
        <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)}
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
        {hasFilter && (
          <button onClick={handleReset} className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
            <X size={14} /> 초기화
          </button>
        )}
      </div>

      {data && (
        <p className="text-sm text-gray-500">
          총 <span className="font-semibold text-gray-900">{data.totalElements.toLocaleString()}</span>건
        </p>
      )}

      {/* 목록 + 상세 패널 */}
      <div className={`transition-all ${selected ? 'md:mr-96' : ''}`}>
        {isLoading ? (
          <div className="text-center py-16 text-gray-400">불러오는 중...</div>
        ) : data?.content.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <Building2 size={40} className="mx-auto mb-3 text-gray-300" />
            <p className="text-sm">수집된 공고가 없습니다.</p>
            <p className="text-xs mt-1 text-gray-400">크롤링 후 AI/인공지능/클라우드/MSP 키워드 공고가 표시됩니다.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {data?.content.map(article => (
                <ProcurementCard
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
