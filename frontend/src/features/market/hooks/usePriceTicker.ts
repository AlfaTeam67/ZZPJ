import { useQuery } from '@tanstack/react-query'

import { fetchPriceTicker } from '@/features/market/api'
import { useEventSource } from '@/features/market/hooks/useEventSource'
import { toPriceTicker, type PriceSnapshot } from '@/types/market'
import { env } from '@/lib/env'

export function usePriceTicker() {
  const sseSnapshots = useEventSource<PriceSnapshot>(`${env.apiUrl}/api/prices/stream`)

  const query = useQuery({
    queryKey: ['market', 'ticker'],
    queryFn: fetchPriceTicker,
    refetchInterval: sseSnapshots === undefined ? 15_000 : false,
  })

  if (sseSnapshots !== undefined) {
    return { ...query, data: sseSnapshots.map(toPriceTicker) }
  }
  return query
}
