import { useState, useEffect } from 'react'
import { useArticles, useTriggerCrawl, useToggleArticleBookmark } from '../hooks/useArticles'
import { useFilterStore } from '../store/filterStore'
import ArticleFilter from '../components/article/ArticleFilter'
import ArticleList from '../components/article/ArticleList'
import { Article, COMPETITOR_LABELS, CATEGORY_LABELS, COMPETITOR_COLORS } from '../types'
import { X, ExternalLink, RefreshCw, Bookmark } from 'lucide-react'

export function ArticleDetail({ article, onClose }: { article: Article; onClose: () => void }) {
  const { mutate: toggleBookmark, isPending } = useToggleArticleBookmark()
  const [note, setNote] = useState('')
  // 패널은 목록 스냅샷을 받으므로, 즉시 반영을 위해 북마크 상태를 로컬로 관리
  const [bookmarked, setBookmarked] = useState(article.bookmarked)
  const [bookmarkedAt, setBookmarkedAt] = useState<string | null | undefined>(article.bookmarkedAt)

  // 패널에 표시된 기사가 바뀌면 입력/표시 값 동기화
  useEffect(() => {
    setNote(article.bookmarkNote ?? '')
    setBookmarked(article.bookmarked)
    setBookmarkedAt(article.bookmarkedAt)
  }, [article.id, article.bookmarkNote, article.bookmarked, article.bookmarkedAt])

  // 저장 토글: 저장 시 현재 메모 함께 반영, 해제 시 메모 제거
  const handleBookmark = () => {
    if (isPending) return
    const next = !bookmarked
    setBookmarked(next) // 즉시 반영
    setBookmarkedAt(next ? new Date().toISOString() : null)
    toggleBookmark({
      id: article.id,
      bookmarked: next,
      note: next ? note.trim() || undefined : undefined,
    })
  }

  // 메모만 저장 (저장 상태로 전환하며 메모 갱신)
  const handleSaveNote = () => {
    if (isPending) return
    setBookmarked(true) // 즉시 반영
    setBookmarkedAt((prev) => prev ?? new Date().toISOString())
    toggleBookmark({ id: article.id, bookmarked: true, note: note.trim() || undefined })
  }

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
        <div className="flex items-center gap-2 ml-2 shrink-0">
          <button
            type="button"
            onClick={handleBookmark}
            disabled={isPending}
            aria-label={bookmarked ? '저장 해제' : '저장'}
            title={bookmarked ? '저장 해제' : '나중에 다시 보기'}
            className="text-gray-300 hover:text-blue-500 transition-colors disabled:opacity-40"
          >
            <Bookmark size={18} className={bookmarked ? 'fill-blue-500 text-blue-500' : ''} />
          </button>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X size={20} />
          </button>
        </div>
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
        <div className="text-xs text-gray-400 italic mb-4">AI 요약이 없습니다.</div>
      )}

      {/* 리마인드 메모 (저장한 기사 나중에 다시 보기용) */}
      <div className="pt-2 border-t border-gray-100">
        <div className="flex items-center justify-between mb-2">
          <h4 className="text-sm font-semibold text-gray-700 flex items-center gap-1.5">
            <Bookmark size={14} className="text-blue-500" />
            리마인드 메모
          </h4>
          {bookmarked && bookmarkedAt && (
            <span className="text-xs text-gray-400">
              {new Date(bookmarkedAt).toLocaleString('ko-KR', {
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}{' '}
              저장됨
            </span>
          )}
        </div>
        <textarea
          value={note}
          onChange={(e) => setNote(e.target.value)}
          maxLength={500}
          rows={2}
          placeholder="이 기사에 대한 의견이나 나중에 확인할 내용을 적어두세요. (선택)"
          className="w-full text-sm border border-gray-200 rounded-md px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-blue-200 focus:border-blue-400"
        />
        <div className="flex justify-end mt-2">
          <button
            onClick={handleSaveNote}
            disabled={isPending}
            className="btn-primary text-xs disabled:opacity-50"
          >
            {isPending ? '저장 중...' : bookmarked ? '메모 저장' : '저장하고 메모 남기기'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function Articles() {
  const [page, setPage] = useState(0)
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null)
  const [crawlMsg, setCrawlMsg] = useState<string | null>(null)
  const { articleFilter } = useFilterStore()

  useEffect(() => { setPage(0) }, [articleFilter])

  const { data, isLoading } = useArticles({ ...articleFilter, page, size: 18 })
  const { mutate: crawl, isPending: isCrawling } = useTriggerCrawl({
    onSuccess: (d) => setCrawlMsg(`신규 ${d.crawledCount}건 수집 완료`),
  })

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="text-sm text-gray-500">
          {data && <>총 <span className="font-semibold text-gray-900">{data.totalElements.toLocaleString()}</span>건</>}
        </div>
        <button
          onClick={() => crawl()}
          disabled={isCrawling}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <RefreshCw size={14} className={isCrawling ? 'animate-spin' : ''} />
          {isCrawling ? '수집 중...' : '지금 수집'}
        </button>
      </div>

      {crawlMsg && (
        <div className="bg-green-50 border border-green-200 text-green-800 text-sm rounded-lg px-4 py-3 flex justify-between">
          ✅ {crawlMsg}
          <button onClick={() => setCrawlMsg(null)} className="text-green-600">✕</button>
        </div>
      )}

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
