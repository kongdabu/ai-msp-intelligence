import { Activity, ArrowUpRight, Building2, CircleAlert, Layers3, Radar as RadarIcon, RefreshCw, Users } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useRadarCollectionStatus, useRadarOverview, useStartRadarCollection } from '../hooks/useRadar'
import { RADAR_LAYER_LABELS, RadarLensCode, RadarPlayer, RadarPlayerLayer } from '../types'

const layerOrder: RadarPlayerLayer[] = ['FRONTIER_LAB', 'CSP_PLATFORM', 'CONSULTING', 'GLOBAL_SI_MSP', 'KOREA_SI_MSP']

function RadarSkeleton() {
  return (
    <div className="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8 animate-pulse">
      <div className="h-36 rounded-3xl bg-slate-200" />
      <div className="grid gap-4 sm:grid-cols-3">{[1, 2, 3].map((index) => <div key={index} className="h-28 rounded-2xl bg-slate-200" />)}</div>
      <div className="grid gap-4 lg:grid-cols-3">{[1, 2, 3].map((index) => <div key={index} className="h-48 rounded-2xl bg-slate-200" />)}</div>
    </div>
  )
}

function WatchlistGroup({ layer, players, selected, onSelect }: { layer: RadarPlayerLayer; players: RadarPlayer[]; selected: boolean; onSelect: () => void }) {
  return (
    <div className={`rounded-2xl border bg-white p-4 transition ${selected ? 'border-blue-500 ring-1 ring-blue-200' : 'border-slate-200'}`}>
      <div className="flex items-center justify-between gap-3">
        <h3 className="text-sm font-bold text-slate-900">{RADAR_LAYER_LABELS[layer]}</h3>
        <button type="button" onClick={onSelect} aria-pressed={selected} className={`rounded-full px-2 py-0.5 text-xs font-semibold transition ${selected ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-500 hover:bg-blue-100 hover:text-blue-700'}`}>{players.length}개 보기</button>
      </div>
      <div className="mt-3 flex flex-wrap gap-2">
        {players.map((player) => (
          <a key={player.id} href={player.website ?? undefined} target="_blank" rel="noreferrer" className="rounded-lg bg-slate-50 px-2.5 py-1.5 text-xs text-slate-700 transition hover:bg-blue-50 hover:text-blue-700">
            {player.name}
          </a>
        ))}
      </div>
    </div>
  )
}

export default function Radar() {
  const { data, isLoading, isError, refetch } = useRadarOverview()
  const { data: collectionStatus } = useRadarCollectionStatus()
  const { mutate: startCollection, isPending: isStartingCollection } = useStartRadarCollection()
  const [selectedLens, setSelectedLens] = useState<RadarLensCode | null>(null)
  const [selectedLayer, setSelectedLayer] = useState<RadarPlayerLayer | null>(null)
  const [highImpactOnly, setHighImpactOnly] = useState(false)

  useEffect(() => {
    if (collectionStatus?.status === 'COMPLETED') void refetch()
  }, [collectionStatus?.completedAt, collectionStatus?.status, refetch])

  // 로딩 상태가 바뀌어도 Hook 호출 순서가 달라지지 않도록 데이터 유무와 무관하게 먼저 계산한다.
  const selectedLensInfo = data?.lenses.find((lens) => lens.code === selectedLens)
  const visibleSignals = useMemo(
    () => (data?.recentSignals ?? []).filter((signal) =>
      (!selectedLens || signal.lenses.includes(selectedLens)) && (!highImpactOnly || signal.impactScore >= 80)),
    [data?.recentSignals, highImpactOnly, selectedLens],
  )

  if (isLoading) return <RadarSkeleton />
  if (isError || !data) {
    return <div className="p-6 text-sm text-slate-600">Radar 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</div>
  }

  const playersByLayer = data.players.reduce<Record<RadarPlayerLayer, RadarPlayer[]>>((groups, player) => {
    groups[player.layer].push(player)
    return groups
  }, {
    FRONTIER_LAB: [], CSP_PLATFORM: [], CONSULTING: [], GLOBAL_SI_MSP: [], KOREA_SI_MSP: [],
  })
  const scrollToSection = (id: string) => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  const stats = [
    { label: '감시 플레이어', value: data.playerCount, icon: Users, tone: 'text-blue-700 bg-blue-50', active: selectedLayer === null, onClick: () => { setSelectedLayer(null); scrollToSection('watch-list') } },
    { label: '정규화된 신호', value: data.signalCount, icon: Activity, tone: 'text-violet-700 bg-violet-50', active: !highImpactOnly && selectedLens === null, onClick: () => { setSelectedLens(null); setHighImpactOnly(false); scrollToSection('evidence-queue') } },
    { label: '고영향 신호', value: data.highImpactSignalCount, icon: CircleAlert, tone: 'text-rose-700 bg-rose-50', active: highImpactOnly, onClick: () => { setHighImpactOnly((current) => !current); scrollToSection('evidence-queue') } },
  ]
  const isCollecting = isStartingCollection || collectionStatus?.status === 'RUNNING'
  const visibleLayers = selectedLayer ? [selectedLayer] : layerOrder
  const collectionDescription = collectionStatus?.status === 'RUNNING'
    ? '원문 수집과 Signal 분석을 진행 중입니다. 최대 12건을 검증합니다.'
    : collectionStatus?.status === 'COMPLETED'
      ? `최근 실행: 원문 ${collectionStatus.collectedArticleCount ?? 0}건 수집 · ${collectionStatus.analyzedArticleCount ?? 0}건 분석 · Signal ${collectionStatus.savedSignalCount ?? 0}건 등록`
      : collectionStatus?.status === 'FAILED'
        ? '최근 수집 작업이 실패했습니다. 다시 실행해 주세요.'
        : '공식 사이트와 활성 수집 소스의 새 원문을 분석해 Signal로 등록합니다.'

  return (
    <div className="mx-auto max-w-7xl space-y-8 p-4 sm:p-6 lg:p-8">
      <section className="overflow-hidden rounded-3xl bg-slate-950 px-6 py-7 text-white sm:px-8">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <div className="inline-flex items-center gap-2 text-sm font-semibold text-blue-300"><RadarIcon size={17} /> AI Services Industry Radar</div>
            <h2 className="mt-3 text-2xl font-bold tracking-tight sm:text-3xl">신호를 사업 구조와 실행 과제로 전환합니다</h2>
            <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-300">단순 뉴스 목록이 아닌 AI Agent, 파트너십, 현장 딜리버리, 가격, 운영 모델의 변화를 추적하고 한국 AI MSP 관점의 영향을 판단합니다.</p>
          </div>
          <div className="shrink-0 rounded-2xl border border-slate-700 bg-slate-900/70 p-3 text-sm text-slate-300 sm:max-w-xs">
            <div className="font-semibold text-white">Signal → Structure → Impact</div>
            <p className="mt-1 text-xs leading-5">{collectionDescription}</p>
            <button type="button" onClick={() => startCollection()} disabled={isCollecting} className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-blue-500 px-3 py-2 text-sm font-bold text-white transition hover:bg-blue-400 disabled:cursor-not-allowed disabled:opacity-60">
              <RefreshCw size={15} className={isCollecting ? 'animate-spin' : ''} />
              {isCollecting ? 'Radar 수집 중...' : '지금 수집하기'}
            </button>
          </div>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-3" aria-label="Radar 현황">
        {stats.map(({ label, value, icon: Icon, tone, onClick }) => (
          <button key={label} type="button" onClick={onClick} className="rounded-2xl border border-slate-200 bg-white p-5 text-left shadow-sm transition hover:border-blue-300 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500">
            <div className={`inline-flex rounded-xl p-2.5 ${tone}`}><Icon size={19} /></div>
            <p className="mt-4 text-3xl font-bold tracking-tight text-slate-950">{value.toLocaleString()}</p>
            <p className="mt-1 text-sm font-medium text-slate-500">{label} · 클릭해 보기</p>
          </button>
        ))}
      </section>

      <section>
        <div className="mb-4 flex items-end justify-between gap-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-blue-600">Six lenses</p>
            <h2 className="mt-1 text-xl font-bold text-slate-950">산업 재편을 보는 6개 관점</h2>
          </div>
          <span className="hidden text-xs text-slate-500 sm:block">각 신호는 하나 이상의 관점으로 분류됩니다.</span>
        </div>
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {data.lenses.map((lens) => (
            <button key={lens.code} type="button" aria-pressed={selectedLens === lens.code} onClick={() => setSelectedLens((current) => current === lens.code ? null : lens.code)} className={`rounded-2xl border p-4 text-left shadow-sm transition focus:outline-none focus:ring-2 focus:ring-blue-500 ${selectedLens === lens.code ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-200' : 'border-slate-200 bg-white hover:border-blue-300 hover:bg-blue-50/40'}`}>
              <div className="flex items-start justify-between gap-3"><h3 className="font-bold text-slate-900">{lens.label}</h3><span className="rounded-full bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-700">{lens.signalCount} 신호</span></div>
              <p className="mt-2 text-sm leading-5 text-slate-500">{lens.description}</p>
              <p className="mt-3 text-xs font-semibold text-blue-700">{selectedLens === lens.code ? '선택 해제' : '이 관점의 근거 보기'}</p>
            </button>
          ))}
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div id="watch-list">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2"><div className="flex items-center gap-2"><Building2 size={19} className="text-blue-600" /><h2 className="text-xl font-bold text-slate-950">Watch List</h2></div>{selectedLayer && <button type="button" onClick={() => setSelectedLayer(null)} className="text-sm font-semibold text-blue-700 hover:text-blue-900">전체 계층 보기</button>}</div>
          <div className="space-y-3">
            {visibleLayers.map((layer) => <WatchlistGroup key={layer} layer={layer} players={playersByLayer[layer]} selected={selectedLayer === layer} onSelect={() => setSelectedLayer((current) => current === layer ? null : layer)} />)}
          </div>
        </div>
        <div>
          <div className="mb-4 flex items-center gap-2"><Layers3 size={19} className="text-blue-600" /><h2 className="text-xl font-bold text-slate-950">주간 브리핑</h2></div>
          {data.weeklyBriefs.length > 0 ? (
            <div className="space-y-3">
              {data.weeklyBriefs.map((brief) => <article key={brief.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><p className="text-xs font-semibold text-blue-600">{new Date(brief.periodStart).toLocaleDateString('ko-KR')} ~ {new Date(brief.periodEnd).toLocaleDateString('ko-KR')}</p><h3 className="mt-2 font-bold text-slate-900">{brief.title}</h3><p className="mt-2 text-sm leading-6 text-slate-600">{brief.executiveSummary}</p></article>)}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-7 text-center"><ArrowUpRight className="mx-auto text-slate-400" size={22} /><h3 className="mt-3 font-bold text-slate-900">첫 주간 브리핑을 준비 중입니다</h3><p className="mt-1 text-sm leading-5 text-slate-500">검증된 신호가 축적되면 플레이어, 파트너십, 딜리버리·가격·운영 모델, 국내 영향을 하나의 브리핑으로 제공합니다.</p></div>
          )}
        </div>
      </section>

      <section id="evidence-queue">
        <div className="mb-4 flex flex-wrap items-end justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-wider text-blue-600">Evidence queue</p><h2 className="mt-1 text-xl font-bold text-slate-950">{selectedLensInfo ? `${selectedLensInfo.label} 근거 신호` : highImpactOnly ? '고영향 근거 신호' : '최근 구조 변화 신호'}</h2></div>{(selectedLensInfo || highImpactOnly) && <button type="button" onClick={() => { setSelectedLens(null); setHighImpactOnly(false) }} className="text-sm font-semibold text-blue-700 hover:text-blue-900">전체 신호 보기</button>}</div>
        {visibleSignals.length > 0 ? <div className="space-y-3">{visibleSignals.map((signal) => <article key={signal.id} className="rounded-2xl border border-slate-200 bg-white p-5"><div className="flex flex-col gap-2 sm:flex-row sm:justify-between"><div><div className="flex flex-wrap gap-1.5">{signal.lenses.map((lens) => <button type="button" key={lens} onClick={() => setSelectedLens(lens)} className={`rounded-full px-2 py-0.5 text-xs font-semibold ${lens === selectedLens ? 'bg-blue-100 text-blue-800' : 'bg-slate-100 text-slate-600 hover:bg-blue-100 hover:text-blue-800'}`}>{lens}</button>)}</div><h3 className="mt-2 font-bold text-slate-900">{signal.title}</h3><p className="mt-1 text-sm text-slate-600">{signal.fact}</p><a href={signal.sourceUrl} target="_blank" rel="noreferrer" className="mt-3 inline-flex text-xs font-semibold text-blue-700 hover:text-blue-900">원문 근거 열기 <ArrowUpRight className="ml-1" size={13} /></a></div><button type="button" onClick={() => setHighImpactOnly((current) => !current)} aria-pressed={highImpactOnly} className="h-fit shrink-0 rounded-lg bg-rose-50 px-2 py-1 text-xs font-bold text-rose-700 hover:bg-rose-100">영향 {signal.impactScore}</button></div></article>)}</div> : <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-sm text-slate-500">{selectedLensInfo ? `${selectedLensInfo.label} 관점으로 분류된 Signal이 아직 없습니다. Radar 수집을 실행하면 원문을 검증해 자동 등록합니다.` : highImpactOnly ? '영향도 80점 이상의 Signal이 아직 없습니다.' : '아직 정규화된 신호가 없습니다. 수집 파이프라인은 원문 근거와 출처 등급을 확인한 뒤 신호를 등록합니다.'}</div>}
      </section>
    </div>
  )
}
