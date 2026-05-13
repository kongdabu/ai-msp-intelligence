import { useState } from 'react'
import { ChevronDown, ChevronUp, ExternalLink } from 'lucide-react'
import { QaMessage, COMPETITOR_LABELS, COMPETITOR_COLORS } from '../../types'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

interface Props {
  message: QaMessage
}

export default function ChatMessage({ message }: Props) {
  const [showSources, setShowSources] = useState(false)
  const isUser = message.role === 'USER'
  const time = format(new Date(message.createdAt), 'HH:mm', { locale: ko })

  if (isUser) {
    return (
      <div className="flex justify-end">
        <div className="max-w-[75%]">
          <div className="bg-blue-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 text-sm leading-relaxed">
            {message.content}
          </div>
          <p className="text-xs text-gray-400 text-right mt-1">{time}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex justify-start">
      <div className="max-w-[80%] space-y-1">
        {/* AI 답변 버블 */}
        <div className="flex items-start gap-2">
          <div className="w-7 h-7 rounded-full bg-blue-100 flex items-center justify-center shrink-0 mt-0.5">
            <span className="text-blue-600 text-xs font-bold">AI</span>
          </div>
          <div className="bg-white border border-gray-200 rounded-2xl rounded-tl-sm px-4 py-3 text-sm text-gray-800 leading-relaxed shadow-sm">
            {message.content}
          </div>
        </div>

        {/* 출처 기사 */}
        {message.sourceArticles.length > 0 && (
          <div className="ml-9">
            <button
              onClick={() => setShowSources(prev => !prev)}
              className="flex items-center gap-1 text-xs text-gray-400 hover:text-blue-500 transition-colors"
            >
              {showSources ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
              참고 기사 {message.sourceArticles.length}건
            </button>
            {showSources && (
              <div className="mt-2 space-y-1.5">
                {message.sourceArticles.map(article => (
                  <div key={article.id}
                    className="flex items-center justify-between gap-2 bg-gray-50 rounded-lg px-3 py-2 text-xs">
                    <div className="flex items-center gap-2 min-w-0">
                      <span
                        className="shrink-0 px-1.5 py-0.5 rounded text-white text-[10px] font-medium"
                        style={{ backgroundColor: COMPETITOR_COLORS[article.competitor] ?? '#6b7280' }}
                      >
                        {COMPETITOR_LABELS[article.competitor] ?? article.competitor}
                      </span>
                      <span className="text-gray-700 truncate">{article.title}</span>
                    </div>
                    <a
                      href={article.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="shrink-0 text-gray-400 hover:text-blue-500"
                    >
                      <ExternalLink size={12} />
                    </a>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        <p className="text-xs text-gray-400 ml-9">{time}</p>
      </div>
    </div>
  )
}
