import { useQuery } from '@tanstack/react-query'

import { fetchPriceTicker } from '@/features/market/api'
import { useEventSource } from '@/features/market/hooks/useEventSource'
import { toPriceTicker, type PriceSnapshot } from '@/types/market'
import { env } from '@/lib/env'

export function usePriceTicker() {
  const sseSnapshots = useEventSource<PriceSnapshot>(`${env.apiUrl}/api/prices/stream`)
  const isLive = sseSnapshots !== undefined

  const query = useQuery({
    queryKey: ['market', 'ticker'],
    queryFn: fetchPriceTicker,
    refetchInterval: isLive ? false : 15_000,
  })

  if (isLive) {
    return { ...query, data: sseSnapshots.map(toPriceTicker), isLive }
  }
  return { ...query, isLive }
}
