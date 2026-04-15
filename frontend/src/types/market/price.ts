export interface PriceSnapshot {
  id: string
  symbol: string
  source: string
  price: string
  currency: string
  changePct24h?: string
  volume24h?: string
  fetchedAt: string
}

export interface PriceTicker {
  symbol: string
  price: string
  currency: string
  changePct24h?: string
  trend: 'UP' | 'DOWN' | 'NEUTRAL'
}

export function toPriceTicker(snapshot: PriceSnapshot): PriceTicker {
  const rawChange = snapshot.changePct24h?.trim()
  const parsedChange = rawChange ? Number.parseFloat(rawChange) : 0
  const change = Number.isFinite(parsedChange) ? parsedChange : 0

  return {
    symbol: snapshot.symbol,
    price: snapshot.price,
    currency: snapshot.currency,
    changePct24h: Number.isFinite(parsedChange) && rawChange !== undefined ? rawChange : undefined,
    trend: change > 0 ? 'UP' : change < 0 ? 'DOWN' : 'NEUTRAL',
  }
}
