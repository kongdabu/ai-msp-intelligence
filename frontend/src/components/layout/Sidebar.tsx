import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  Newspaper,
  Lightbulb,
  Bookmark,
  BookmarkCheck,
  Settings,
  X,
  Flame,
  Radar,
  Compass,
  SlidersHorizontal,
  Database,
} from 'lucide-react'

interface NavItem {
  to: string
  icon: React.ElementType
  label: string
  end?: boolean
}

interface NavSection {
  title: string
  items: NavItem[]
}

const navSections: NavSection[] = [
  {
    title: 'AI 인텔리전스',
    items: [
      { to: '/', icon: Radar, label: '인더스트리 레이더', end: true },
      { to: '/strategy-reports', icon: Compass, label: '데일리 브리핑' },
      { to: '/stored-radar-reports', icon: Database, label: '저장된 Radar' },
      { to: '/insights', icon: Lightbulb, label: '전략 인사이트' },
      { to: '/trends', icon: Flame, label: '트렌드 분석' },
      { to: '/legacy/dashboard', icon: LayoutDashboard, label: '종합 대시보드' },
    ],
  },
  {
    title: '데이터 & 보관함',
    items: [
      { to: '/articles', icon: Newspaper, label: '수집 기사' },
      { to: '/saved-articles', icon: BookmarkCheck, label: '저장한 기사' },
      { to: '/saved', icon: Bookmark, label: '저장한 인사이트' },
    ],
  },
  {
    title: '관리',
    items: [
      { to: '/settings/watch-list', icon: SlidersHorizontal, label: 'Watch List 설정' },
      { to: '/settings', icon: Settings, label: '시스템 설정' },
    ],
  },
]

interface SidebarProps {
  open: boolean
  onClose: () => void
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  return (
    <aside
      className={`
      fixed md:static inset-y-0 left-0 z-30
      w-60 bg-gray-900 text-white flex flex-col min-h-screen shrink-0
      transition-transform duration-200
      ${open ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
    `}
    >
      {/* 로고 영역 */}
      <div className="px-5 py-5 border-b border-gray-800 flex items-start justify-between">
        <div>
          <div className="text-xs text-blue-400 font-bold tracking-wider uppercase">AI MSP</div>
          <div className="text-white font-extrabold text-lg leading-tight mt-0.5">Intelligence</div>
          <div className="text-[11px] text-gray-400 mt-1 font-medium">산업 재편 & 전략 인텔리전스</div>
        </div>
        <button
          className="md:hidden text-gray-400 hover:text-white mt-1 p-1 rounded-md hover:bg-gray-800 transition-colors"
          onClick={onClose}
          aria-label="메뉴 닫기"
        >
          <X size={18} />
        </button>
      </div>

      {/* 섹션별 네비게이션 */}
      <nav className="flex-1 px-3 py-3 space-y-4 overflow-y-auto">
        {navSections.map((section) => (
          <div key={section.title} className="space-y-1">
            <div className="px-3 py-1 text-[11px] font-bold tracking-wider text-gray-400 uppercase">
              {section.title}
            </div>
            {section.items.map(({ to, icon: Icon, label, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-sm font-semibold'
                      : 'text-gray-300 hover:bg-gray-800/80 hover:text-white'
                  }`
                }
              >
                <Icon size={17} className="shrink-0" />
                <span>{label}</span>
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      {/* 하단 정보 */}
      <div className="px-5 py-4 border-t border-gray-800 text-[11px] text-gray-400">
        <div className="font-semibold text-gray-400">AI MSP Intelligence v0.1</div>
        <div className="mt-0.5 text-gray-400">Powered by Google Gemini</div>
      </div>
    </aside>
  )
}
