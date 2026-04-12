export interface Article {
  id: number
  url: string
  title: string
  summary: string | null
  competitor: Competitor
  category: Category
  sourceType: SourceType
  sourceName: string
  publishedAt: string
  collectedAt: string
  isProcessed: boolean
  relevanceScore: number | null
}

export interface Insight {
  id: number
  title: string
  content: string
  insightType: InsightType
  competitor: Competitor
  impactScore: number
  actionItems: string[]
  sourceArticleCount: number
  generatedAt: string
}

export interface InsightDetail extends Insight {
  sourceArticles: Article[]
}

export interface Source {
  id: number
  name: string
  url: string
  type: SourceType
  competitor: Competitor
  active: boolean
  lastCrawledAt: string | null
  crawlCount: number
  errorCount: number
}

export interface DashboardSummary {
  todayArticleCount: number
  unprocessedInsightCount: number
  highImpactInsightCount: number
  activeSourceCount: number
  competitorDistribution: Record<Competitor, number>
  categoryTrends: CategoryTrend[]
  latestInsights: Insight[]
  latestArticles: Article[]
}

export interface CategoryTrend {
  category: Category
  date: string
  count: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export type Competitor = 'LG_CNS' | 'SK_AX' | 'BESPIN' | 'PWC' | 'GENERAL'
export type Category = 'AI_AGENT' | 'VERTICAL_AI' | 'ITO' | 'MSP' | 'CLOUD' | 'GEN_AI'
export type SourceType = 'NEWS' | 'HOMEPAGE' | 'SNS' | 'IDC'
export type InsightType = 'OPPORTUNITY' | 'THREAT' | 'TREND' | 'STRATEGY'

export const COMPETITOR_LABELS: Record<Competitor, string> = {
  LG_CNS: 'LG CNS',
  SK_AX: 'SK AX',
  BESPIN: '베스핀글로벌',
  PWC: 'PwC',
  GENERAL: '일반',
}

export const COMPETITOR_COLORS: Record<Competitor, string> = {
  LG_CNS: '#3b82f6',
  SK_AX: '#ef4444',
  BESPIN: '#10b981',
  PWC: '#f59e0b',
  GENERAL: '#6b7280',
}

export const CATEGORY_LABELS: Record<Category, string> = {
  AI_AGENT: 'AI Agent',
  VERTICAL_AI: 'Vertical AI',
  ITO: 'ITO',
  MSP: 'MSP',
  CLOUD: 'Cloud',
  GEN_AI: 'Gen AI',
}

export const INSIGHT_TYPE_LABELS: Record<InsightType, string> = {
  OPPORTUNITY: '기회',
  THREAT: '위협',
  TREND: '트렌드',
  STRATEGY: '전략',
}

export const INSIGHT_TYPE_COLORS: Record<InsightType, string> = {
  OPPORTUNITY: 'bg-green-100 text-green-800',
  THREAT: 'bg-red-100 text-red-800',
  TREND: 'bg-blue-100 text-blue-800',
  STRATEGY: 'bg-purple-100 text-purple-800',
}
