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
  const { data: reportsData, isLoading, isError, refetch } = useStrategyReports(page, 10)
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
    <div className="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">
      {/* 상단 헤더 */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-slate-200 pb-5">
        <div>
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-indigo-50 border border-indigo-100 text-xs font-semibold text-indigo-700 mb-2">
            <Compass size={14} />
            Daily Intelligence Brief
          </div>
          <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">AI 서비스 산업 데일리 브리핑</h1>
          <p className="mt-1 text-sm text-slate-600">
            일간 수집된 산업 신호를 바탕으로 밸류체인 재편, FDE 딜리버리, AI 과금 체계 전이 및 국내 MSP 핵심 실행 과제를 요약합니다.
          </p>
        </div>

        <button
          onClick={handleGenerate}
          disabled={isGenerating}
          className={`inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold shadow-sm transition-all shrink-0 cursor-pointer ${
            isAdmin
              ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white hover:from-indigo-700 hover:to-blue-700'
              : 'bg-slate-100 border border-slate-300 text-slate-700 hover:bg-slate-200'
          }`}
          title={isAdmin ? 'AI 데일리 브리핑 생성' : '관리자 인증 필요'}
        >
          {isGenerating ? (
            <>
              <RefreshCw size={16} className="animate-spin" />
              Gemini 데일리 브리핑 생성 중...
            </>
          ) : isAdmin ? (
            <>
              <Sparkles size={16} />
              신규 데일리 브리핑 생성
            </>
          ) : (
            <>
              <Sparkles size={16} className="text-slate-400" />
              🔒 데일리 브리핑 생성 (관리자 전용)
            </>
          )}
        </button>
      </div>

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-400">
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
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* 좌측 보고서 목록 타임라인 (lg:col-span-4) */}
          <div className="lg:col-span-4 space-y-3">
            <div className="flex items-center justify-between px-1">
              <h2 className="text-sm font-bold text-slate-700 flex items-center gap-1.5">
                <Clock size={16} className="text-slate-500" />
                발행 히스토리
              </h2>
              <span className="text-xs text-slate-400">총 {reportsData?.totalElements ?? reports.length}건</span>
            </div>

            <div className="space-y-2">
              {reports.map((report) => {
                const isSelected = selectedReport?.id === report.id
                return (
                  <div
                    key={report.id}
                    onClick={() => setSelectedReportId(report.id)}
                    className={`p-4 rounded-xl border transition-all cursor-pointer text-left ${
                      isSelected
                        ? 'border-indigo-600 bg-indigo-50/70 shadow-sm ring-1 ring-indigo-600'
                        : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'
                    }`}
                  >
                    <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
                      <span className="flex items-center gap-1 font-medium text-slate-600">
                        <Calendar size={13} />
                        {formatPeriod(report.periodStart, report.periodEnd)}
                      </span>
                      <span className="inline-flex items-center gap-1 rounded bg-slate-100 px-1.5 py-0.5 font-medium text-slate-600">
                        <Layers size={11} />
                        신호 {report.sourceSignalCount}건
                      </span>
                    </div>
                    <h3 className={`font-semibold text-sm line-clamp-2 ${isSelected ? 'text-indigo-950' : 'text-slate-800'}`}>
                      {report.title}
                    </h3>
                    <p className="mt-1.5 text-xs text-slate-500 line-clamp-2">
                      {report.executiveSummary}
                    </p>
                  </div>
                )
              })}
            </div>

            {/* 페이지네이션 */}
            {(reportsData?.totalPages ?? 1) > 1 && (
              <div className="flex items-center justify-between pt-2 px-1 text-xs text-slate-500">
                <button
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="inline-flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 font-medium hover:bg-slate-50 disabled:opacity-40 cursor-pointer"
                >
                  <ChevronLeft size={14} />
                  이전
                </button>
                <span>
                  {page + 1} / {reportsData?.totalPages}
                </span>
                <button
                  disabled={page + 1 >= (reportsData?.totalPages ?? 1)}
                  onClick={() => setPage((p) => p + 1)}
                  className="inline-flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 font-medium hover:bg-slate-50 disabled:opacity-40 cursor-pointer"
                >
                  다음
                  <ChevronRight size={14} />
                </button>
              </div>
            )}
          </div>

          {/* 우측 보고서 상세 뷰어 (lg:col-span-8) */}
          {selectedReport && (
            <div className="lg:col-span-8 space-y-6">
              {/* 보고서 타이틀 & 메타 카드 */}
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 mb-2">
                  <span className="inline-flex items-center gap-1 rounded-md bg-blue-50 border border-blue-100 px-2.5 py-1 font-semibold text-blue-700">
                    <Calendar size={13} />
                    분석 기간: {formatPeriod(selectedReport.periodStart, selectedReport.periodEnd)}
                  </span>
                  <span className="inline-flex items-center gap-1 rounded-md bg-emerald-50 border border-emerald-100 px-2.5 py-1 font-semibold text-emerald-700">
                    <Layers size={13} />
                    근거 신호 {selectedReport.sourceSignalCount}건 종합
                  </span>
                  <span className="text-slate-400">
                    생성: {new Date(selectedReport.generatedAt).toLocaleString('ko-KR')}
                  </span>
                </div>
                <h2 className="text-xl sm:text-2xl font-bold text-slate-950 leading-snug">
                  {selectedReport.title}
                </h2>
              </div>

              {/* 국내 MSP 관점 Top 3 Action 배너 */}
              <div className="rounded-2xl border border-amber-200 bg-gradient-to-br from-amber-50/80 via-orange-50/40 to-amber-100/30 p-6 shadow-sm">
                <div className="flex items-center gap-2 text-amber-900 font-bold text-base mb-3">
                  <div className="rounded-lg bg-amber-500 p-1.5 text-white">
                    <CheckCircle2 size={18} />
                  </div>
                  국내 MSP 관점 핵심 실행 과제 (Top 3 Action)
                </div>
                <div className="prose prose-sm prose-amber max-w-none text-slate-800 whitespace-pre-line leading-relaxed font-medium bg-white/80 p-4 rounded-xl border border-amber-200/60 shadow-xs">
                  {selectedReport.top3Actions}
                </div>
              </div>

              {/* 경영진 핵심 요약 (Executive Summary) */}
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-3">
                  <FileText size={18} className="text-indigo-600" />
                  경영진 핵심 요약 (Executive Summary)
                </h3>
                <div className="text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-4 rounded-xl border border-slate-100">
                  {selectedReport.executiveSummary}
                </div>
              </div>

              {/* 밸류체인 재편 분석 (Value Chain Impact) */}
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-3">
                  <TrendingUp size={18} className="text-blue-600" />
                  Consulting–SI–MSP–ITO 산업 밸류체인 재편 및 상호 침투
                </h3>
                <div className="text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-4 rounded-xl border border-slate-100">
                  {selectedReport.valueChainImpact}
                </div>
              </div>

              {/* 3대 심층 테마 분석 그리드 */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* 1. FDE 딜리버리 모델 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-2">
                  <div className="flex items-center gap-2 text-indigo-900 font-bold text-sm">
                    <Boxes size={18} className="text-indigo-600" />
                    FDE / RDE 딜리버리
                  </div>
                  <p className="text-xs text-slate-500 font-medium">현장 상주형 전문 딜리버리 및 상업 모델</p>
                  <div className="text-xs leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3 rounded-lg border border-slate-100">
                    {selectedReport.fdeDeliveryAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>

                {/* 2. AI Pricing 과금 전이 */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-2">
                  <div className="flex items-center gap-2 text-emerald-900 font-bold text-sm">
                    <Coins size={18} className="text-emerald-600" />
                    AI Pricing 모델 전이
                  </div>
                  <p className="text-xs text-slate-500 font-medium">M/M 단가에서 Consumption/Outcome 전환</p>
                  <div className="text-xs leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3 rounded-lg border border-slate-100">
                    {selectedReport.pricingModelAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>

                {/* 3. Agentic ITO / Ops */}
                <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-2">
                  <div className="flex items-center gap-2 text-purple-900 font-bold text-sm">
                    <Cpu size={18} className="text-purple-600" />
                    Agentic ITO / Ops
                  </div>
                  <p className="text-xs text-slate-500 font-medium">Observe→Execute 자율 운영 루프</p>
                  <div className="text-xs leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-3 rounded-lg border border-slate-100">
                    {selectedReport.agenticOpsAnalysis || '분석 내용이 없습니다.'}
                  </div>
                </div>
              </div>

              {/* 기회, 위협 및 구조적 리스크 */}
              {selectedReport.mspOpportunitiesThreats && (
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                  <h3 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-3">
                    <ShieldAlert size={18} className="text-rose-600" />
                    국내 AI MSP 기회, 위협 및 구조적 리스크
                  </h3>
                  <div className="text-sm leading-relaxed text-slate-700 whitespace-pre-line bg-slate-50 p-4 rounded-xl border border-slate-100">
                    {selectedReport.mspOpportunitiesThreats}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
