import { Insight } from '../../types'

interface Props {
  insights: Insight[]
}

export default function KeywordCloud({ insights }: Props) {
  // 인사이트 제목에서 키워드 추출 (간단 구현)
  const wordCount = new Map<string, number>()
  const stopWords = new Set(['및', '을', '를', '이', '가', '의', '에', '로', '으로', '한', '하는', '대한'])

  insights.forEach((insight) => {
    const words = insight.title.split(/[\s,·]+/).filter((w) => w.length > 1 && !stopWords.has(w))
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
