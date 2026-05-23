import { useQuery } from '@tanstack/react-query'
import { fetchPortfolioValuation } from '@/features/portfolio/api'

export function usePortfolioValuation(portfolioId: string | null) {
  return useQuery({
    queryKey: ['portfolio-valuation', portfolioId],
    queryFn: async () => {
      if (!portfolioId) return null
      return fetchPortfolioValuation(portfolioId)
    },
    enabled: !!portfolioId,
    refetchInterval: 30000, // Refetch every 30 seconds
  })
}
