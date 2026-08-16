import { Activity, ChevronLeft, ChevronRight, CircleAlert, ExternalLink, Layers3, Radar as RadarIcon, RefreshCw, Square, Users } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useCancelRadarCollection, useRadarCollectionStatus, useRadarOverview, useRadarSignals, useStartRadarCollection } from '../hooks/useRadar'
import { RadarLensCode, RadarSignal } from '../types'

function RadarSkeleton() {
  return <div className="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8 animate-pulse"><div className="h-36 rounded-3xl bg-slate-200" /><div className="grid gap-4 lg:grid-cols-[280px_1fr]"><div className="h-96 rounded-2xl bg-slate-200" /><div className="h-[32rem] rounded-2xl bg-slate-200" /></div></div>
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
  const selectedLensParam = searchParams.get('lens')
  const selectedLens = overview?.lenses.some((lens) => lens.code === selectedLensParam)
    ? selectedLensParam as RadarLensCode : null
  const highImpactOnly = searchParams.get('impact') === 'high'
  const page = Math.max(Number(searchParams.get('page') ?? '0'), 0)
  const { data: signalPage, isLoading: isSignalsLoading } = useRadarSignals({ lens: selectedLens, minimumImpactScore: highImpactOnly ? 80 : null, page })
  const [selectedSignalId, setSelectedSignalId] = useState<number | null>(null)
  const selectedSignal = useMemo(() => {
    const signals = signalPage?.content ?? []
    return signals.find((signal) => signal.id === selectedSignalId) ?? signals[0] ?? null
  }, [selectedSignalId, signalPage?.content])

  useEffect(() => {
    if (collectionStatus?.status === 'COMPLETED') void refetch()
  }, [collectionStatus?.completedAt, collectionStatus?.status, refetch])

  if (isLoading) return <RadarSkeleton />
  if (isError || !overview) return <div className="p-6 text-sm text-slate-600">Radar 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</div>

  const selectedLensInfo = overview.lenses.find((lens) => lens.code === selectedLens)
  const isCollecting = isStartingCollection || collectionStatus?.status === 'RUNNING' || collectionStatus?.status === 'CANCELLING'
  const updateFilters = (updates: Record<string, string | null>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => value ? next.set(key, value) : next.delete(key))
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
  const collectionDescription = collectionStatus?.status === 'COMPLETED'
    ? `최근 실행: 원문 ${collectionStatus.collectedArticleCount ?? 0}건 · Signal ${collectionStatus.savedSignalCount ?? 0}건 등록`
    : collectionStatus?.status === 'RUNNING' ? '원문 수집과 Signal 분석을 진행 중입니다.'
      : collectionStatus?.status === 'CANCELLING' ? '현재 작업을 안전하게 중지하고 있습니다.'
        : collectionStatus?.status === 'CANCELLED' ? '최근 Radar 수집 작업이 취소되었습니다.'
          : '공식 원문을 검증해 사업 구조 Signal로 등록합니다.'

  return <div className="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">
    <section className="overflow-hidden rounded-3xl bg-slate-950 px-6 py-7 text-white sm:px-8">
      <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div><div className="inline-flex items-center gap-2 text-sm font-semibold text-blue-300"><RadarIcon size={17} /> AI Services Industry Radar</div><h2 className="mt-3 text-2xl font-bold tracking-tight sm:text-3xl">신호를 사업 구조와 실행 과제로 전환합니다</h2><p className="mt-2 max-w-3xl text-sm leading-6 text-slate-300">좌측에서 산업 관점을 선택하면 우측에서 검증된 Signal과 한국 AI MSP 관점의 영향을 바로 확인할 수 있습니다.</p></div>
        <div className="shrink-0 rounded-2xl border border-slate-700 bg-slate-900/70 p-3 text-sm text-slate-300 sm:max-w-xs"><div className="font-semibold text-white">Signal → Structure → Impact</div><p className="mt-1 text-xs leading-5">{collectionDescription}</p>{isCollecting ? <button type="button" onClick={() => cancelCollection()} disabled={isCancellingCollection || collectionStatus?.status === 'CANCELLING'} className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-rose-600 px-3 py-2 text-sm font-bold text-white transition hover:bg-rose-500 disabled:cursor-not-allowed disabled:opacity-60"><Square size={14} />{collectionStatus?.status === 'CANCELLING' ? '취소 요청 중...' : '작업 취소'}</button> : <button type="button" onClick={() => startCollection()} disabled={isStartingCollection} className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-blue-500 px-3 py-2 text-sm font-bold text-white transition hover:bg-blue-400 disabled:cursor-not-allowed disabled:opacity-60"><RefreshCw size={15} className={isStartingCollection ? 'animate-spin' : ''} />지금 수집하기</button>}</div>
      </div>
    </section>

    <div className="grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
      <aside className="h-fit rounded-2xl border border-slate-200 bg-white p-3 shadow-sm lg:sticky lg:top-6" aria-label="Radar 탐색">
        <p className="px-2 pb-2 pt-1 text-xs font-bold uppercase tracking-wider text-blue-600">Radar navigation</p>
        <div className="space-y-1">
          <button type="button" onClick={() => updateFilters({ lens: null, impact: null })} className={`flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-left text-sm font-semibold ${!selectedLens && !highImpactOnly ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'}`}><span className="flex items-center gap-2"><Activity size={16} />전체 Signal</span><span>{overview.signalCount}</span></button>
          <button type="button" onClick={() => updateFilters({ lens: null, impact: highImpactOnly ? null : 'high' })} className={`flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-left text-sm font-semibold ${highImpactOnly ? 'bg-rose-600 text-white' : 'text-slate-700 hover:bg-rose-50'}`}><span className="flex items-center gap-2"><CircleAlert size={16} />고영향 Signal</span><span>{overview.highImpactSignalCount}</span></button>
        </div>
        <div className="my-4 border-t border-slate-100" />
        <p className="px-2 pb-2 text-xs font-bold uppercase tracking-wider text-blue-600">산업 재편 6개 관점</p>
        <div className="space-y-1">{overview.lenses.map((lens) => <button key={lens.code} type="button" aria-pressed={selectedLens === lens.code} onClick={() => updateFilters({ lens: selectedLens === lens.code ? null : lens.code, impact: null })} className={`w-full rounded-xl px-3 py-2.5 text-left transition ${selectedLens === lens.code ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-700 hover:bg-blue-50'}`}><span className="flex items-center justify-between gap-2 text-sm font-semibold"><span>{lens.label}</span><span className={`rounded-full px-2 py-0.5 text-xs ${selectedLens === lens.code ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'}`}>{lens.signalCount}</span></span><span className={`mt-1 block text-xs leading-4 ${selectedLens === lens.code ? 'text-blue-100' : 'text-slate-500'}`}>{lens.description}</span></button>)}</div>
        <div className="my-4 border-t border-slate-100" />
        <Link to="/settings/watch-list" className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-blue-50 hover:text-blue-700"><span className="flex items-center gap-2"><Users size={16} />감시 대상 관리</span><span className="text-xs">{overview.playerCount}개</span></Link>
      </aside>

      <section className="min-w-0">
        <div className="mb-4 flex flex-wrap items-end justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-wider text-blue-600">Evidence queue</p><h2 className="mt-1 text-2xl font-bold text-slate-950">{selectedLensInfo ? `${selectedLensInfo.label} 구조 변화` : highImpactOnly ? '고영향 구조 변화' : '전체 구조 변화 Signal'}</h2><p className="mt-1 text-sm text-slate-500">근거 Signal을 선택하면 사업 구조 영향과 실행 권고를 상세하게 볼 수 있습니다.</p></div><div className="rounded-xl bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-700">{signalPage?.totalElements ?? 0}건</div></div>
        <div className="grid gap-4 xl:grid-cols-[minmax(0,0.9fr)_minmax(320px,0.7fr)]">
          <div className="space-y-3">{isSignalsLoading ? <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">Signal을 불러오는 중입니다.</div> : signalPage?.content.length ? signalPage.content.map((signal) => <button key={signal.id} type="button" onClick={() => setSelectedSignalId(signal.id)} className={`w-full rounded-2xl border bg-white p-4 text-left transition ${selectedSignal?.id === signal.id ? 'border-blue-500 ring-1 ring-blue-200' : 'border-slate-200 hover:border-blue-300 hover:bg-blue-50/30'}`}><div className="flex items-start justify-between gap-3"><div><p className="text-xs font-semibold text-blue-700">{signalLabel(signal)}</p><h3 className="mt-1 font-bold text-slate-900">{signal.title}</h3></div><span className="shrink-0 rounded-lg bg-rose-50 px-2 py-1 text-xs font-bold text-rose-700">영향 {signal.impactScore}</span></div><p className="mt-2 line-clamp-2 text-sm leading-5 text-slate-600">{signal.fact}</p><div className="mt-3 flex flex-wrap gap-1.5">{signal.lenses.map((lens) => <span key={lens} className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-600">{lens}</span>)}</div></button>) : <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center text-sm text-slate-500">{selectedLensInfo ? `${selectedLensInfo.label} 관점으로 분류된 Signal이 아직 없습니다.` : '표시할 Signal이 없습니다.'}</div>}
            {signalPage && signalPage.totalPages > 1 && <div className="flex items-center justify-between pt-2"><button type="button" disabled={signalPage.number === 0} onClick={() => movePage(signalPage.number - 1)} className="inline-flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 disabled:opacity-40"><ChevronLeft size={16} />이전</button><span className="text-sm text-slate-500">{signalPage.number + 1} / {signalPage.totalPages}</span><button type="button" disabled={signalPage.number + 1 >= signalPage.totalPages} onClick={() => movePage(signalPage.number + 1)} className="inline-flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 disabled:opacity-40">다음<ChevronRight size={16} /></button></div>}
          </div>
          <article className="h-fit rounded-2xl border border-slate-200 bg-white p-5 shadow-sm xl:sticky xl:top-6">{selectedSignal ? <><div className="flex flex-wrap gap-1.5">{selectedSignal.lenses.map((lens) => <span key={lens} className="rounded-full bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-700">{lens}</span>)}</div><h3 className="mt-3 text-lg font-bold text-slate-950">{selectedSignal.title}</h3><p className="mt-3 text-sm leading-6 text-slate-700">{selectedSignal.fact}</p><div className="mt-5 space-y-4 border-t border-slate-100 pt-4"><Detail title="무엇이 바뀌었나" content={selectedSignal.assessment?.whatChanged} /><Detail title="산업 구조 영향" content={selectedSignal.assessment?.industryStructureImpact} /><Detail title="MSP 기회" content={selectedSignal.assessment?.mspOpportunity} /><Detail title="MSP 위협·구조적 위험" content={[selectedSignal.assessment?.mspThreat, selectedSignal.assessment?.structuralRisk].filter(Boolean).join('\n')} /><Detail title="권고 행동" content={selectedSignal.assessment?.recommendedAction} /></div><a href={selectedSignal.sourceUrl} target="_blank" rel="noreferrer" className="mt-5 inline-flex items-center gap-1 text-sm font-bold text-blue-700 hover:text-blue-900">원문 근거 열기 <ExternalLink size={15} /></a></> : <div className="py-12 text-center text-sm text-slate-500">좌측 목록에서 Signal을 선택하면 상세 분석을 표시합니다.</div>}</article>
        </div>
        {overview.weeklyBriefs.length > 0 && <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-5"><div className="flex items-center gap-2"><Layers3 size={18} className="text-blue-600" /><h2 className="font-bold text-slate-900">최신 주간 브리핑</h2></div><h3 className="mt-3 font-semibold text-slate-900">{overview.weeklyBriefs[0].title}</h3><p className="mt-1 text-sm leading-6 text-slate-600">{overview.weeklyBriefs[0].executiveSummary}</p></section>}
      </section>
    </div>
  </div>
}

function Detail({ title, content }: { title: string; content?: string | null }) {
  if (!content) return null
  return <div><h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">{title}</h4><p className="mt-1 whitespace-pre-line text-sm leading-6 text-slate-700">{content}</p></div>
}
