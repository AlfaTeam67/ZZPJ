import { useQuery } from '@tanstack/react-query'

import { fetchRecommendations } from '@/features/advisor/api'
import { fetchFirstPortfolio } from '@/features/portfolio/api'

export function useRecommendations(
  riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE' = 'MODERATE',
  investmentHorizon: 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM' = 'MID_TERM'
) {
  return useQuery({
    queryKey: ['advisor', 'recommendations', riskTolerance, investmentHorizon],
    queryFn: async () => {
      const portfolio = await fetchFirstPortfolio()
      if (!portfolio) return null

      return fetchRecommendations({
        portfolioId: portfolio.id,
        riskTolerance,
        investmentHorizon,
      })
    },
  })
}
