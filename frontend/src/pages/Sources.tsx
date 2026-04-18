import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { Source, COMPETITOR_LABELS, Competitor, SourceType } from '../types'
import { useTriggerCrawl } from '../hooks/useArticles'
import { formatDistanceToNow } from 'date-fns'
import { ko } from 'date-fns/locale'
import { Plus, RefreshCw, CheckCircle, XCircle, CheckCheck } from 'lucide-react'

const SOURCE_TYPE_COLORS: Record<string, string> = {
  NEWS: 'bg-blue-100 text-blue-800',
  HOMEPAGE: 'bg-purple-100 text-purple-800',
  SNS: 'bg-pink-100 text-pink-800',
  IDC: 'bg-orange-100 text-orange-800',
}

export default function Sources() {
  const queryClient = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', url: '', type: 'NEWS', competitor: 'GENERAL' })
  const [crawlResult, setCrawlResult] = useState<{ crawledCount: number } | null>(null)

  const { data: sources = [], isLoading } = useQuery<Source[]>({
    queryKey: ['sources'],
    queryFn: async () => {
      const { data } = await axios.get('/api/sources')
      return data
    },
  })

  const { mutate: toggle } = useMutation({
    mutationFn: (id: number) => axios.put(`/api/sources/${id}/toggle`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sources'] }),
  })

  const { mutate: addSource, isPending: isAdding } = useMutation({
    mutationFn: (body: typeof form) => axios.post('/api/sources', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sources'] })
      setShowForm(false)
      setForm({ name: '', url: '', type: 'NEWS', competitor: 'GENERAL' })
    },
  })

  const { mutate: triggerCrawl, isPending: isCrawling } = useTriggerCrawl({
    onSuccess: (data) => setCrawlResult({ crawledCount: data.crawledCount }),
  })

  useEffect(() => {
    if (!crawlResult) return
    const timer = setTimeout(() => setCrawlResult(null), 6000)
    return () => clearTimeout(timer)
  }, [crawlResult])

  return (
    <div className="p-4 sm:p-6 space-y-4">
      {crawlResult && (
        <div className="flex items-center gap-2 px-4 py-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-800">
          <CheckCheck size={16} className="shrink-0" />
          <span>수집 완료 — 신규 기사 <strong>{crawlResult.crawledCount}건</strong> 저장되었습니다.</span>
          <button onClick={() => setCrawlResult(null)} className="ml-auto text-green-600 hover:text-green-800">✕</button>
        </div>
      )}

      {/* 액션 버튼 */}
      <div className="flex items-center justify-between gap-3">
        <div className="text-sm text-gray-500">
          총 <span className="font-semibold text-gray-900">{sources.length}</span>개
          {' '}(<span className="text-green-600">{sources.filter(s => s.active).length}개 활성</span>)
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => triggerCrawl()}
            disabled={isCrawling}
            className="flex items-center gap-1.5 btn-secondary text-xs sm:text-sm"
          >
            <RefreshCw size={14} className={isCrawling ? 'animate-spin' : ''} />
            <span className="hidden sm:inline">{isCrawling ? '수집 중...' : '지금 수집'}</span>
          </button>
          <button
            onClick={() => setShowForm(!showForm)}
            className="flex items-center gap-1.5 btn-primary text-xs sm:text-sm"
          >
            <Plus size={14} />
            <span>소스 추가</span>
          </button>
        </div>
      </div>

      {/* 소스 추가 폼 */}
      {showForm && (
        <div className="card border-blue-200 bg-blue-50">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">신규 소스 추가</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3">
            <input
              type="text"
              placeholder="소스 이름"
              className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
            <input
              type="url"
              placeholder="URL"
              className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 sm:col-span-2"
              value={form.url}
              onChange={(e) => setForm({ ...form, url: e.target.value })}
            />
            <select
              className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value })}
            >
              {(['NEWS', 'HOMEPAGE', 'SNS', 'IDC'] as SourceType[]).map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
            <select
              className="border border-gray-300 rounded-md text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={form.competitor}
              onChange={(e) => setForm({ ...form, competitor: e.target.value })}
            >
              {(['GENERAL', 'LG_CNS', 'SK_AX', 'BESPIN', 'PWC'] as (Competitor | 'GENERAL')[]).map((c) => (
                <option key={c} value={c}>{COMPETITOR_LABELS[c as Competitor] ?? c}</option>
              ))}
            </select>
          </div>
          <div className="flex gap-2 mt-3">
            <button
              onClick={() => addSource(form)}
              disabled={isAdding || !form.name || !form.url}
              className="btn-primary"
            >
              {isAdding ? '저장 중...' : '저장'}
            </button>
            <button onClick={() => setShowForm(false)} className="btn-secondary">취소</button>
          </div>
        </div>
      )}

      {/* 소스 목록 */}
      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="card animate-pulse h-16" />
          ))}
        </div>
      ) : (
        <div className="card overflow-x-auto p-0">
          <table className="w-full min-w-[640px]">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">소스명</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">URL</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">타입</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">경쟁사</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">마지막 수집</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">수집/오류</th>
                <th className="text-center px-4 py-3 text-xs font-semibold text-gray-500">상태</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sources.map((source) => (
                <tr key={source.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm font-medium text-gray-900 whitespace-nowrap">{source.name}</td>
                  <td className="px-4 py-3 text-xs text-gray-500 max-w-[200px] truncate">
                    <a href={source.url} target="_blank" rel="noopener noreferrer" className="hover:text-blue-600">
                      {source.url}
                    </a>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`badge ${SOURCE_TYPE_COLORS[source.type] ?? 'bg-gray-100 text-gray-700'}`}>
                      {source.type}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-600 whitespace-nowrap">
                    {COMPETITOR_LABELS[source.competitor as Competitor] ?? source.competitor}
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500 whitespace-nowrap">
                    {source.lastCrawledAt
                      ? formatDistanceToNow(new Date(source.lastCrawledAt), { addSuffix: true, locale: ko })
                      : '-'}
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500 whitespace-nowrap">
                    {source.crawlCount} / <span className={source.errorCount > 0 ? 'text-red-500' : ''}>{source.errorCount}</span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button onClick={() => toggle(source.id)} className="flex items-center gap-1 mx-auto">
                      {source.active
                        ? <CheckCircle size={18} className="text-green-500" />
                        : <XCircle size={18} className="text-gray-300" />}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
