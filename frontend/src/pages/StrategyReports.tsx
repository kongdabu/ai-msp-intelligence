import { useState } from 'react'
import {
  Compass,
  Sparkles,
  RefreshCw,
  Calendar,
  Layers,
  CheckCircle2,
  AlertTriangle,
  TrendingUp,
  Boxes,
  Cpu,
  Coins,
  ShieldAlert,
  FileText,
  Clock,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'
import { useGenerateStrategyReport, useStrategyReports } from '../hooks/useStrategyReports'
import { useToastStore } from '../store/toastStore'
import { useAuthStore } from '../store/authStore'
import AdminAuthModal from '../components/common/AdminAuthModal'
import { StrategyReport } from '../types'

export default function StrategyReports() {
  const [page, setPage] = useState(0)
  const [selectedReportId, setSelectedReportId] = useState<number | null>(null)
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)
  const { data: reportsData, isLoading, isError, refetch } = useStrategyReports(page, 15)
  const { mutate: generateReport, isPending: isGenerating } = useGenerateStrategyReport()
  const { showToast } = useToastStore()
  const { isAdmin } = useAuthStore()

  const reports = reportsData?.content ?? []
  const selectedReport: StrategyReport | undefined = selectedReportId
    ? reports.find((r) => r.id === selectedReportId)
    : reports[0]

  const handleGenerate = () => {
    if (!isAdmin) {
      setIsAuthModalOpen(true)
      return
    }

    generateReport(undefined, {
      onSuccess: (newReport) => {
        showToast('신규 데일리 브리핑이 생성되었습니다.', 'success')
        setSelectedReportId(newReport.id)
      },
      onError: (err) => {
        showToast(`데일리 브리핑 생성 실패: ${err.message}`, 'error')
      },
    })
  }

  const formatPeriod = (start: string, end: string) => {
    return `${start.substring(0, 10)} ~ ${end.substring(0, 10)}`
  }

  return (
    <div className="mx-auto max-w-7xl p-3 sm:p-5 lg:p-6 space-y-3">
      {/* 1. 상단 슬림 컨트롤 바 */}
      <section className="rounded-2xl bg-slate-950 px-5 py-3 text-white shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <div className="rounded-xl bg-indigo-600/30 border border-indigo-500/30 p-2 text-indigo-400 shrink-0">
            <Compass size={18} />
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-white tracking-tight">AI 서비스 산업 데일리 브리핑</h1>
              <span className="inline-flex items-center gap-1 rounded-full bg-indigo-900/60 border border-indigo-700/50 px-2 py-0.5 text-[11px] font-semibold text-indigo-300">
                Daily Strategic Brief
              </span>
            </div>
            <p className="text-xs text-slate-400 truncate">
              일간 수집된 산업 신호 종합, 밸류체인 재편 및 국내 MSP 핵심 실행 과제 (Top 3 Action)
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={handleGenerate}
            disabled={isGenerating}
            className={`inline-flex items-center gap-1.5 rounded-xl px-3.5 py-1.5 text-xs font-bold transition shadow-xs cursor-pointer ${
              isAdmin
                ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white hover:from-indigo-700 hover:to-blue-700'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700'
            }`}
            title={isAdmin ? 'AI 데일리 브리핑 생성' : '관리자 인증 필요'}
          >
            {isGenerating ? (
              <>
                <RefreshCw size={13} className="animate-spin" />
                Gemini 데일리 브리핑 생성 중...
              </>
            ) : isAdmin ? (
              <>
                <Sparkles size={13} />
                신규 데일리 브리핑 생성
              </>
            ) : (
              <>
                <Sparkles size={13} className="text-slate-400" />
                🔒 데일리 브리핑 생성 (관리자)
              </>
            )}
          </button>
        </div>
      </section>

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />

      {/* 2. 본문 2-Pane 독립 스크롤 컨테이너 (고정 뷰포트 높이) */}
      {isLoading ? (
        <div className="flex flex-col items-center justify-center h-[calc(100vh-140px)] text-slate-400">
          <RefreshCw size={32} className="animate-spin mb-3 text-indigo-500" />
          <p className="text-sm font-medium">데일리 브리핑을 불러오는 중입니다...</p>
        </div>
      ) : isError ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center text-red-700">
          <AlertTriangle size={28} className="mx-auto mb-2" />
          <p className="font-semibold">데일리 브리핑을 불러오지 못했습니다.</p>
          <button
            onClick={() => refetch()}
            className="mt-3 text-sm underline hover:text-red-900 cursor-pointer"
          >
            다시 시도
          </button>
        </div>
      ) : reports.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center shadow-sm">
          <Compass size={48} className="mx-auto mb-3 text-indigo-400" />
          <h3 className="text-lg font-bold text-slate-800">생성된 데일리 브리핑이 없습니다</h3>
          <p className="mt-1 text-sm text-slate-500 max-w-md mx-auto">
            상단의 '신규 데일리 브리핑 생성' 버튼을 클릭하면 최근 검증된 Radar Signal 데이터를 종합하여 데일리 브리핑을 작성합니다.
          </p>
          <button
            onClick={handleGenerate}
            disabled={isGenerating}
            className="mt-5 inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors cursor-pointer"
          >
            <Sparkles size={16} />
            첫 번째 데일리 브리핑 생성하기
          </button>
        </div>
      ) : (
        <div className="grid gap-3 lg:grid-cols-12 h-[calc(100vh-140px)] min-h-[600px] overflow-hidden">
          {/* Pane 1. 좌측 발행 히스토리 목록 (lg:col-span-4 / xl:col-span-3.5) - 독립 스크롤 */}
          <aside className="lg:col-span-4 xl:col-span-3.5 h-full flex flex-col min-w-0 rounded-2xl border border-slate-200 bg-white shadow-xs overflow-hidden">
            {/* 목록 헤더 */}
            <div className="p-3.5 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between shrink-0">
              <div className="flex items-center gap-1.5 font-bold text-xs text-slate-800">
                <Clock size={14} className="text-slate-500" />
                <span>발행 히스토리</span>
                <span className="rounded-md bg-indigo-100 text-indigo-800 text-[10px] font-extrabold px-1.5 py-0.2">
                  {reportsData?.totalElements ?? reports.length}건
                </span>
              </div>

              {/* 페이지네이션 */}
              {(reportsData?.totalPages ?? 1) > 1 && (
                <div className="flex items-center gap-1 text-xs text-slate-500">
                  <button
                    disabled={page === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    className="p-1 rounded hover:bg-slate-200 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronLeft size={13} />
                  </button>
                  <span className="text-[11px] font-medium">
                    {page + 1}/{reportsData?.totalPages}
                  </span>
                  <button
                    disabled={page + 1 >= (reportsData?.totalPages ?? 1)}
                    onClick={() => setPage((p) => p + 1)}
                    className="p-1 rounded hover:bg-slate-200 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronRight size={13} />
                  </button>
                </div>
              )}
            </div>

            {/* 목록 리스트 본문 (독립 스크롤) */}
            <div className="flex-1 overflow-y-auto p-2.5 space-y-2 custom-scrollbar bg-slate-50/30">
              {reports.map((report) => {
                const isSelected = selectedReport?.id === report.id
                return (
                  <button
                    key={report.id}
                    type="button"
                    onClick={() => setSelectedReportId(report.id)}
                    className={`w-full rounded-xl border p-3 text-left transition-all cursor-pointer ${
                      isSelected
                        ? 'border-indigo-600 bg-indigo-50/70 shadow-xs ring-1 ring-indigo-600'
                        : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80'
                    }`}
                  >
                    <div className="flex items-center justify-between text-[11px] text-slate-500 mb-1">
                      <span className="flex items-center gap-1 font-semibold text-slate-700">
                        <Calendar size={12} className="text-slate-400" />
                        {formatPeriod(report.periodStart, report.periodEnd)}
                      </span>
                      <span className="rounded bg-slate-100 px-1.5 py-0.2 font-bold text-[10px] text-slate-600">
                        신호 {report.sourceSignalCount}건
                      </span>
                    </div>

                    <h3
                      className={`text-xs font-bold line-clamp-2 leading-snug ${
                        isSelected ? 'text-indigo-950' : 'text-slate-900'
                      }`}
                    >
                      {report.title}
                    </h3>

                    <p className="mt-1 line-clamp-2 text-[11px] leading-relaxed text-slate-500">
                      {report.executiveSummary}
                    </p>
                  </button>
                )
              })}
            </div>
          </aside>

          {/* Pane 2. 우측 데일리 브리핑 상세 뷰어 (lg:col-span-8 / xl:col-span-8.5) - 독립 스크롤 */}
          {selectedReport && (
            <main className="lg:col-span-8 xl:col-span-8.5 h-full overflow-y-auto rounded-2xl border border-slate-200 bg-white p-5 sm:p-6 shadow-xs custom-scrollbar space-y-5">
              {/* 상단 타이틀 & 메타 */}
              <div className="border-b border-slate-100 pb-4 space-y-2">
                <div className="flex flex-wrap items-center gap-1.5 text-xs">
                  <span className="inline-flex items-center gap-1 rounded-md bg-blue-50 border border-blue-100 px-2 py-0.5 font-bold text-blue-700 text-[11px]">
                    <Calendar size={12} />
                    기간: {formatPeriod(selectedReport.periodStart, selectedReport.periodEnd)}
                  </span>
                  <span className="inline-flex items-center gap-1 rounded-md bg-emerald-50 border border-emerald-100 px-2 py-0.5 font-bold text-emerald-700 text-[11px]">
                    <Layers size={12} />
                    근거 신호 {selectedReport.sourceSignalCount}건 종합
                  </span>
                  <span className="text-[11px] text-slate-400 ml-auto">
                    발행: {new Date(selectedReport.generatedAt).toLocaleString('ko-KR')}
                  </span>
                </div>
                <h2 className="text-lg sm:text-xl font-bold text-slate-950 leading-snug">
                  {selectedReport.title}
                </h2>
              </div>

              {/* 국내 MSP 관점 Top 3 Action 하이라이트 배너 */}
              <div className="rounded-2xl border border-amber-200 bg-gradient-to-br from-amber-50/90 via-orange-50/40 to-amber-100/30 p-4 sm:p-5 shadow-xs">
                <div className="flex items-center gap-2 text-amber-900 font-bold text-sm mb-2.5">
                  <div className="rounded-lg bg-amber-500 p-1 text-white">
                    <CheckCircle2 size={16} />
                  </div>
                  국내 MSP 관점 핵심 실행 과제 (Top 3 Action)
                </div>
                <div className="prose prose-sm prose-amber max-w-none text-slate-800 whitespace-pre-line leading-relaxed text-xs sm:text-sm font-medium bg-white/80 p-3.5 rounded-xl border border-amber-200/60 shadow-2xs">
                  {selectedReport.top3Actions}
                </div>
              </div>

              {/* 경영진 핵심 요약 (Executive Summary) */}
              <div className="rounded-2xl border border-slate-200 bg-white p-4 sm:p-5 shadow-xs space-y-2">
                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                  <FileText size={16} className="text-indigo-600" />
                  경영진 핵심 요약 (Executive Summary)
                </h3>
                <div className="text-xs sm:text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3.5 rounded-xl border border-slate-100">
                  {selectedReport.executiveSummary}
                </div>
              </div>

              {/* 밸류체인 재편 분석 (Value Chain Impact) */}
              <div className="rounded-2xl border border-slate-200 bg-white p-4 sm:p-5 shadow-xs space-y-2">
                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                  <TrendingUp size={16} className="text-blue-600" />
                  Consulting–SI–MSP–ITO 산업 밸류체인 재편 및 상호 침투
                </h3>
                <div className="text-xs sm:text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3.5 rounded-xl border border-slate-100">
                  {selectedReport.valueChainImpact}
                </div>
              </div>

              {/* 3대 심층 테마 분석 그리드 */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                {/* 1. FDE 딜리버리 모델 */}
                <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs space-y-1.5">
                  <div className="flex items-center gap-1.5 text-indigo-900 font-bold text-xs">
                    <Boxes size={15} className="text-indigo-600" />
                    FDE / RDE 딜리버리
                  </div>
                  <p className="text-[11px] text-slate-400 font-medium">현장 상주형 딜리버리 & 상업 모델</p>
                  <div className="text-[11px] leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                    {selectedReport.fdeDeliveryAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>

                {/* 2. AI Pricing 과금 전이 */}
                <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs space-y-1.5">
                  <div className="flex items-center gap-1.5 text-emerald-900 font-bold text-xs">
                    <Coins size={15} className="text-emerald-600" />
                    AI Pricing 모델 전이
                  </div>
                  <p className="text-[11px] text-slate-400 font-medium">M/M에서 Consumption/Outcome 전환</p>
                  <div className="text-[11px] leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                    {selectedReport.pricingModelAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>

                {/* 3. Agentic ITO / Ops */}
                <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs space-y-1.5">
                  <div className="flex items-center gap-1.5 text-purple-900 font-bold text-xs">
                    <Cpu size={15} className="text-purple-600" />
                    Agentic ITO / Ops
                  </div>
                  <p className="text-[11px] text-slate-400 font-medium">Observe→Execute 자율 운영 루프</p>
                  <div className="text-[11px] leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                    {selectedReport.agenticOpsAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>
              </div>

              {/* 기회, 위협 및 구조적 리스크 */}
              {selectedReport.mspOpportunitiesThreats && (
                <div className="rounded-2xl border border-slate-200 bg-white p-4 sm:p-5 shadow-xs space-y-2">
                  <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                    <ShieldAlert size={16} className="text-rose-600" />
                    국내 AI MSP 기회, 위협 및 구조적 리스크
                  </h3>
                  <div className="text-xs sm:text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3.5 rounded-xl border border-slate-100">
                    {selectedReport.mspOpportunitiesThreats}
                  </div>
                </div>
              )}
            </main>
          )}
        </div>
      )}
    </div>
  )
}
