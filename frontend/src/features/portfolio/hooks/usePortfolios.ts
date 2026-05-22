import { useQuery } from '@tanstack/react-query'
import { fetchPortfolios } from '@/features/portfolio/api'

export function usePortfolios() {
  return useQuery({
    queryKey: ['portfolios', 'list'],
    queryFn: fetchPortfolios,
  })
}
