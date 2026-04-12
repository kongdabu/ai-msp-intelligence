import { useFilterStore } from '../../store/filterStore'
import { Competitor, Category, SourceType } from '../../types'
import { X } from 'lucide-react'

const COMPETITORS: { value: Competitor | ''; label: string }[] = [
  { value: '', label: '전체 경쟁사' },
  { value: 'LG_CNS', label: 'LG CNS' },
  { value: 'SK_AX', label: 'SK AX' },
  { value: 'BESPIN', label: '베스핀글로벌' },
  { value: 'PWC', label: 'PwC' },
  { value: 'GENERAL', label: '일반' },
]

const CATEGORIES: { value: Category | ''; label: string }[] = [
  { value: '', label: '전체 카테고리' },
  { value: 'AI_AGENT', label: 'AI Agent' },
  { value: 'VERTICAL_AI', label: 'Vertical AI' },
  { value: 'ITO', label: 'ITO' },
  { value: 'MSP', label: 'MSP' },
  { value: 'CLOUD', label: 'Cloud' },
  { value: 'GEN_AI', label: 'Gen AI' },
]

const SOURCE_TYPES: { value: SourceType | ''; label: string }[] = [
  { value: '', label: '전체 소스' },
  { value: 'NEWS', label: '뉴스' },
  { value: 'HOMEPAGE', label: '홈페이지' },
  { value: 'SNS', label: 'SNS' },
  { value: 'IDC', label: 'IDC 리포트' },
]

export default function ArticleFilter() {
  const { articleFilter, setArticleFilter, resetArticleFilter } = useFilterStore()

  const hasFilter =
    articleFilter.competitor ||
    articleFilter.category ||
    articleFilter.sourceType ||
    articleFilter.keyword

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4">
      <div className="flex flex-wrap gap-3 items-center">
        {/* 경쟁사 */}
        <select
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={articleFilter.competitor}
          onChange={(e) => setArticleFilter({ competitor: e.target.value as Competitor | '' })}
        >
          {COMPETITORS.map(({ value, label }) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>

        {/* 카테고리 */}
        <select
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={articleFilter.category}
          onChange={(e) => setArticleFilter({ category: e.target.value as Category | '' })}
        >
          {CATEGORIES.map(({ value, label }) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>

        {/* 소스 타입 */}
        <select
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={articleFilter.sourceType}
          onChange={(e) => setArticleFilter({ sourceType: e.target.value as SourceType | '' })}
        >
          {SOURCE_TYPES.map(({ value, label }) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>

        {/* 날짜 범위 */}
        <input
          type="date"
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={articleFilter.dateFrom}
          onChange={(e) => setArticleFilter({ dateFrom: e.target.value })}
        />
        <span className="text-gray-400 text-sm">~</span>
        <input
          type="date"
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={articleFilter.dateTo}
          onChange={(e) => setArticleFilter({ dateTo: e.target.value })}
        />

        {/* 키워드 */}
        <input
          type="text"
          placeholder="키워드 검색..."
          className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[180px]"
          value={articleFilter.keyword}
          onChange={(e) => setArticleFilter({ keyword: e.target.value })}
        />

        {/* 초기화 */}
        {hasFilter && (
          <button
            onClick={resetArticleFilter}
            className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
          >
            <X size={14} />
            초기화
          </button>
        )}
      </div>
    </div>
  )
}
