import { Insight } from '../../types'

interface Props {
  insights: Insight[]
}

// 카테고리 코드 → 표시 이름 매핑
const CATEGORY_LABELS: Record<string, string> = {
  AI_AGENT: 'AI Agent',
  VERTICAL_AI: 'Vertical AI',
  ITO: 'ITO',
  MSP: 'MSP',
  CLOUD: 'Cloud',
  GEN_AI: 'Gen AI',
  GENERAL: '일반',
}

// 경쟁사 코드 → 표시 이름 매핑
const COMPETITOR_LABELS: Record<string, string> = {
  LG_CNS: 'LG CNS',
  SK_AX: 'SK AX',
  BESPIN: '베스핀',
  PWC: 'PwC',
}

function normalizeToken(token: string): string {
  // 카테고리 코드면 변환
  if (CATEGORY_LABELS[token]) return CATEGORY_LABELS[token]
  // 경쟁사 코드면 변환
  if (COMPETITOR_LABELS[token]) return COMPETITOR_LABELS[token]
  return token
}

export default function KeywordCloud({ insights }: Props) {
  const wordCount = new Map<string, number>()
  const stopWords = new Set(['및', '을', '를', '이', '가', '의', '에', '로', '으로', '한', '하는', '대한'])

  insights.forEach((insight) => {
    // | 와 공백/구두점 모두로 분리
    const words = insight.title
      .split(/[\s,·|]+/)
      .map((w) => normalizeToken(w.trim()))
      .filter((w) => w.length > 1 && !stopWords.has(w))
    words.forEach((word) => wordCount.set(word, (wordCount.get(word) ?? 0) + 1))
  })

  const keywords = Array.from(wordCount.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 15)

  const maxCount = keywords[0]?.[1] ?? 1
  const sizes = ['text-xs', 'text-sm', 'text-base', 'text-lg', 'text-xl', 'text-2xl']

  if (keywords.length === 0) {
    return <div className="text-gray-400 text-sm text-center py-8">데이터가 없습니다</div>
  }

  return (
    <div className="flex flex-wrap gap-2 py-2">
      {keywords.map(([word, count]) => {
        const idx = Math.min(Math.floor((count / maxCount) * (sizes.length - 1)), sizes.length - 1)
        const opacity = 0.5 + (count / maxCount) * 0.5
        return (
          <span
            key={word}
            className={`${sizes[idx]} font-medium text-blue-600 cursor-default hover:text-blue-800 transition-colors`}
            style={{ opacity }}
            title={`${count}건`}
          >
            {word}
          </span>
        )
      })}
    </div>
  )
}
