import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { PageResponse, StoredRadarReport } from '../types'

export interface StoredRadarReportFilters {
  reportType: string
  fromDate: string
  toDate: string
  page: number
  size?: number
}

export function useStoredRadarReports(filters: StoredRadarReportFilters) {
  return useQuery<PageResponse<StoredRadarReport>>({
    queryKey: ['stored-radar-reports', filters],
    queryFn: async () => {
      const { data } = await axios.get('/api/v1/radar/reports', {
        params: {
          reportType: filters.reportType || undefined,
          fromDate: filters.fromDate || undefined,
          toDate: filters.toDate || undefined,
          page: filters.page,
          size: filters.size ?? 20,
        },
      })
      return data
    },
    placeholderData: (previousData) => previousData,
  })
}

export function useStoredRadarReport(reportDate: string | null, reportType: string | null) {
  return useQuery<StoredRadarReport>({
    queryKey: ['stored-radar-report', reportDate, reportType],
    queryFn: async () => {
      const { data } = await axios.get(`/api/v1/radar/reports/${reportDate}`, {
        params: { reportType },
      })
      return data
    },
    enabled: reportDate !== null && reportType !== null,
  })
}
