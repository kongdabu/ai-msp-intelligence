import { Article, COMPETITOR_LABELS, COMPETITOR_COLORS, CATEGORY_LABELS } from '../../types'
import { Bookmark } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { ko } from 'date-fns/locale'
import { useToggleArticleBookmark } from '../../hooks/useArticles'

interface Props {
  article: Article
  onClick: (article: Article) => void
}

export default function ArticleCard({ article, onClick }: Props) {
  const { mutate: toggleBookmark, isPending } = useToggleArticleBookmark()

  const timeAgo = article.publishedAt
    ? formatDistanceToNow(new Date(article.publishedAt), { addSuffix: true, locale: ko })
    : ''

  const competitorColor = COMPETITOR_COLORS[article.competitor] ?? '#6b7280'

  // 카드 클릭(상세 열기)과 분리하기 위해 이벤트 전파 차단
  const handleBookmark = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (isPending) return
    toggleBookmark({ id: article.id, bookmarked: !article.bookmarked, note: article.bookmarkNote ?? undefined })
  }

  return (
    <div
      className="card cursor-pointer hover:shadow-md transition-shadow"
      onClick={() => onClick(article)}
    >
      {/* 배지 영역 */}
      <div className="flex items-center gap-2 mb-2 flex-wrap">
        <span
          className="badge text-white text-xs"
          style={{ backgroundColor: competitorColor }}
        >
          {COMPETITOR_LABELS[article.competitor] ?? article.competitor}
        </span>
        {article.category && (
          <span className="badge bg-gray-100 text-gray-700">
            {CATEGORY_LABELS[article.category] ?? article.category}
          </span>
        )}
        <span className="badge bg-blue-50 text-blue-700">{article.sourceType}</span>
        <button
          type="button"
          onClick={handleBookmark}
          disabled={isPending}
          aria-label={article.bookmarked ? '저장 해제' : '저장'}
          title={article.bookmarked ? '저장 해제' : '나중에 다시 보기'}
          className="ml-auto text-gray-300 hover:text-blue-500 transition-colors disabled:opacity-40"
        >
          <Bookmark
            size={16}
            className={article.bookmarked ? 'fill-blue-500 text-blue-500' : ''}
          />
        </button>
      </div>

      {/* 제목 */}
      <h3 className="font-semibold text-gray-900 text-sm leading-snug mb-1 line-clamp-2">
        {article.title}
      </h3>

      {/* 요약 */}
      {article.summary && (
        <p className="text-xs text-gray-600 line-clamp-2 mb-3">{article.summary}</p>
      )}

      {/* 하단 정보 */}
      <div className="flex items-center justify-between text-xs text-gray-400 mt-auto">
        <span className="truncate max-w-[120px]">{article.sourceName}</span>
        <span>{timeAgo}</span>
      </div>

      {/* 관련도 점수 바 */}
      {article.relevanceScore !== null && article.relevanceScore !== undefined && (
        <div className="mt-2">
          <div className="flex items-center justify-between text-xs text-gray-400 mb-1">
            <span>관련도</span>
            <span>{article.relevanceScore}%</span>
          </div>
          <div className="h-1 bg-gray-100 rounded-full">
            <div
              className="h-1 bg-blue-500 rounded-full"
              style={{ width: `${article.relevanceScore}%` }}
            />
          </div>
        </div>
      )}

      {/* 리마인드 메모 (저장 시 작성) */}
      {article.bookmarkNote && (
        <div className="mt-2 pt-2 border-t border-gray-100">
          <p className="text-xs text-blue-600 bg-blue-50 rounded px-2 py-1 line-clamp-2">
            📌 {article.bookmarkNote}
          </p>
        </div>
      )}
    </div>
  )
}
