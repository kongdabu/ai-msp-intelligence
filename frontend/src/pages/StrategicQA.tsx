import { useState, useRef, useEffect, KeyboardEvent } from 'react'
import { MessageSquare, Plus, Send, Loader2 } from 'lucide-react'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import ChatMessage from '../components/qa/ChatMessage'
import { useSessions, useMessages, useCreateSession, useAskQuestion } from '../hooks/useQA'

const EXAMPLE_QUESTIONS = [
  '금융권 망분리 환경에서 AI Agent 도입 사례가 경쟁사 중 어디에 있나?',
  '지난 3개월간 경쟁사들의 주요 AI 전략 변화는?',
  '공공 AI 발주에서 가장 자주 등장한 기술 키워드는?',
]

export default function StrategicQA() {
  const [selectedSessionId, setSelectedSessionId] = useState<number | null>(null)
  const [input, setInput] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const { data: sessions = [] } = useSessions()
  const { data: messages = [], isLoading: messagesLoading } = useMessages(selectedSessionId)
  const createSession = useCreateSession()
  const askQuestion = useAskQuestion(selectedSessionId)

  // 새 메시지가 추가되면 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleNewSession = async () => {
    const session = await createSession.mutateAsync()
    setSelectedSessionId(session.id)
    setInput('')
  }

  const handleSend = async (text?: string) => {
    const question = (text ?? input).trim()
    if (!question || askQuestion.isPending) return

    // 세션 없으면 자동 생성
    let sessionId = selectedSessionId
    if (!sessionId) {
      const session = await createSession.mutateAsync()
      sessionId = session.id
      setSelectedSessionId(session.id)
    }

    setInput('')
    await askQuestion.mutateAsync(question)
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const isPending = askQuestion.isPending || createSession.isPending

  return (
    <div className="flex h-[calc(100vh-64px)] bg-gray-50">
      {/* 세션 목록 사이드바 */}
      <aside className="w-64 shrink-0 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-100">
          <button
            onClick={handleNewSession}
            disabled={createSession.isPending}
            className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-3 py-2 rounded-lg transition-colors disabled:opacity-50"
          >
            <Plus size={15} />
            새 대화
          </button>
        </div>
        <nav className="flex-1 overflow-y-auto p-2 space-y-0.5">
          {sessions.length === 0 ? (
            <p className="text-xs text-gray-400 text-center py-6">대화 기록 없음</p>
          ) : (
            sessions.map(session => (
              <button
                key={session.id}
                onClick={() => setSelectedSessionId(session.id)}
                className={`w-full text-left px-3 py-2.5 rounded-lg text-sm transition-colors ${
                  selectedSessionId === session.id
                    ? 'bg-blue-50 text-blue-700 font-medium'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <div className="truncate">{session.title}</div>
                <div className="text-xs text-gray-400 mt-0.5">
                  {format(new Date(session.createdAt), 'MM.dd HH:mm', { locale: ko })}
                </div>
              </button>
            ))
          )}
        </nav>
      </aside>

      {/* 채팅 영역 */}
      <main className="flex-1 flex flex-col min-w-0">
        {/* 헤더 */}
        <div className="bg-white border-b border-gray-200 px-6 py-4">
          <div className="flex items-center gap-2">
            <MessageSquare size={18} className="text-blue-600" />
            <h1 className="text-base font-semibold text-gray-900">전략 Q&A</h1>
          </div>
          <p className="text-xs text-gray-500 mt-0.5">수집된 기사·인사이트를 기반으로 전략 질문에 답변합니다</p>
        </div>

        {/* 메시지 스레드 */}
        <div className="flex-1 overflow-y-auto px-6 py-6 space-y-4">
          {!selectedSessionId && messages.length === 0 && (
            <div className="flex flex-col items-center justify-center h-full space-y-6 text-center">
              <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center">
                <MessageSquare size={24} className="text-blue-600" />
              </div>
              <div>
                <p className="text-gray-700 font-medium">무엇이 궁금하신가요?</p>
                <p className="text-sm text-gray-400 mt-1">경쟁사 동향, 시장 트렌드, 전략 방향 등을 물어보세요</p>
              </div>
              <div className="flex flex-col gap-2 w-full max-w-lg">
                {EXAMPLE_QUESTIONS.map(q => (
                  <button
                    key={q}
                    onClick={() => handleSend(q)}
                    disabled={isPending}
                    className="text-left text-sm text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-xl px-4 py-3 transition-colors disabled:opacity-50"
                  >
                    {q}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messagesLoading && (
            <div className="flex justify-center py-8">
              <Loader2 size={20} className="animate-spin text-gray-400" />
            </div>
          )}

          {messages.map(msg => (
            <ChatMessage key={msg.id} message={msg} />
          ))}

          {isPending && (
            <div className="flex justify-start">
              <div className="flex items-center gap-2 ml-9">
                <div className="w-7 h-7 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                  <span className="text-blue-600 text-xs font-bold">AI</span>
                </div>
                <div className="bg-white border border-gray-200 rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm">
                  <Loader2 size={16} className="animate-spin text-gray-400" />
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* 입력 영역 */}
        <div className="bg-white border-t border-gray-200 px-6 py-4">
          <div className="flex items-end gap-3 max-w-4xl mx-auto">
            <textarea
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="질문을 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)"
              disabled={isPending}
              rows={1}
              className="flex-1 resize-none border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:bg-gray-50"
              style={{ minHeight: '48px', maxHeight: '140px' }}
              onInput={e => {
                const el = e.currentTarget
                el.style.height = 'auto'
                el.style.height = Math.min(el.scrollHeight, 140) + 'px'
              }}
            />
            <button
              onClick={() => handleSend()}
              disabled={!input.trim() || isPending}
              className="shrink-0 w-11 h-11 flex items-center justify-center bg-blue-600 hover:bg-blue-700 text-white rounded-xl transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              {isPending ? <Loader2 size={18} className="animate-spin" /> : <Send size={18} />}
            </button>
          </div>
        </div>
      </main>
    </div>
  )
}
