import { useQuery } from '@tanstack/react-query'
import { fetchTransactions } from '@/features/portfolio/api'

export function useTransactions(portfolioId: string | null) {
  return useQuery({
    queryKey: ['portfolio', 'transactions', portfolioId],
    queryFn: async () => {
      if (!portfolioId) return []
      return fetchTransactions(portfolioId)
    },
    enabled: !!portfolioId,
  })
}
