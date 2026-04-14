import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts'
import { Competitor, COMPETITOR_LABELS, COMPETITOR_COLORS } from '../../types'

interface Props {
  data: Record<string, number>
}

export default function CompetitorDonut({ data }: Props) {
  const chartData = Object.entries(data)
    .filter(([, v]) => v > 0)
    .map(([key, value]) => ({
      name: COMPETITOR_LABELS[key as Competitor] ?? key,
      value,
      color: COMPETITOR_COLORS[key as Competitor] ?? '#9ca3af',
    }))

  if (chartData.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-gray-400 text-sm">
        데이터가 없습니다
      </div>
    )
  }

  return (
    <div>
      <ResponsiveContainer width="100%" height={200}>
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            innerRadius={55}
            outerRadius={85}
            paddingAngle={3}
            dataKey="value"
          >
            {chartData.map((entry, index) => (
              <Cell key={index} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => [`${value}건`, '']} />
        </PieChart>
      </ResponsiveContainer>
      <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 mt-2">
        {chartData.map((entry) => (
          <div key={entry.name} className="flex items-center gap-1 text-xs text-gray-600">
            <span className="inline-block w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
            {entry.name}
          </div>
        ))}
      </div>
    </div>
  )
}
