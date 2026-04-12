import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Newspaper, Lightbulb, Users, Database } from 'lucide-react'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: '대시보드' },
  { to: '/articles', icon: Newspaper, label: '기사 수집' },
  { to: '/insights', icon: Lightbulb, label: '인사이트' },
  { to: '/competitors', icon: Users, label: '경쟁사 분석' },
  { to: '/sources', icon: Database, label: '소스 관리' },
]

export default function Sidebar() {
  return (
    <aside className="w-60 bg-gray-900 text-white flex flex-col min-h-screen">
      {/* 로고 */}
      <div className="px-6 py-5 border-b border-gray-700">
        <div className="text-sm text-blue-400 font-semibold tracking-wide">AI MSP</div>
        <div className="text-white font-bold text-lg leading-tight">Intelligence</div>
        <div className="text-xs text-gray-400 mt-0.5">경쟁사 동향 모니터링</div>
      </div>

      {/* 네비게이션 */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* 하단 정보 */}
      <div className="px-6 py-4 border-t border-gray-700 text-xs text-gray-500">
        <div>AI MSP Intelligence v0.1</div>
        <div className="mt-1">Powered by Claude API</div>
      </div>
    </aside>
  )
}
