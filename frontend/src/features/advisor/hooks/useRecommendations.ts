import { useQuery } from '@tanstack/react-query'

import { fetchRecommendations } from '@/features/advisor/api'
import { fetchFirstPortfolio } from '@/features/portfolio/api'

export function useRecommendations(riskTolerance: 'LOW' | 'MEDIUM' | 'HIGH' = 'MEDIUM') {
  return useQuery({
    queryKey: ['advisor', 'recommendations', riskTolerance],
    queryFn: async () => {
      const portfolio = await fetchFirstPortfolio()
      if (!portfolio) return null

      return fetchRecommendations({
        userId: portfolio.userId,
        portfolioId: portfolio.id,
        riskTolerance,
      })
    },
  })
}
