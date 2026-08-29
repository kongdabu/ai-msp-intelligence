import { useMemo, useState } from 'react'
import {
  AlertTriangle,
  ArrowUpRight,
  Calendar,
  ChevronLeft,
  ChevronRight,
  Database,
  FileCode2,
  FileText,
  Filter,
  RefreshCw,
  Search,
  ShieldCheck,
  Sparkles,
  Tag,
} from 'lucide-react'
import { useStoredRadarReport, useStoredRadarReports } from '../hooks/useStoredRadarReports'
import { StoredRadarReport } from '../types'

interface ReportIdentity {
  reportDate: string
  reportType: string
}

function reportIdentity(report: StoredRadarReport): ReportIdentity {
  return { reportDate: report.reportDate, reportType: report.reportType }
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('ko-KR', { dateStyle: 'medium', timeStyle: 'short' })
}

export default function StoredRadarReports() {
  const [reportType, setReportType] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<ReportIdentity | null>(null)
  const [signalQuery, setSignalQuery] = useState('')
  const [showMarkdown, setShowMarkdown] = useState(false)
  const { data: reportsData, isLoading, isError, refetch } = useStoredRadarReports({
    reportType,
    fromDate,
    toDate,
    page,
    size: 20,
  })

  const reports = reportsData?.content ?? []
  const selectedFromList = selected
    ? reports.find((report) => report.reportDate === selected.reportDate && report.reportType === selected.reportType)
    : reports[0]
  const selectedIdentity = selectedFromList ? reportIdentity(selectedFromList) : null
  const { data: selectedReport, isLoading: isDetailLoading } = useStoredRadarReport(
    selectedIdentity?.reportDate ?? null,
    selectedIdentity?.reportType ?? null,
  )
  const report = selectedReport ?? selectedFromList
  const visibleSignals = useMemo(() => {
    if (!report) return []
    const query = signalQuery.trim().toLowerCase()
    if (!query) return report.signals
    return report.signals.filter((signal) => [signal.company, signal.category, signal.importance, signal.signal, signal.fact]
      .some((value) => value.toLowerCase().includes(query)))
  }, [report, signalQuery])

  const applyFilters = () => {
    setPage(0)
    setSelected(null)
  }

  return (
    <div className="mx-auto max-w-7xl space-y-3 p-3 sm:p-5 lg:p-6">
      <section className="flex flex-col gap-3 rounded-2xl bg-slate-950 px-5 py-4 text-white shadow-xs lg:flex-row lg:items-center lg:justify-between">
        <div className="flex min-w-0 items-center gap-3">
          <div className="shrink-0 rounded-xl border border-cyan-400/30 bg-cyan-500/15 p-2 text-cyan-300">
            <Database size={19} />
          </div>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-base font-bold tracking-tight">저장된 AI Services Radar</h1>
              <span className="rounded-full border border-cyan-500/30 bg-cyan-950/70 px-2 py-0.5 text-[11px] font-semibold text-cyan-200">
                MCP 저장소
              </span>
            </div>
            <p className="mt-1 text-xs text-slate-400">MCP로 적재된 Radar 보고서, 구조화 신호와 원본 Markdown을 조회합니다.</p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs text-slate-300">
          <ShieldCheck size={15} className="text-emerald-400" />
          <span>보고서 {reportsData?.totalElements ?? 0}건 저장됨</span>
        </div>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white p-3 shadow-xs">
        <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-5">
          <label className="text-xs font-semibold text-slate-600">
            보고서 유형
            <input value={reportType} onChange={(event) => setReportType(event.target.value)} placeholder="예: AI_SERVICES_RADAR"
              className="mt-1 w-full rounded-lg border border-slate-200 px-2.5 py-2 text-sm font-normal outline-none focus:border-cyan-500 focus:ring-2 focus:ring-cyan-100" />
          </label>
          <label className="text-xs font-semibold text-slate-600">
            시작일
            <input type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-200 px-2.5 py-2 text-sm font-normal outline-none focus:border-cyan-500 focus:ring-2 focus:ring-cyan-100" />
          </label>
          <label className="text-xs font-semibold text-slate-600">
            종료일
            <input type="date" value={toDate} onChange={(event) => setToDate(event.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-200 px-2.5 py-2 text-sm font-normal outline-none focus:border-cyan-500 focus:ring-2 focus:ring-cyan-100" />
          </label>
          <div className="flex items-end gap-2 lg:col-span-2">
            <button type="button" onClick={applyFilters} className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-slate-800 px-3 py-2 text-sm font-semibold text-white transition-colors hover:bg-slate-700">
              <Filter size={15} /> 필터 적용
            </button>
            <button type="button" onClick={() => void refetch()} className="inline-flex items-center justify-center rounded-lg border border-slate-200 p-2 text-slate-600 transition-colors hover:bg-slate-50" aria-label="보고서 목록 새로고침">
              <RefreshCw size={16} />
            </button>
          </div>
        </div>
      </section>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState onRetry={() => void refetch()} />
      ) : reports.length === 0 ? (
        <EmptyState />
      ) : (
        <div className="grid min-h-[620px] gap-3 lg:grid-cols-12">
          <aside className="flex min-h-[360px] flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs lg:col-span-4">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-3.5 py-3">
              <span className="text-xs font-bold text-slate-800">저장 보고서</span>
              <Pagination page={page} totalPages={reportsData?.totalPages ?? 1} onChange={setPage} />
            </div>
            <div className="flex-1 space-y-2 overflow-y-auto bg-slate-50/40 p-2.5">
              {reports.map((item) => {
                const isSelected = selectedIdentity?.reportDate === item.reportDate && selectedIdentity.reportType === item.reportType
                return (
                  <button key={`${item.reportType}-${item.reportDate}`} type="button" onClick={() => { setSelected(reportIdentity(item)); setSignalQuery(''); setShowMarkdown(false) }}
                    className={`w-full rounded-xl border p-3 text-left transition-colors ${isSelected ? 'border-cyan-600 bg-cyan-50 ring-1 ring-cyan-600' : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'}`}>
                    <div className="flex items-center justify-between gap-2 text-[11px] text-slate-500">
                      <span className="inline-flex items-center gap-1"><Calendar size={12} />{item.reportDate}</span>
                      <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-slate-600">신호 {item.signals.length}건</span>
                    </div>
                    <h2 className="mt-1.5 line-clamp-2 text-sm font-bold leading-snug text-slate-900">{item.title}</h2>
                    <p className="mt-1 line-clamp-2 text-xs leading-relaxed text-slate-500">{item.executiveView}</p>
                    <span className="mt-2 inline-flex rounded bg-indigo-50 px-1.5 py-0.5 text-[10px] font-bold text-indigo-700">{item.reportType}</span>
                  </button>
                )
              })}
            </div>
          </aside>

          <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs lg:col-span-8">
            {isDetailLoading && !selectedReport ? <LoadingState /> : report ? (
              <ReportDetail report={report} signals={visibleSignals} signalQuery={signalQuery} onSignalQueryChange={setSignalQuery} showMarkdown={showMarkdown} onToggleMarkdown={() => setShowMarkdown((value) => !value)} />
            ) : null}
          </section>
        </div>
      )}
    </div>
  )
}

function ReportDetail({ report, signals, signalQuery, onSignalQueryChange, showMarkdown, onToggleMarkdown }: {
  report: StoredRadarReport
  signals: StoredRadarReport['signals']
  signalQuery: string
  onSignalQueryChange: (value: string) => void
  showMarkdown: boolean
  onToggleMarkdown: () => void
}) {
  return <div className="h-full overflow-y-auto p-4 sm:p-5">
    <div className="border-b border-slate-100 pb-4">
      <div className="flex flex-wrap items-center gap-2 text-[11px] font-semibold text-slate-500">
        <span className="inline-flex items-center gap-1 rounded-md bg-cyan-50 px-2 py-1 text-cyan-800"><Calendar size={12} />{report.reportDate}</span>
        <span className="inline-flex items-center gap-1 rounded-md bg-indigo-50 px-2 py-1 text-indigo-800"><Tag size={12} />{report.reportType}</span>
        {report.promptVersion && <span className="rounded-md bg-slate-100 px-2 py-1 text-slate-600">Prompt {report.promptVersion}</span>}
      </div>
      <h2 className="mt-3 text-xl font-bold leading-snug text-slate-950">{report.title}</h2>
      <p className="mt-1 text-xs text-slate-400">마지막 저장: {formatDateTime(report.updatedAt)}</p>
    </div>

    <div className="mt-4 grid gap-3 md:grid-cols-2">
      <ContentCard title="경영진 관점" icon={<Sparkles size={16} className="text-cyan-600" />} content={report.executiveView} />
      <ContentCard title="전략적 해석" icon={<FileText size={16} className="text-indigo-600" />} content={report.strategicInterpretation} />
    </div>

    <div className="mt-5 flex items-center justify-between gap-3">
      <div>
        <h3 className="text-sm font-bold text-slate-900">구조화된 산업 신호</h3>
        <p className="mt-0.5 text-xs text-slate-500">기업·카테고리·사실·권고 행동과 근거 출처를 함께 확인합니다.</p>
      </div>
      <span className="shrink-0 rounded-full bg-slate-100 px-2 py-1 text-[11px] font-bold text-slate-600">{signals.length}건</span>
    </div>
    <label className="relative mt-3 block">
      <Search size={16} className="pointer-events-none absolute left-3 top-2.5 text-slate-400" />
      <input value={signalQuery} onChange={(event) => onSignalQueryChange(event.target.value)} placeholder="기업, 카테고리, 신호 또는 사실 검색"
        className="w-full rounded-xl border border-slate-200 py-2 pl-9 pr-3 text-sm outline-none focus:border-cyan-500 focus:ring-2 focus:ring-cyan-100" />
    </label>
    <div className="mt-3 space-y-3">
      {signals.map((signal) => <article key={signal.id} className="rounded-xl border border-slate-200 bg-slate-50/50 p-3.5">
        <div className="flex flex-wrap items-center gap-1.5 text-[11px] font-bold">
          <span className="rounded bg-slate-900 px-1.5 py-0.5 text-white">{signal.company}</span>
          <span className="rounded bg-indigo-100 px-1.5 py-0.5 text-indigo-700">{signal.category}</span>
          <span className="rounded bg-amber-100 px-1.5 py-0.5 text-amber-800">{signal.importance}</span>
        </div>
        <h4 className="mt-2 text-sm font-bold text-slate-900">{signal.signal}</h4>
        <p className="mt-1 text-xs leading-relaxed text-slate-700"><strong>사실:</strong> {signal.fact}</p>
        <div className="mt-2 grid gap-2 text-xs sm:grid-cols-2">
          <p className="rounded-lg bg-white p-2 text-slate-600"><strong>변화:</strong> {signal.whatChanged}</p>
          <p className="rounded-lg bg-white p-2 text-slate-600"><strong>권고 행동:</strong> {signal.recommendedAction}</p>
        </div>
        {signal.sources.length > 0 && <div className="mt-2 border-t border-slate-200 pt-2">
          <p className="mb-1 text-[11px] font-bold text-slate-500">근거 출처</p>
          <div className="flex flex-wrap gap-1.5">
            {signal.sources.map((source) => <a key={source.id} href={source.url} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 rounded-md border border-slate-200 bg-white px-2 py-1 text-[11px] text-blue-700 hover:border-blue-300 hover:bg-blue-50">
              {source.publisher}: {source.title}<ArrowUpRight size={11} />
            </a>)}
          </div>
        </div>}
      </article>)}
      {signals.length === 0 && <p className="rounded-xl border border-dashed border-slate-300 p-6 text-center text-sm text-slate-500">조건에 맞는 신호가 없습니다.</p>}
    </div>

    <div className="mt-5 border-t border-slate-100 pt-4">
      <button type="button" onClick={onToggleMarkdown} className="inline-flex items-center gap-2 text-sm font-bold text-slate-700 hover:text-cyan-700">
        <FileCode2 size={16} className="text-cyan-600" /> 원본 Markdown {showMarkdown ? '숨기기' : '보기'}
      </button>
      {showMarkdown && <pre className="mt-3 overflow-x-auto rounded-xl bg-slate-950 p-4 text-xs leading-relaxed text-slate-100 whitespace-pre-wrap">{report.markdown}</pre>}
    </div>
  </div>
}

function ContentCard({ title, icon, content }: { title: string; icon: React.ReactNode; content: string }) {
  return <section className="rounded-xl border border-slate-200 p-3.5">
    <h3 className="flex items-center gap-1.5 text-sm font-bold text-slate-900">{icon}{title}</h3>
    <p className="mt-2 whitespace-pre-line text-xs leading-relaxed text-slate-700">{content}</p>
  </section>
}

function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) return null
  return <div className="flex items-center gap-1 text-xs text-slate-500">
    <button type="button" disabled={page === 0} onClick={() => onChange(Math.max(0, page - 1))} className="rounded p-1 hover:bg-slate-200 disabled:opacity-30" aria-label="이전 페이지"><ChevronLeft size={14} /></button>
    <span>{page + 1}/{totalPages}</span>
    <button type="button" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)} className="rounded p-1 hover:bg-slate-200 disabled:opacity-30" aria-label="다음 페이지"><ChevronRight size={14} /></button>
  </div>
}

function LoadingState() {
  return <div className="flex min-h-[360px] flex-col items-center justify-center text-slate-400"><RefreshCw size={30} className="mb-3 animate-spin text-cyan-500" /><p className="text-sm font-medium">저장된 Radar 보고서를 불러오는 중입니다...</p></div>
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-red-700"><AlertTriangle size={28} className="mx-auto mb-2" /><p className="font-semibold">저장된 Radar 보고서를 불러오지 못했습니다.</p><button type="button" onClick={onRetry} className="mt-3 text-sm underline">다시 시도</button></div>
}

function EmptyState() {
  return <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center shadow-xs"><Database size={44} className="mx-auto mb-3 text-cyan-500" /><h2 className="text-lg font-bold text-slate-800">저장된 Radar 보고서가 없습니다</h2><p className="mt-1 text-sm text-slate-500">AI Services Radar에서 MCP의 <code>save_radar_report</code> 도구를 호출하면 이곳에서 확인할 수 있습니다.</p></div>
}
