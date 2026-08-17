import {
  Activity,
  ChevronLeft,
  ChevronRight,
  CircleAlert,
  ExternalLink,
  Radar as RadarIcon,
  RefreshCw,
  Square,
  Users,
  Building2,
  Sparkles,
  TrendingUp,
  ShieldAlert,
  CheckCircle2,
  Calendar,
  Search,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import {
  useCancelRadarCollection,
  useRadarCollectionStatus,
  useRadarOverview,
  useRadarSignals,
  useStartRadarCollection,
} from '../hooks/useRadar'
import { RadarLensCode, RadarSignal } from '../types'
import { useAuthStore } from '../store/authStore'
import AdminAuthModal from '../components/common/AdminAuthModal'

function RadarSkeleton() {
  return (
    <div className="mx-auto max-w-7xl space-y-4 p-4 sm:p-6 lg:p-8 animate-pulse">
      <div className="h-16 rounded-2xl bg-slate-200" />
      <div className="grid gap-4 lg:grid-cols-12 h-[calc(100vh-180px)]">
        <div className="lg:col-span-3 rounded-2xl bg-slate-200" />
        <div className="lg:col-span-4 rounded-2xl bg-slate-200" />
        <div className="lg:col-span-5 rounded-2xl bg-slate-200" />
      </div>
    </div>
  )
}

function signalLabel(signal: RadarSignal) {
  return signal.players.length > 0 ? signal.players.join(' · ') : signal.signalType
}

export default function Radar() {
  const { data: overview, isLoading, isError, refetch } = useRadarOverview()
  const { data: collectionStatus } = useRadarCollectionStatus()
  const { mutate: startCollection, isPending: isStartingCollection } = useStartRadarCollection()
  const { mutate: cancelCollection, isPending: isCancellingCollection } = useCancelRadarCollection()
  const [searchParams, setSearchParams] = useSearchParams()
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const { isAdmin } = useAuthStore()

  const selectedLensParam = searchParams.get('lens')
  const selectedLens = overview?.lenses.some((lens) => lens.code === selectedLensParam)
    ? (selectedLensParam as RadarLensCode)
    : null
  const highImpactOnly = searchParams.get('impact') === 'high'
  const page = Math.max(Number(searchParams.get('page') ?? '0'), 0)
  const { data: signalPage, isLoading: isSignalsLoading } = useRadarSignals({
    lens: selectedLens,
    minimumImpactScore: highImpactOnly ? 80 : null,
    page,
    size: 30,
  })

  const [selectedSignalId, setSelectedSignalId] = useState<number | null>(null)

  const filteredSignals = useMemo(() => {
    const signals = signalPage?.content ?? []
    if (!searchTerm.trim()) return signals
    const term = searchTerm.toLowerCase()
    return signals.filter(
      (s) =>
        s.title.toLowerCase().includes(term) ||
        s.fact.toLowerCase().includes(term) ||
        s.players.some((p) => p.toLowerCase().includes(term))
    )
  }, [searchTerm, signalPage?.content])

  const selectedSignal = useMemo(() => {
    return filteredSignals.find((signal) => signal.id === selectedSignalId) ?? filteredSignals[0] ?? null
  }, [selectedSignalId, filteredSignals])

  useEffect(() => {
    if (collectionStatus?.status === 'COMPLETED') void refetch()
  }, [collectionStatus?.completedAt, collectionStatus?.status, refetch])

  if (isLoading) return <RadarSkeleton />
  if (isError || !overview)
    return (
      <div className="p-6 text-sm text-slate-600">
        Radar 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.
      </div>
    )

  const selectedLensInfo = overview.lenses.find((lens) => lens.code === selectedLens)
  const isCollecting =
    isStartingCollection || collectionStatus?.status === 'RUNNING' || collectionStatus?.status === 'CANCELLING'

  const updateFilters = (updates: Record<string, string | null>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => (value ? next.set(key, value) : next.delete(key)))
    next.delete('page')
    setSearchParams(next)
    setSelectedSignalId(null)
  }

  const movePage = (nextPage: number) => {
    const next = new URLSearchParams(searchParams)
    next.set('page', String(nextPage))
    setSearchParams(next)
    setSelectedSignalId(null)
  }

  const handleStartCollection = () => {
    if (!isAdmin) {
      setIsAuthModalOpen(true)
      return
    }
    startCollection()
  }

  const handleCancelCollection = () => {
    if (!isAdmin) {
      setIsAuthModalOpen(true)
      return
    }
    cancelCollection()
  }

  return (
    <div className="mx-auto max-w-7xl p-3 sm:p-5 lg:p-6 space-y-3">
      {/* 1. 상단 슬림 컨트롤 바 */}
      <section className="rounded-2xl bg-slate-950 px-5 py-3 text-white shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <div className="rounded-xl bg-blue-600/30 border border-blue-500/30 p-2 text-blue-400 shrink-0">
            <RadarIcon size={18} />
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-white tracking-tight">AI Services Industry Radar</h1>
              <span className="inline-flex items-center gap-1 rounded-full bg-blue-900/60 border border-blue-700/50 px-2 py-0.5 text-[11px] font-semibold text-blue-300">
                Live Feed
              </span>
            </div>
            <p className="text-xs text-slate-400 truncate">
              35개 Watch List 사업자 대상 6대 산업 재편 관점 검증 신호
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          {isCollecting ? (
            <button
              type="button"
              onClick={handleCancelCollection}
              disabled={isCancellingCollection || collectionStatus?.status === 'CANCELLING'}
              className="inline-flex items-center gap-1.5 rounded-xl bg-rose-600 px-3.5 py-1.5 text-xs font-bold text-white transition hover:bg-rose-500 disabled:opacity-50 cursor-pointer"
            >
              <Square size={13} />
              {collectionStatus?.status === 'CANCELLING' ? '취소 요청 중...' : '작업 취소'}
            </button>
          ) : (
            <button
              type="button"
              onClick={handleStartCollection}
              disabled={isStartingCollection}
              className={`inline-flex items-center gap-1.5 rounded-xl px-3.5 py-1.5 text-xs font-bold transition cursor-pointer ${
                isAdmin
                  ? 'bg-blue-600 text-white hover:bg-blue-500 shadow-sm'
                  : 'bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700'
              }`}
              title={isAdmin ? 'Radar 분석 실행' : '관리자 인증 필요'}
            >
              <RefreshCw size={13} className={isStartingCollection ? 'animate-spin' : ''} />
              {isAdmin ? 'Radar 분석 실행' : '🔒 Radar 분석 (관리자)'}
            </button>
          )}
        </div>
      </section>

      {/* 2. 메인 3-Pane 독립 스크롤 컨테이너 (고정 뷰포트 높이) */}
      <div className="grid gap-3 lg:grid-cols-12 h-[calc(100vh-140px)] min-h-[600px] overflow-hidden">
        {/* Pane 1. 좌측 관점 필터 네비게이션 (lg:col-span-3) */}
        <aside className="lg:col-span-3 h-full overflow-y-auto space-y-2 pr-1 custom-scrollbar">
          <div className="rounded-2xl border border-slate-200 bg-white p-3 shadow-xs space-y-1">
            <div className="px-2 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-400">
              필터
            </div>
            <button
              type="button"
              onClick={() => updateFilters({ lens: null, impact: null })}
              className={`flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs font-semibold transition-all cursor-pointer ${
                !selectedLens && !highImpactOnly
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              <span className="flex items-center gap-2">
                <Activity size={14} /> 전체 Signal
              </span>
              <span className="text-[11px] opacity-80">{overview.signalCount}</span>
            </button>
            <button
              type="button"
              onClick={() => updateFilters({ lens: null, impact: highImpactOnly ? null : 'high' })}
              className={`flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs font-semibold transition-all cursor-pointer ${
                highImpactOnly
                  ? 'bg-rose-600 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-rose-50 hover:text-rose-700'
              }`}
            >
              <span className="flex items-center gap-2">
                <CircleAlert size={14} /> 고영향 Signal
              </span>
              <span className="text-[11px] opacity-80">{overview.highImpactSignalCount}</span>
            </button>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-3 shadow-xs space-y-1">
            <div className="px-2 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-400">
              산업 재편 6대 관점
            </div>
            {overview.lenses.map((lens) => {
              const isSelected = selectedLens === lens.code
              return (
                <button
                  key={lens.code}
                  type="button"
                  onClick={() =>
                    updateFilters({ lens: selectedLens === lens.code ? null : lens.code, impact: null })
                  }
                  className={`w-full rounded-xl px-3 py-2 text-left transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-blue-600 text-white shadow-xs'
                      : 'text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <div className="flex items-center justify-between gap-1 text-xs font-semibold">
                    <span>{lens.label}</span>
                    <span
                      className={`rounded-md px-1.5 py-0.2 text-[10px] font-bold ${
                        isSelected ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {lens.signalCount}
                    </span>
                  </div>
                  <span
                    className={`mt-0.5 block text-[11px] line-clamp-1 ${
                      isSelected ? 'text-blue-100' : 'text-slate-400'
                    }`}
                  >
                    {lens.description}
                  </span>
                </button>
              )
            })}
          </div>

          <Link
            to="/settings/watch-list"
            className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-3 text-xs font-semibold text-slate-700 shadow-xs hover:bg-blue-50 hover:text-blue-700 transition-colors"
          >
            <span className="flex items-center gap-2">
              <Users size={14} className="text-blue-600" /> Watch List 관리
            </span>
            <span className="text-[11px] text-slate-400 font-medium">{overview.playerCount}개 기업</span>
          </Link>
        </aside>

        {/* Pane 2. 가운데 신호 목록 피드 (lg:col-span-4 / xl:col-span-4.5) - 독립 스크롤 */}
        <section className="lg:col-span-4 xl:col-span-4.5 h-full flex flex-col min-w-0 rounded-2xl border border-slate-200 bg-white shadow-xs overflow-hidden">
          {/* 피드 헤더 */}
          <div className="p-3.5 border-b border-slate-100 bg-slate-50/50 space-y-2 shrink-0">
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-1.5 min-w-0">
                <span className="font-bold text-xs text-slate-900 truncate">
                  {selectedLensInfo
                    ? `${selectedLensInfo.label}`
                    : highImpactOnly
                    ? '고영향 Signal'
                    : '전체 신호'}
                </span>
                <span className="rounded-md bg-blue-100 text-blue-800 text-[10px] font-extrabold px-1.5 py-0.5 shrink-0">
                  {filteredSignals.length}건
                </span>
              </div>

              {/* 페이지 이동 버튼 */}
              {signalPage && signalPage.totalPages > 1 && (
                <div className="flex items-center gap-1 text-xs text-slate-500 shrink-0">
                  <button
                    type="button"
                    disabled={signalPage.number === 0}
                    onClick={() => movePage(signalPage.number - 1)}
                    className="p-1 rounded hover:bg-slate-200 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronLeft size={14} />
                  </button>
                  <span className="text-[11px] font-medium">
                    {signalPage.number + 1}/{signalPage.totalPages}
                  </span>
                  <button
                    type="button"
                    disabled={signalPage.number + 1 >= signalPage.totalPages}
                    onClick={() => movePage(signalPage.number + 1)}
                    className="p-1 rounded hover:bg-slate-200 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronRight size={14} />
                  </button>
                </div>
              )}
            </div>

            {/* 검색창 */}
            <div className="relative">
              <Search size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="기업명, 키워드 검색..."
                className="w-full rounded-xl border border-slate-200 bg-white pl-8 pr-3 py-1.5 text-xs text-slate-800 placeholder:text-slate-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-100"
              />
            </div>
          </div>

          {/* 피드 목록 영역 (독립 스크롤) */}
          <div className="flex-1 overflow-y-auto p-2.5 space-y-2 custom-scrollbar bg-slate-50/30">
            {isSignalsLoading ? (
              <div className="p-8 text-center text-xs text-slate-400">Signal을 불러오는 중...</div>
            ) : filteredSignals.length === 0 ? (
              <div className="p-8 text-center text-xs text-slate-400 border border-dashed border-slate-200 rounded-xl">
                일치하는 Signal이 없습니다.
              </div>
            ) : (
              filteredSignals.map((signal) => {
                const isSelected = selectedSignal?.id === signal.id
                return (
                  <button
                    key={signal.id}
                    type="button"
                    onClick={() => setSelectedSignalId(signal.id)}
                    className={`w-full rounded-xl border p-3 text-left transition-all cursor-pointer ${
                      isSelected
                        ? 'border-blue-600 bg-blue-50/70 shadow-xs ring-1 ring-blue-600'
                        : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2 mb-1">
                      <p className="text-[11px] font-bold text-blue-700 truncate">
                        {signalLabel(signal)}
                      </p>
                      <span
                        className={`shrink-0 rounded-md px-1.5 py-0.5 text-[10px] font-extrabold ${
                          signal.impactScore >= 80
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        영향 {signal.impactScore}
                      </span>
                    </div>

                    <h3
                      className={`text-xs font-bold line-clamp-2 leading-snug ${
                        isSelected ? 'text-blue-950' : 'text-slate-900'
                      }`}
                    >
                      {signal.title}
                    </h3>

                    <p className="mt-1 line-clamp-2 text-[11px] leading-relaxed text-slate-500">
                      {signal.fact}
                    </p>

                    <div className="mt-2 flex flex-wrap gap-1">
                      {signal.lenses.map((lens) => (
                        <span
                          key={lens}
                          className="rounded bg-slate-100 px-1.5 py-0.2 text-[10px] font-medium text-slate-600"
                        >
                          {lens}
                        </span>
                      ))}
                    </div>
                  </button>
                )
              })
            )}
          </div>
        </section>

        {/* Pane 3. 우측 신호 상세 분석 뷰어 (lg:col-span-5 / xl:col-span-4.5) - 독립 스크롤 */}
        <section className="lg:col-span-5 xl:col-span-4.5 h-full overflow-y-auto rounded-2xl border border-slate-200 bg-white p-5 shadow-xs custom-scrollbar">
          {selectedSignal ? (
            <article className="space-y-4">
              {/* 상단 메타 */}
              <div className="border-b border-slate-100 pb-3 space-y-2">
                <div className="flex flex-wrap items-center gap-1.5">
                  {selectedSignal.lenses.map((lens) => (
                    <span
                      key={lens}
                      className="rounded-md bg-blue-50 border border-blue-100 px-2 py-0.5 text-[11px] font-bold text-blue-700"
                    >
                      {lens}
                    </span>
                  ))}
                  <span className="rounded-md bg-rose-50 border border-rose-100 px-2 py-0.5 text-[11px] font-bold text-rose-700">
                    영향도 {selectedSignal.impactScore}점
                  </span>
                  <span className="text-[11px] text-slate-400 ml-auto flex items-center gap-1">
                    <Calendar size={12} />
                    {new Date(selectedSignal.occurredAt).toLocaleDateString('ko-KR')}
                  </span>
                </div>

                <h2 className="text-base sm:text-lg font-bold text-slate-950 leading-snug">
                  {selectedSignal.title}
                </h2>

                <div className="rounded-xl bg-slate-50 p-3 border border-slate-100 text-xs text-slate-700 leading-relaxed">
                  <span className="font-bold text-slate-900 block mb-0.5">📌 사실 요약 (Fact)</span>
                  {selectedSignal.fact}
                </div>
              </div>

              {/* 구조 분석 세부 섹션들 */}
              <div className="space-y-3">
                <DetailCard
                  icon={<TrendingUp size={15} className="text-blue-600" />}
                  title="무엇이 바뀌었나"
                  content={selectedSignal.assessment?.whatChanged}
                />
                <DetailCard
                  icon={<Building2 size={15} className="text-indigo-600" />}
                  title="산업 구조 영향"
                  content={selectedSignal.assessment?.industryStructureImpact}
                />
                <DetailCard
                  icon={<Sparkles size={15} className="text-emerald-600" />}
                  title="MSP 기회"
                  content={selectedSignal.assessment?.mspOpportunity}
                />
                <DetailCard
                  icon={<ShieldAlert size={15} className="text-rose-600" />}
                  title="MSP 위협 및 구조적 위험"
                  content={[
                    selectedSignal.assessment?.mspThreat,
                    selectedSignal.assessment?.structuralRisk,
                  ]
                    .filter(Boolean)
                    .join('\n')}
                />
                <DetailCard
                  icon={<CheckCircle2 size={15} className="text-amber-600" />}
                  title="권고 행동"
                  content={selectedSignal.assessment?.recommendedAction}
                  highlight
                />
              </div>

              {/* 원문 링크 */}
              <div className="pt-2">
                <a
                  href={selectedSignal.sourceUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-xs font-bold text-white hover:bg-slate-800 transition-colors shadow-xs"
                >
                  원문 기사 출처 열기 <ExternalLink size={13} />
                </a>
              </div>
            </article>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-center p-8 text-slate-400">
              <RadarIcon size={36} className="text-slate-300 mb-2" />
              <p className="text-xs font-medium">좌측 피드에서 신호를 선택하면 상세 분석을 확인하실 수 있습니다.</p>
            </div>
          )}
        </section>
      </div>

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />
    </div>
  )
}

function DetailCard({
  icon,
  title,
  content,
  highlight = false,
}: {
  icon: React.ReactNode
  title: string
  content?: string | null
  highlight?: boolean
}) {
  if (!content) return null
  return (
    <div
      className={`rounded-xl border p-3 text-xs leading-relaxed transition-all ${
        highlight
          ? 'border-amber-200 bg-amber-50/60 text-amber-950 font-medium'
          : 'border-slate-100 bg-slate-50/60 text-slate-700'
      }`}
    >
      <div className="flex items-center gap-1.5 font-bold text-slate-900 mb-1">
        {icon}
        <span>{title}</span>
      </div>
      <p className="whitespace-pre-line text-[11px] leading-relaxed">{content}</p>
    </div>
  )
}
