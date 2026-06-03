import { useState } from 'react'
import { useBookmarkedArticles } from '../hooks/useArticles'
import ArticleList from '../components/article/ArticleList'
import { ArticleDetail } from './Articles'
import { Article } from '../types'
import { Bookmark } from 'lucide-react'

export default function SavedArticles() {
  const [page, setPage] = useState(0)
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null)

  const { data, isLoading } = useBookmarkedArticles({ page, size: 18 })

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center gap-2">
        <Bookmark className="text-blue-500" size={20} />
        <h1 className="text-lg font-bold text-gray-900">저장한 기사</h1>
      </div>
      <p className="text-sm text-gray-500">
        나중에 다시 확인하려고 저장(북마크)한 기사 목록입니다.
        {data && (
          <>
            {' '}총 <span className="font-semibold text-gray-900">{data.totalElements.toLocaleString()}</span>건
          </>
        )}
      </p>

      {!isLoading && data?.content.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <Bookmark size={36} className="mx-auto mb-3 text-gray-300" />
          <p className="text-lg font-medium">저장한 기사가 없습니다</p>
          <p className="text-sm mt-1">기사 카드의 북마크 아이콘을 눌러 저장해보세요.</p>
        </div>
      ) : (
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
      )}

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
