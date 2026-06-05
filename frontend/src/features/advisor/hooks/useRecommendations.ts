import { useQuery } from '@tanstack/react-query'

import { fetchRecommendations } from '@/features/advisor/api'
import { fetchFirstPortfolio } from '@/features/portfolio/api'

export function useRecommendations() {
  return useQuery({
    queryKey: ['advisor', 'recommendations'],
    queryFn: async () => {
      const portfolio = await fetchFirstPortfolio()
      if (!portfolio) return null

      return fetchRecommendations({
        portfolioId: portfolio.id,
      })
    },
  })
}
