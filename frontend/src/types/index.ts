export interface Article {
  id: number
  url: string
  title: string
  originalContent: string | null
  summary: string | null
  competitor: Competitor
  category: Category
  sourceType: SourceType
  sourceName: string
  publishedAt: string
  collectedAt: string
  isProcessed: boolean
  relevanceScore: number | null
  bookmarked: boolean
  bookmarkedAt?: string | null
  bookmarkNote?: string | null
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
  confidenceScore?: number
  validationReason?: string
  bookmarked: boolean
  bookmarkedAt?: string | null
  bookmarkNote?: string | null
}

export interface InsightDetail extends Insight {
  sourceArticles: Article[]
}

export interface DashboardSummary {
  todayArticleCount: number
  unprocessedInsightCount: number
  highImpactInsightCount: number
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

export interface SystemConfig {
  maxArticlesForInsight: number
  maxInsightsPerGeneration: number
  minRelevanceScoreForInsight: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface RadarLens {
  code: RadarLensCode
  label: string
  description: string
  signalCount: number
}

export interface RadarPlayer {
  id: number
  name: string
  layer: RadarPlayerLayer
  country: string
  website: string | null
  watchPriority: number
}

export interface RadarAssessment {
  whatChanged: string
  industryStructureImpact: string
  mspOpportunity: string | null
  mspThreat: string | null
  structuralRisk: string | null
  recommendedAction: string
  deliveryModel: string | null
  pricingModel: string | null
}

export interface RadarSignal {
  id: number
  title: string
  fact: string
  sourceUrl: string
  sourceTier: string
  signalType: string
  occurredAt: string
  capturedAt: string
  confidenceScore: number
  impactScore: number
  status: string
  lenses: RadarLensCode[]
  players: string[]
  assessment: RadarAssessment | null
}

export interface RadarWeeklyBrief {
  id: number
  periodStart: string
  periodEnd: string
  title: string
  executiveSummary: string
  playerMoves: string
  partnershipChanges: string
  deliveryModelChanges: string
  pricingChanges: string
  agenticOperationsChanges: string
  koreaImpact: string
  generatedAt: string
}

export interface RadarOverview {
  playerCount: number
  signalCount: number
  highImpactSignalCount: number
  lenses: RadarLens[]
  players: RadarPlayer[]
  recentSignals: RadarSignal[]
  weeklyBriefs: RadarWeeklyBrief[]
}

export type Competitor = 'LG_CNS' | 'SK_AX' | 'BESPIN' | 'PWC' | 'GENERAL'
export type Category = 'FRONTIER_LABS' | 'AI_ECOSYSTEM' | 'AI_DELIVERY_MODEL' | 'CONSULTING' | 'AGENTIC_OPERATIONS' | 'AI_AGENT' | 'VERTICAL_AI' | 'ITO' | 'MSP' | 'CLOUD' | 'GEN_AI'
export type SourceType = 'NEWS' | 'HOMEPAGE' | 'SNS' | 'IDC' | 'PROCUREMENT' | 'JOB_POSTING'
export type InsightType = 'OPPORTUNITY' | 'THREAT' | 'TREND' | 'STRATEGY'
export type RadarLensCode = 'AI_AGENT' | 'FRONTIER_LABS' | 'PARTNERSHIP' | 'DEPLOYMENT_MODEL' | 'AI_PRICING' | 'AGENTIC_OPERATIONS'
export type RadarPlayerLayer = 'FRONTIER_LAB' | 'CSP_PLATFORM' | 'CONSULTING' | 'GLOBAL_SI_MSP' | 'KOREA_SI_MSP'

export const RADAR_LAYER_LABELS: Record<RadarPlayerLayer, string> = {
  FRONTIER_LAB: 'Frontier Labs',
  CSP_PLATFORM: 'CSP·플랫폼',
  CONSULTING: '컨설팅',
  GLOBAL_SI_MSP: '글로벌 SI·MSP',
  KOREA_SI_MSP: '국내 SI·MSP',
}

export interface BattleCard {
  id: number
  competitor: Competitor
  strengths: string[]
  weaknesses: string[]
  opportunities: string[]
  threats: string[]
  ourStrategy: string
  impactScore: number | null
  sourceArticleCount: number
  generatedAt: string | null
}

export interface BattleCardDetail extends Omit<BattleCard, 'sourceArticleCount'> {
  sourceArticles: BattleCardSourceArticle[]
}

export interface BattleCardSourceArticle {
  id: number
  title: string
  url: string
  competitor: Competitor
  relevanceScore: number | null
}

export type TrendNewsStatus = 'DRAFT' | 'PUBLISHED'

export interface TrendNews {
  id: number
  periodStart: string
  periodEnd: string
  title: string
  summary: string
  trendScore: number | null
  confidenceScore: number | null
  status: TrendNewsStatus
  keywords: string[]
  sourceArticleCount: number
  generatedAt: string
}

export interface TrendNewsSourceArticle {
  id: number
  title: string
  url: string
  sourceName: string
  competitor: Competitor
  category: Category
  summary: string | null
  publishedAt: string | null
  relevanceScore: number | null
}

export interface TrendNewsDetail extends TrendNews {
  content: string
  actionItems: string[]
  sourceArticles: TrendNewsSourceArticle[]
}

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
  FRONTIER_LABS: 'Frontier AI Labs',
  AI_ECOSYSTEM: 'AI 생태계',
  AI_DELIVERY_MODEL: 'AI 서비스 모델',
  CONSULTING: '컨설팅',
  AGENTIC_OPERATIONS: 'Agentic AI·AIOps',
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
