import { toPriceTicker, type PriceSnapshot, type PriceTicker } from '@/types/market'

export async function fetchMarketSnapshots(): Promise<PriceSnapshot[]> {
  return [
    {
      id: '1',
      symbol: 'AAPL',
      source: 'ALPHA_VANTAGE',
      price: '184.65',
      currency: 'USD',
      changePct24h: '1.24',
      volume24h: '52451321.21',
      fetchedAt: '2026-04-02T08:55:00.000Z',
    },
    {
      id: '2',
      symbol: 'MSFT',
      source: 'ALPHA_VANTAGE',
      price: '412.31',
      currency: 'USD',
      changePct24h: '-0.34',
      volume24h: '33451102.42',
      fetchedAt: '2026-04-02T08:55:00.000Z',
    },
    {
      id: '3',
      symbol: 'NVDA',
      source: 'ALPHA_VANTAGE',
      price: '957.18',
      currency: 'USD',
      changePct24h: '2.48',
      volume24h: '75421345.66',
      fetchedAt: '2026-04-02T08:55:00.000Z',
    },
  ]
}

export async function fetchPriceTicker(): Promise<PriceTicker[]> {
  const snapshots = await fetchMarketSnapshots()
  return snapshots.map(toPriceTicker)
}
