import { useState } from 'react'
import { useLocation } from 'react-router-dom'
import { Menu, Lock, Unlock } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import AdminAuthModal from '../common/AdminAuthModal'

const pageTitles: Record<string, { title: string; description: string }> = {
  '/': { title: '인더스트리 레이더', description: '35개 주요 사업자 동향 및 6대 산업 재편 관점 검증 신호' },
  '/radar': { title: '인더스트리 레이더', description: '35개 주요 사업자 동향 및 6대 산업 재편 관점 검증 신호' },
  '/strategy-reports': { title: '데일리 브리핑', description: '일간 산업 신호 종합, 밸류체인 재편 및 Top 3 Action 리포트' },
  '/legacy/dashboard': { title: '종합 대시보드', description: 'AI 생태계 통계 및 사업모델 동향 요약' },
  '/articles': { title: '수집 기사', description: '수집된 뉴스 및 공식 기사 목록' },
  '/saved-articles': { title: '저장한 기사', description: '북마크한 기사 및 리마인드 메모' },
  '/insights': { title: '전략 인사이트', description: 'Gemini AI 기반 기회·위협·전략 인사이트' },
  '/saved': { title: '저장한 인사이트', description: '보관된 전략 인사이트' },
  '/trends': { title: '트렌드 분석', description: '최근 30일 기사에서 식별한 핵심 시장 흐름' },
  '/settings/watch-list': { title: 'Watch List 설정', description: 'Radar 감시 대상 기업 및 우선순위 관리' },
  '/settings': { title: '시스템 설정', description: '수집 및 AI 분석 파라미터 환경 설정' },
}

interface HeaderProps {
  onMenuClick: () => void
}

export default function Header({ onMenuClick }: HeaderProps) {
  const { pathname } = useLocation()
  const { title, description } = pageTitles[pathname] ?? { title: '', description: '' }
  const { isAdmin } = useAuthStore()
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)

  return (
    <>
      <header className="bg-white border-b border-gray-200 px-4 sm:px-6 py-3 sm:py-4 flex items-center justify-between gap-3 sticky top-0 z-20 shadow-xs">
        <div className="flex items-center gap-3 min-w-0">
          <button
            className="md:hidden shrink-0 text-gray-500 hover:text-gray-700 p-1"
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

        <div className="flex items-center gap-2.5 shrink-0">
          {/* 관리자 모드 토글 버튼 */}
          <button
            type="button"
            onClick={() => setIsAuthModalOpen(true)}
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer border ${
              isAdmin
                ? 'bg-emerald-50 border-emerald-200 text-emerald-700 hover:bg-emerald-100'
                : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100 hover:text-slate-900'
            }`}
            title={isAdmin ? '관리자 권한 활성화됨 (클릭하여 해제)' : '관리자 권한 잠금 해제'}
          >
            {isAdmin ? (
              <>
                <Unlock size={13} className="text-emerald-600" />
                <span>관리자 모드</span>
              </>
            ) : (
              <>
                <Lock size={13} className="text-slate-400" />
                <span>관리자 인증</span>
              </>
            )}
          </button>

          {/* 날짜 배지 */}
          <div className="text-xs text-gray-500 border border-gray-200 bg-gray-50 rounded-lg px-2.5 py-1.5 hidden md:block font-medium">
            {new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })}
          </div>
        </div>
      </header>

      {/* 관리자 인증 모달 */}
      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />
    </>
  )
}
