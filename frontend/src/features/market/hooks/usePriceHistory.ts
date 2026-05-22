import { useQuery } from '@tanstack/react-query'
import { fetchPriceHistory } from '@/features/market/api'

export function usePriceHistory(symbol: string | null) {
  return useQuery({
    queryKey: ['price-history', symbol],
    queryFn: async () => {
      if (!symbol) return []
      return fetchPriceHistory(symbol)
    },
    enabled: !!symbol,
  })
}
