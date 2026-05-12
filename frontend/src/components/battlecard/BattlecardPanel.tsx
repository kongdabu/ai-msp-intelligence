import { useState } from 'react'
import { BattleCard, COMPETITOR_LABELS, COMPETITOR_COLORS } from '../../types'
import { useBattleCardDetail } from '../../hooks/useBattlecards'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import { ChevronDown, ChevronUp, ExternalLink } from 'lucide-react'

interface Props {
  card: BattleCard
}

const SWOT_CONFIG = [
  { key: 'strengths',     label: '강점',  bg: 'bg-blue-50',   border: 'border-blue-200',   text: 'text-blue-800',   dot: 'bg-blue-500' },
  { key: 'weaknesses',    label: '약점',  bg: 'bg-red-50',    border: 'border-red-200',    text: 'text-red-800',    dot: 'bg-red-500' },
  { key: 'opportunities', label: '기회',  bg: 'bg-green-50',  border: 'border-green-200',  text: 'text-green-800',  dot: 'bg-green-500' },
  { key: 'threats',       label: '위협',  bg: 'bg-amber-50',  border: 'border-amber-200',  text: 'text-amber-800',  dot: 'bg-amber-500' },
] as const

export default function BattlecardPanel({ card }: Props) {
  const [showArticles, setShowArticles] = useState(false)
  const { data: detail, isLoading: detailLoading } = useBattleCardDetail(showArticles ? card.id : null)

  const competitorLabel = COMPETITOR_LABELS[card.competitor] ?? card.competitor
  const competitorColor = COMPETITOR_COLORS[card.competitor] ?? '#6b7280'
  const impactScore     = card.impactScore ?? 0
  const generatedAt     = card.generatedAt ? new Date(card.generatedAt) : null

  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-6 space-y-5">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-3 h-3 rounded-full" style={{ backgroundColor: competitorColor }} />
          <h3 className="text-lg font-bold text-gray-900">{competitorLabel}</h3>
          {generatedAt && (
            <span className="text-xs text-gray-400">
              {format(generatedAt, 'yyyy.MM.dd', { locale: ko })} 생성
            </span>
          )}
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">위협도</span>
          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
            impactScore >= 4 ? 'bg-red-100 text-red-700' :
            impactScore >= 3 ? 'bg-amber-100 text-amber-700' :
                               'bg-green-100 text-green-700'
          }`}>
            {impactScore}/5
          </span>
        </div>
      </div>

      {/* SWOT 2×2 그리드 */}
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

      {/* 출처 기사 — 펼치기/접기 */}
      <div className="border-t border-gray-100 pt-3">
        <button
          onClick={() => setShowArticles(prev => !prev)}
          className="flex items-center gap-1.5 text-xs text-gray-500 hover:text-blue-600 transition-colors"
        >
          {showArticles ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          참조 기사 {card.sourceArticleCount}건 기반
          {!showArticles && <span className="text-gray-400">(클릭하여 확인)</span>}
        </button>

        {showArticles && (
          <div className="mt-3 space-y-2">
            {detailLoading ? (
              <p className="text-xs text-gray-400 pl-1">불러오는 중...</p>
            ) : detail?.sourceArticles.length === 0 ? (
              <p className="text-xs text-gray-400 pl-1">출처 기사 정보 없음</p>
            ) : (
              detail?.sourceArticles
                .sort((a, b) => (b.relevanceScore ?? 0) - (a.relevanceScore ?? 0))
                .map(article => (
                  <div key={article.id} className="flex items-start justify-between gap-2 text-xs bg-gray-50 rounded-lg px-3 py-2">
                    <span className="text-gray-700 line-clamp-1 flex-1">{article.title}</span>
                    <div className="flex items-center gap-2 shrink-0">
                      {article.relevanceScore !== null && (
                        <span className="text-blue-600 font-medium">{article.relevanceScore}%</span>
                      )}
                      <a
                        href={article.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-gray-400 hover:text-blue-500"
                      >
                        <ExternalLink size={12} />
                      </a>
                    </div>
                  </div>
                ))
            )}
          </div>
        )}
      </div>
    </div>
  )
}
