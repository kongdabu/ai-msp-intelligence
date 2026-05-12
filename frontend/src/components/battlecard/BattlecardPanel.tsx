import { BattleCard, COMPETITOR_LABELS, COMPETITOR_COLORS } from '../../types'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

interface Props {
  card: BattleCard
}

const SWOT_CONFIG = [
  { key: 'strengths',     label: '강점 (Strengths)',     bg: 'bg-blue-50',   border: 'border-blue-200',   text: 'text-blue-800',   dot: 'bg-blue-500' },
  { key: 'weaknesses',    label: '약점 (Weaknesses)',    bg: 'bg-red-50',    border: 'border-red-200',    text: 'text-red-800',    dot: 'bg-red-500' },
  { key: 'opportunities', label: '기회 (Opportunities)', bg: 'bg-green-50',  border: 'border-green-200',  text: 'text-green-800',  dot: 'bg-green-500' },
  { key: 'threats',       label: '위협 (Threats)',       bg: 'bg-amber-50',  border: 'border-amber-200',  text: 'text-amber-800',  dot: 'bg-amber-500' },
] as const

export default function BattlecardPanel({ card }: Props) {
  const competitorLabel = COMPETITOR_LABELS[card.competitor] ?? card.competitor
  const competitorColor = COMPETITOR_COLORS[card.competitor] ?? '#6b7280'

  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-6 space-y-5">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-3 h-3 rounded-full" style={{ backgroundColor: competitorColor }} />
          <h3 className="text-lg font-bold text-gray-900">{competitorLabel}</h3>
          <span className="text-xs text-gray-400">
            {format(new Date(card.generatedAt), 'yyyy.MM.dd', { locale: ko })} 생성
          </span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">위협도</span>
          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
            card.impactScore >= 4 ? 'bg-red-100 text-red-700' :
            card.impactScore >= 3 ? 'bg-amber-100 text-amber-700' :
            'bg-green-100 text-green-700'
          }`}>
            {card.impactScore}/5
          </span>
        </div>
      </div>

      {/* SWOT 2x2 그리드 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {SWOT_CONFIG.map(({ key, label, bg, border, text, dot }) => {
          const items = card[key as keyof Pick<BattleCard, 'strengths' | 'weaknesses' | 'opportunities' | 'threats'>]
          return (
            <div key={key} className={`${bg} ${border} border rounded-lg p-4`}>
              <div className={`text-xs font-semibold ${text} mb-2`}>{label}</div>
              {items.length === 0 ? (
                <p className="text-xs text-gray-400">데이터 없음</p>
              ) : (
                <ul className="space-y-1">
                  {items.map((item, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
                      <span className={`mt-1.5 w-1.5 h-1.5 rounded-full shrink-0 ${dot}`} />
                      {item}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )
        })}
      </div>

      {/* 대응 전략 */}
      {card.ourStrategy && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <div className="text-xs font-semibold text-gray-500 mb-1">우리의 대응 전략</div>
          <p className="text-sm text-gray-800 leading-relaxed">{card.ourStrategy}</p>
        </div>
      )}

      {/* 출처 기사 수 */}
      <div className="text-xs text-gray-400 text-right">
        참조 기사 {card.sourceArticleCount}건 기반
      </div>
    </div>
  )
}
