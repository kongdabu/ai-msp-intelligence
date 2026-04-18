import { useState } from 'react'
import { useArticles } from '../hooks/useArticles'
import { useFilterStore } from '../store/filterStore'
import ArticleFilter from '../components/article/ArticleFilter'
import ArticleList from '../components/article/ArticleList'
import { Article, COMPETITOR_LABELS, CATEGORY_LABELS, COMPETITOR_COLORS } from '../types'
import { X, ExternalLink } from 'lucide-react'

function ArticleDetail({ article, onClose }: { article: Article; onClose: () => void }) {
  return (
    <div className="p-5">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-2 flex-wrap">
          <span
            className="badge text-white"
            style={{ backgroundColor: COMPETITOR_COLORS[article.competitor] ?? '#6b7280' }}
          >
            {COMPETITOR_LABELS[article.competitor]}
          </span>
          {article.category && (
            <span className="badge bg-gray-100 text-gray-700">
              {CATEGORY_LABELS[article.category]}
            </span>
          )}
        </div>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 ml-2 shrink-0">
          <X size={20} />
        </button>
      </div>

      <h2 className="text-base font-bold text-gray-900 mb-1">{article.title}</h2>
      <p className="text-xs text-gray-400 mb-4">
        {article.sourceName} ·{' '}
        {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString('ko-KR') : ''}
      </p>

      <a
        href={article.url}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center gap-2 btn-primary w-full justify-center mb-4"
      >
        <ExternalLink size={14} />
        원문 보기
      </a>

      {article.summary && (
        <div className="bg-blue-50 rounded-lg p-4 mb-4">
          <h4 className="text-xs font-semibold text-blue-700 mb-2">AI 요약</h4>
          <p className="text-sm text-gray-700 leading-relaxed">{article.summary}</p>
        </div>
      )}

      {article.relevanceScore !== null && (
        <div className="mb-4">
          <div className="flex justify-between text-xs text-gray-500 mb-1">
            <span>관련도 점수</span>
            <span className="font-semibold">{article.relevanceScore}%</span>
          </div>
          <div className="h-2 bg-gray-100 rounded-full">
            <div
              className="h-2 bg-blue-500 rounded-full"
              style={{ width: `${article.relevanceScore}%` }}
            />
          </div>
        </div>
      )}

      {article.summary === null && (
        <div className="text-xs text-gray-400 italic">AI 요약이 없습니다.</div>
      )}
    </div>
  )
}

export default function Articles() {
  const [page, setPage] = useState(0)
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null)
  const { articleFilter } = useFilterStore()

  const { data, isLoading } = useArticles({ ...articleFilter, page, size: 18 })

  return (
    <div className="p-4 sm:p-6 space-y-4">
      <ArticleFilter />

      {data && (
        <div className="text-sm text-gray-500">
          총 <span className="font-semibold text-gray-900">{data.totalElements.toLocaleString()}</span>건
        </div>
      )}

      <div className={`transition-all ${selectedArticle ? 'md:mr-96' : ''}`}>
        <ArticleList
          articles={data?.content ?? []}
          totalPages={data?.totalPages ?? 0}
          currentPage={page}
          onPageChange={(p) => setPage(p)}
          onArticleClick={setSelectedArticle}
          isLoading={isLoading}
        />
      </div>

      {/* 모바일: 전체 화면 오버레이 */}
      {selectedArticle && (
        <div className="md:hidden fixed inset-0 bg-white z-40 overflow-y-auto">
          <ArticleDetail article={selectedArticle} onClose={() => setSelectedArticle(null)} />
        </div>
      )}

      {/* 데스크탑: 우측 슬라이드 패널 */}
      {selectedArticle && (
        <div className="hidden md:block fixed right-0 top-0 h-full w-96 bg-white border-l border-gray-200 shadow-xl z-40 overflow-y-auto">
          <ArticleDetail article={selectedArticle} onClose={() => setSelectedArticle(null)} />
        </div>
      )}
    </div>
  )
}
