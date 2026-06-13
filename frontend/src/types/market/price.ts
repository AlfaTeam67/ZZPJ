export interface PriceSnapshot {
  id: string
  symbol: string
  source: string
  price: string | number   // backend zwraca number, frontend używa string
  currency: string
  changePct24h?: string | number | null
  volume24h?: string | number | null
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
  // Normalizuj — backend może zwracać price/changePct24h jako number lub string
  const priceStr = String(snapshot.price ?? '0')
  const rawChange = snapshot.changePct24h != null ? String(snapshot.changePct24h).trim() : ''
  const parsedChange = rawChange ? Number.parseFloat(rawChange) : 0
  const change = Number.isFinite(parsedChange) ? parsedChange : 0

  return {
    symbol: snapshot.symbol,
    price: priceStr,
    currency: snapshot.currency,
    changePct24h: rawChange && Number.isFinite(parsedChange) ? rawChange : undefined,
    trend: change > 0 ? 'UP' : change < 0 ? 'DOWN' : 'NEUTRAL',
  }
}
