import { useQuery } from '@tanstack/react-query'

import { fetchTransactions } from '@/features/portfolio/api'

export function useTransactions(portfolioId: string) {
  return useQuery({
    queryKey: ['portfolio', 'transactions', portfolioId],
    queryFn: () => fetchTransactions(portfolioId),
    enabled: !!portfolioId,
  })
}
