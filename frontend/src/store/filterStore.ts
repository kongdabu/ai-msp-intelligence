import { create } from 'zustand'
import { Category, Competitor, SourceType } from '../types'

interface ArticleFilter {
  competitor: Competitor | ''
  category: Category | ''
  sourceType: SourceType | ''
  keyword: string
  dateFrom: string
  dateTo: string
}

interface FilterStore {
  articleFilter: ArticleFilter
  setArticleFilter: (filter: Partial<ArticleFilter>) => void
  resetArticleFilter: () => void
}

const defaultFilter: ArticleFilter = {
  competitor: '',
  category: '',
  sourceType: '',
  keyword: '',
  dateFrom: '',
  dateTo: '',
}

export const useFilterStore = create<FilterStore>((set) => ({
  articleFilter: defaultFilter,
  setArticleFilter: (filter) =>
    set((state) => ({
      articleFilter: { ...state.articleFilter, ...filter },
    })),
  resetArticleFilter: () => set({ articleFilter: defaultFilter }),
}))
