import { useQuery } from '@tanstack/react-query'
import { fetchSymbols } from '@/features/market/api'

export function useSymbols() {
  return useQuery({
    queryKey: ['market', 'symbols'],
    queryFn: fetchSymbols,
    staleTime: 5 * 60_000,
  })
}
