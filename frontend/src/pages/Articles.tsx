import { useState } from 'react'
import { useArticles } from '../hooks/useArticles'
import { useFilterStore } from '../store/filterStore'
import ArticleFilter from '../components/article/ArticleFilter'
import ArticleList from '../components/article/ArticleList'
import { Article, COMPETITOR_LABELS, CATEGORY_LABELS, COMPETITOR_COLORS } from '../types'
import { X, ExternalLink } from 'lucide-react'

export default function Articles() {
  const [page, setPage] = useState(0)
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null)
  const { articleFilter } = useFilterStore()

  const { data, isLoading } = useArticles({
    ...articleFilter,
    page,
    size: 18,
  })

  const handleArticleClick = (article: Article) => {
    setSelectedArticle(article)
  }

  return (
    <div className="p-6 flex gap-6">
      {/* 메인 콘텐츠 */}
      <div className={`flex-1 min-w-0 space-y-4 transition-all ${selectedArticle ? 'lg:mr-96' : ''}`}>
        <ArticleFilter />

        {/* 결과 수 */}
        {data && (
          <div className="text-sm text-gray-500">
            총 <span className="font-semibold text-gray-900">{data.totalElements.toLocaleString()}</span>건
          </div>
        )}

        <ArticleList
          articles={data?.content ?? []}
          totalPages={data?.totalPages ?? 0}
          currentPage={page}
          onPageChange={(p) => setPage(p)}
          onArticleClick={handleArticleClick}
          isLoading={isLoading}
        />
      </div>

      {/* 우측 슬라이드 패널 */}
      {selectedArticle && (
        <div className="fixed right-0 top-0 h-full w-96 bg-white border-l border-gray-200 shadow-xl z-40 overflow-y-auto">
          <div className="p-5">
            {/* 패널 헤더 */}
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-2 flex-wrap">
                <span
                  className="badge text-white"
                  style={{ backgroundColor: COMPETITOR_COLORS[selectedArticle.competitor] ?? '#6b7280' }}
                >
                  {COMPETITOR_LABELS[selectedArticle.competitor]}
                </span>
                {selectedArticle.category && (
                  <span className="badge bg-gray-100 text-gray-700">
                    {CATEGORY_LABELS[selectedArticle.category]}
                  </span>
                )}
              </div>
              <button
                onClick={() => setSelectedArticle(null)}
                className="text-gray-400 hover:text-gray-600 ml-2"
              >
                <X size={20} />
              </button>
            </div>

            <h2 className="text-base font-bold text-gray-900 mb-1">{selectedArticle.title}</h2>
            <p className="text-xs text-gray-400 mb-4">
              {selectedArticle.sourceName} ·{' '}
              {selectedArticle.publishedAt
                ? new Date(selectedArticle.publishedAt).toLocaleDateString('ko-KR')
                : ''}
            </p>

            {/* 원문 링크 */}
            <a
              href={selectedArticle.url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 btn-primary w-full justify-center mb-4"
            >
              <ExternalLink size={14} />
              원문 보기
            </a>

            {/* AI 요약 */}
            {selectedArticle.summary && (
              <div className="bg-blue-50 rounded-lg p-4 mb-4">
                <h4 className="text-xs font-semibold text-blue-700 mb-2">AI 요약</h4>
                <p className="text-sm text-gray-700 leading-relaxed">{selectedArticle.summary}</p>
              </div>
            )}

            {/* 관련도 */}
            {selectedArticle.relevanceScore !== null && (
              <div className="mb-4">
                <div className="flex justify-between text-xs text-gray-500 mb-1">
                  <span>관련도 점수</span>
                  <span className="font-semibold">{selectedArticle.relevanceScore}%</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full">
                  <div
                    className="h-2 bg-blue-500 rounded-full"
                    style={{ width: `${selectedArticle.relevanceScore}%` }}
                  />
                </div>
              </div>
            )}

            {/* 원문 내용 (없으면 스킵) */}
            {selectedArticle.summary === null && (
              <div className="text-xs text-gray-400 italic">AI 요약이 없습니다.</div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
