import { useFilterStore } from '../../store/filterStore'
import { Category, SourceType } from '../../types'
import { X } from 'lucide-react'

const CATEGORIES: { value: Category | ''; label: string }[] = [
  { value: '', label: '전체 관심 주제' },
  { value: 'FRONTIER_LABS', label: 'Frontier AI Labs' },
  { value: 'AI_ECOSYSTEM', label: 'AI 생태계' },
  { value: 'AI_DELIVERY_MODEL', label: 'AI 서비스 모델' },
  { value: 'CONSULTING', label: '컨설팅' },
  { value: 'AGENTIC_OPERATIONS', label: 'Agentic AI·AIOps' },
]

const SOURCE_TYPES: { value: SourceType | ''; label: string }[] = [
  { value: '', label: '전체 소스' },
  { value: 'NEWS', label: '뉴스' },
  { value: 'HOMEPAGE', label: '홈페이지' },
  { value: 'SNS', label: 'SNS' },
  { value: 'IDC', label: 'IDC 리포트' },
  { value: 'PROCUREMENT', label: '나라장터 공고' },
  { value: 'JOB_POSTING', label: '채용공고' },
]

export default function ArticleFilter() {
  const { articleFilter, setArticleFilter, resetArticleFilter } = useFilterStore()

  const hasFilter =
    articleFilter.category ||
    articleFilter.sourceType ||
    articleFilter.keyword

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4">
      <div className="flex flex-wrap gap-3 items-center">
        {/* 관심 주제 */}
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
