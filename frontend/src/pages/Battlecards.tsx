import { useState, useEffect } from 'react'
import { Swords, RefreshCw, AlertCircle } from 'lucide-react'
import { useBattleCards, useBattleCardsByCompetitor, useGenerateBattleCards } from '../hooks/useBattlecards'
import BattlecardPanel from '../components/battlecard/BattlecardPanel'
import { Competitor } from '../types'

const TABS: { value: Competitor | 'ALL'; label: string }[] = [
  { value: 'ALL',    label: '전체' },
  { value: 'LG_CNS', label: 'LG CNS' },
  { value: 'SK_AX',  label: 'SK AX' },
  { value: 'BESPIN', label: '베스핀글로벌' },
  { value: 'PWC',    label: 'PwC' },
]

function BattlecardList({ competitor }: { competitor: Competitor | 'ALL' }) {
  const allQuery        = useBattleCards()
  const competitorQuery = useBattleCardsByCompetitor(competitor !== 'ALL' ? competitor : '')

  const { data, isLoading, error } = competitor === 'ALL' ? allQuery : competitorQuery

  if (isLoading) return <div className="text-center py-16 text-gray-400">불러오는 중...</div>
  if (error) return (
    <div className="flex items-center gap-2 text-red-500 py-8">
      <AlertCircle size={18} />
      <span className="text-sm">배틀카드를 불러올 수 없습니다.</span>
    </div>
  )
  if (!data || data.length === 0) return (
    <div className="text-center py-16 text-gray-400">
      <Swords size={40} className="mx-auto mb-3 text-gray-300" />
      <p className="text-sm">아직 생성된 배틀카드가 없습니다.</p>
      <p className="text-xs mt-1">"배틀카드 갱신" 버튼을 클릭해 생성하세요.</p>
    </div>
  )

  // 전체 탭: 경쟁사별 최신 1건만 표시 / 특정 탭: 이력 전체 표시
  const cards = competitor === 'ALL'
    ? data
    : data.slice(0, 10)

  return (
    <div className="space-y-4">
      {competitor !== 'ALL' && data.length > 1 && (
        <p className="text-xs text-gray-400">최근 {data.length}개 이력 (최대 10건)</p>
      )}
      {cards.map(card => <BattlecardPanel key={card.id} card={card} />)}
    </div>
  )
}

export default function Battlecards() {
  const [activeTab, setActiveTab]     = useState<Competitor | 'ALL'>('ALL')
  const [generateMsg, setGenerateMsg] = useState<string | null>(null)

  // 성공 메시지 5초 후 자동 닫힘
  useEffect(() => {
    if (!generateMsg) return
    const timer = setTimeout(() => setGenerateMsg(null), 5000)
    return () => clearTimeout(timer)
  }, [generateMsg])

  const { mutate: generate, isPending } = useGenerateBattleCards({
    onSuccess: (data) => setGenerateMsg(`${data.length}개 경쟁사 배틀카드 생성 완료`),
  })

  return (
    <div className="p-6 space-y-6 max-w-5xl mx-auto">
      {/* 헤더 */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <Swords size={22} className="text-blue-600" />
          <h1 className="text-xl font-bold text-gray-900">배틀카드</h1>
          <span className="text-sm text-gray-500">경쟁사별 SWOT 분석 및 영업 대응 전략</span>
        </div>
        <button
          onClick={() => generate()}
          disabled={isPending}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          <RefreshCw size={15} className={isPending ? 'animate-spin' : ''} />
          {isPending ? '생성 중...' : '배틀카드 갱신'}
        </button>
      </div>

      {/* 성공 메시지 (5초 자동 닫힘) */}
      {generateMsg && (
        <div className="bg-green-50 border border-green-200 text-green-800 text-sm rounded-lg px-4 py-3 flex justify-between items-center">
          <span>✅ {generateMsg}</span>
          <button onClick={() => setGenerateMsg(null)} className="text-green-600 hover:text-green-800 ml-3">✕</button>
        </div>
      )}

      {/* 탭 */}
      <div className="flex gap-1 border-b border-gray-200">
        {TABS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => setActiveTab(value)}
            className={`px-4 py-2 text-sm font-medium rounded-t-lg transition-colors ${
              activeTab === value
                ? 'bg-white border border-b-white border-gray-200 -mb-px text-blue-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* 본문 */}
      <BattlecardList competitor={activeTab} />
    </div>
  )
}
