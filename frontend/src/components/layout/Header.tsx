import { useLocation } from 'react-router-dom'
import { RefreshCw } from 'lucide-react'
import { useTriggerCrawl } from '../../hooks/useArticles'

const pageTitles: Record<string, { title: string; description: string }> = {
  '/': { title: '대시보드', description: '경쟁사 동향 현황 요약' },
  '/articles': { title: '기사 수집', description: '수집된 뉴스·블로그 기사 목록' },
  '/insights': { title: 'AI 인사이트', description: 'Claude가 분석한 전략 인사이트' },
  '/competitors': { title: '경쟁사 분석', description: '경쟁사별 상세 동향 분석' },
  '/sources': { title: '소스 관리', description: '크롤링 소스 설정 및 관리' },
}

export default function Header() {
  const { pathname } = useLocation()
  const { title, description } = pageTitles[pathname] ?? { title: '', description: '' }
  const { mutate: triggerCrawl, isPending } = useTriggerCrawl()

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
      <div>
        <h1 className="text-xl font-bold text-gray-900">{title}</h1>
        <p className="text-sm text-gray-500 mt-0.5">{description}</p>
      </div>
      <div className="flex items-center gap-3">
        <button
          onClick={() => triggerCrawl()}
          disabled={isPending}
          className="flex items-center gap-2 btn-secondary"
        >
          <RefreshCw size={16} className={isPending ? 'animate-spin' : ''} />
          {isPending ? '수집 중...' : '지금 수집'}
        </button>
        <div className="text-xs text-gray-400 border border-gray-200 rounded px-2 py-1">
          {new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })}
        </div>
      </div>
    </header>
  )
}
