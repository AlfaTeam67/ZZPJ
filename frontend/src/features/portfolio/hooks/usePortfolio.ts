import { useQuery } from '@tanstack/react-query'

import { fetchFirstPortfolio } from '@/features/portfolio/api'

export function usePortfolio() {
  return useQuery({
    queryKey: ['portfolio', 'overview'],
    queryFn: fetchFirstPortfolio,
  })
}
