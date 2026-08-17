import { ArrowLeft, Building2, Save, SlidersHorizontal, Lock } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useRadarPlayers, useUpdateRadarPlayer } from '../hooks/useRadar'
import { RADAR_LAYER_LABELS, RadarPlayer } from '../types'
import { useAuthStore } from '../store/authStore'
import AdminAuthModal from '../components/common/AdminAuthModal'

function PlayerRow({ player, isAdmin, onRequireAuth }: { player: RadarPlayer; isAdmin: boolean; onRequireAuth: () => void }) {
  const updatePlayer = useUpdateRadarPlayer()
  const [website, setWebsite] = useState(player.website ?? '')
  const [watchPriority, setWatchPriority] = useState(player.watchPriority)
  const [active, setActive] = useState(player.active)

  useEffect(() => {
    setWebsite(player.website ?? '')
    setWatchPriority(player.watchPriority)
    setActive(player.active)
  }, [player.active, player.id, player.watchPriority, player.website])

  const save = () => {
    if (!isAdmin) {
      onRequireAuth()
      return
    }
    updatePlayer.mutate({
      id: player.id,
      input: { website: website.trim() || null, watchPriority, active },
    })
  }

  return (
    <article className={`rounded-xl border p-4 transition-all ${active ? 'border-slate-200 bg-white' : 'border-slate-200 bg-slate-50 opacity-70'}`}>
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="font-bold text-slate-900">{player.name}</h3>
          <p className="mt-1 text-xs text-slate-500">{player.country} · {RADAR_LAYER_LABELS[player.layer]}</p>
        </div>
        <label className="flex items-center gap-2 text-sm font-medium text-slate-700">
          <input
            type="checkbox"
            checked={active}
            disabled={!isAdmin}
            onChange={(event) => {
              if (!isAdmin) {
                onRequireAuth()
                return
              }
              setActive(event.target.checked)
            }}
            className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 disabled:opacity-50"
          />
          수집·분석 대상
        </label>
      </div>
      <div className="mt-3 grid gap-2 sm:grid-cols-[minmax(0,1fr)_110px_auto]">
        <input
          value={website}
          disabled={!isAdmin}
          onChange={(event) => setWebsite(event.target.value)}
          placeholder="공식 사이트 URL"
          className="min-w-0 rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-700 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:bg-slate-100 disabled:text-slate-500"
        />
        <select
          value={watchPriority}
          disabled={!isAdmin}
          onChange={(event) => setWatchPriority(Number(event.target.value))}
          className="rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-700 disabled:bg-slate-100 disabled:text-slate-500"
        >
          <option value={1}>우선순위 1</option>
          <option value={2}>우선순위 2</option>
          <option value={3}>우선순위 3</option>
          <option value={4}>우선순위 4</option>
          <option value={5}>우선순위 5</option>
        </select>
        <button
          type="button"
          onClick={save}
          disabled={updatePlayer.isPending}
          className={`inline-flex items-center justify-center gap-1 rounded-lg px-3 py-2 text-sm font-semibold transition-colors cursor-pointer ${
            isAdmin
              ? 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 disabled:opacity-50'
              : 'border border-slate-200 bg-slate-100 text-slate-400'
          }`}
          title={isAdmin ? '저장' : '관리자 인증 필요'}
        >
          <Save size={15} />
          {isAdmin ? '저장' : '🔒 저장'}
        </button>
      </div>
      {updatePlayer.isError && <p className="mt-2 text-xs text-rose-600">저장에 실패했습니다. 권한과 네트워크 상태를 확인해 주세요.</p>}
    </article>
  )
}

export default function WatchListSettings() {
  const { data: players, isLoading, isError } = useRadarPlayers()
  const { isAdmin } = useAuthStore()
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false)

  if (isLoading) return <div className="p-6 text-sm text-slate-500">감시 대상을 불러오는 중입니다.</div>
  if (isError || !players) return <div className="p-6 text-sm text-slate-500">감시 대상을 불러오지 못했습니다.</div>

  const groups = players.reduce<Record<string, RadarPlayer[]>>((result, player) => {
    result[player.layer] = [...(result[player.layer] ?? []), player]
    return result
  }, {})

  return (
    <div className="mx-auto max-w-4xl space-y-6 p-4 sm:p-6 lg:p-8">
      <div>
        <Link to="/settings" className="inline-flex items-center gap-1 text-sm font-semibold text-blue-700 hover:text-blue-900">
          <ArrowLeft size={16} />
          시스템 설정
        </Link>
        <div className="mt-3 flex items-start justify-between flex-wrap gap-3">
          <div className="flex items-start gap-3">
            <div className="rounded-xl bg-blue-50 p-2.5 text-blue-700">
              <SlidersHorizontal size={20} />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-950">Radar 감시 대상 관리</h1>
              <p className="mt-1 text-sm leading-6 text-slate-600">
                수집·분석에 포함할 사업자와 우선순위를 관리합니다.
              </p>
            </div>
          </div>
          {!isAdmin && (
            <button
              type="button"
              onClick={() => setIsAuthModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-indigo-200 bg-indigo-50 text-indigo-700 text-xs font-semibold hover:bg-indigo-100 transition-colors cursor-pointer"
            >
              <Lock size={13} />
              관리자 인증하고 수정하기
            </button>
          )}
        </div>
      </div>

      {!isAdmin && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-900 flex items-start gap-2.5">
          <Lock size={16} className="text-amber-600 shrink-0 mt-0.5" />
          <div>
            <p className="font-bold">현재 조회 전용 모드입니다.</p>
            <p className="mt-0.5 text-amber-800">
              감시 대상이나 우선순위를 수정하려면 관리자 인증을 진행해 주세요.
            </p>
          </div>
        </div>
      )}

      {Object.entries(groups).map(([layer, groupedPlayers]) => (
        <section key={layer}>
          <div className="mb-3 flex items-center gap-2">
            <Building2 size={17} className="text-blue-600" />
            <h2 className="font-bold text-slate-900">{RADAR_LAYER_LABELS[layer as keyof typeof RADAR_LAYER_LABELS]}</h2>
            <span className="text-sm text-slate-500">{groupedPlayers.length}개</span>
          </div>
          <div className="space-y-3">
            {groupedPlayers.map((player) => (
              <PlayerRow key={player.id} player={player} isAdmin={isAdmin} onRequireAuth={() => setIsAuthModalOpen(true)} />
            ))}
          </div>
        </section>
      ))}

      <AdminAuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} />
    </div>
  )
}
