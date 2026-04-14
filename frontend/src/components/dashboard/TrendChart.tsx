import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts'
import { CategoryTrend, CATEGORY_LABELS, Category } from '../../types'

interface Props {
  data: CategoryTrend[]
}

const LINE_COLORS = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#06b6d4']

export default function TrendChart({ data }: Props) {
  const dateMap = new Map<string, Record<string, number>>()
  data.forEach(({ date, category, count }) => {
    if (!date || !category) return
    if (!dateMap.has(date)) dateMap.set(date, {})
    dateMap.get(date)![category] = count
  })

  const chartData = Array.from(dateMap.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, cats]) => ({ date: date.slice(5), ...cats }))

  const categories = [...new Set(
    data.map((d) => d.category).filter((c): c is Category => !!c)
  )]

  if (chartData.length === 0 || categories.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-gray-400 text-sm">
        데이터가 없습니다
      </div>
    )
  }

  return (
    <div>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={chartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="date" tick={{ fontSize: 12 }} />
          <YAxis tick={{ fontSize: 12 }} />
          <Tooltip formatter={(value, name) => [value, CATEGORY_LABELS[name as Category] ?? name]} />
          {categories.map((cat, i) => (
            <Line
              key={cat}
              type="monotone"
              dataKey={cat}
              stroke={LINE_COLORS[i % LINE_COLORS.length]}
              strokeWidth={2}
              dot={false}
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
      <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 mt-2">
        {categories.map((cat, i) => (
          <div key={cat} className="flex items-center gap-1 text-xs text-gray-600">
            <span className="inline-block w-4 h-0.5" style={{ backgroundColor: LINE_COLORS[i % LINE_COLORS.length] }} />
            {CATEGORY_LABELS[cat]}
          </div>
        ))}
      </div>
    </div>
  )
}
