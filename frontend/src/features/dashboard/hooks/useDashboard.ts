import { useQuery } from '@tanstack/react-query'

import {
  fetchAdvisorSnapshot,
  fetchPerformanceSeries,
  fetchPortfolioMetric,
  fetchWatchlist,
} from '@/features/dashboard/api'
import type { ChartRange } from '@/features/dashboard/types'

export function usePortfolioMetric() {
  return useQuery({
    queryKey: ['dashboard', 'portfolio-metric'],
    queryFn: fetchPortfolioMetric,
  })
}

export function usePerformanceSeries(range: ChartRange) {
  return useQuery({
    queryKey: ['dashboard', 'performance', range],
    queryFn: () => fetchPerformanceSeries(range),
  })
}

export function useWatchlist() {
  return useQuery({
    queryKey: ['dashboard', 'watchlist'],
    queryFn: fetchWatchlist,
  })
}

export function useAdvisorSnapshot() {
  return useQuery({
    queryKey: ['dashboard', 'advisor-snapshot'],
    queryFn: fetchAdvisorSnapshot,
  })
}
