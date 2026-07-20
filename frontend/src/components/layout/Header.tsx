import { useLocation } from 'react-router-dom'
import { Menu } from 'lucide-react'

const pageTitles: Record<string, { title: string; description: string }> = {
  '/': { title: '전략 브리핑', description: '인사이트 중심 경쟁사 동향 요약' },
  '/articles': { title: '기사 수집', description: '수집된 뉴스·블로그 기사 목록' },
  '/insights': { title: 'AI 인사이트', description: 'Gemini가 분석한 전략 인사이트' },
  '/competitors': { title: '경쟁사 분석', description: '경쟁사별 상세 동향 분석' },
  '/trends': { title: 'Trend News', description: '최근 30일 기사에서 식별한 Hot Trend' },
  '/sources': { title: '소스 관리', description: '크롤링 소스 설정 및 관리' },
}

interface HeaderProps {
  onMenuClick: () => void
}

export default function Header({ onMenuClick }: HeaderProps) {
  const { pathname } = useLocation()
  const { title, description } = pageTitles[pathname] ?? { title: '', description: '' }
  return (
    <header className="bg-white border-b border-gray-200 px-4 sm:px-6 py-3 sm:py-4 flex items-center justify-between gap-3">
      <div className="flex items-center gap-3 min-w-0">
        <button
          className="md:hidden shrink-0 text-gray-500 hover:text-gray-700"
          onClick={onMenuClick}
          aria-label="메뉴 열기"
        >
          <Menu size={22} />
        </button>
        <div className="min-w-0">
          <h1 className="text-lg sm:text-xl font-bold text-gray-900 truncate">{title}</h1>
          <p className="text-xs sm:text-sm text-gray-500 hidden sm:block">{description}</p>
        </div>
      </div>
      <div className="text-xs text-gray-400 border border-gray-200 rounded px-2 py-1 hidden sm:block">
        {new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })}
      </div>
    </header>
  )
}
