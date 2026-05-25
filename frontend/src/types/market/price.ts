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
  const raw = snapshot.changePct24h
  const parsedChange = raw != null ? Number.parseFloat(String(raw)) : 0
  const change = Number.isFinite(parsedChange) ? parsedChange : 0

  return {
    symbol: snapshot.symbol,
    price: String(snapshot.price),
    currency: snapshot.currency,
    changePct24h: Number.isFinite(parsedChange) && raw != null ? String(raw) : undefined,
    trend: change > 0 ? 'UP' : change < 0 ? 'DOWN' : 'NEUTRAL',
  }
}
