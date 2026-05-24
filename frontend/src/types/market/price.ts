// Backend zwraca BigDecimal jako liczby JSON, ale wcześniej kontrakt zakładał
// stringi. Akceptujemy oba warianty, żeby było wstecznie kompatybilnie i
// odporne na zmiany serializacji po stronie market-data-service.
export type DecimalLike = string | number

export interface PriceSnapshot {
  id: string
  symbol: string
  source: string
  price: DecimalLike
  currency: string
  changePct24h?: DecimalLike
  volume24h?: DecimalLike
  fetchedAt: string
}

export interface PriceTicker {
  symbol: string
  price: DecimalLike
  currency: string
  changePct24h?: string
  trend: 'UP' | 'DOWN' | 'NEUTRAL'
}

function parseDecimal(value: DecimalLike | undefined): number | undefined {
  if (value === undefined || value === null) return undefined
  const parsed = typeof value === 'number' ? value : Number.parseFloat(value.trim())
  return Number.isFinite(parsed) ? parsed : undefined
}

export function toPriceTicker(snapshot: PriceSnapshot): PriceTicker {
  const change = parseDecimal(snapshot.changePct24h)

  return {
    symbol: snapshot.symbol,
    price: snapshot.price,
    currency: snapshot.currency,
    changePct24h: change !== undefined ? String(change) : undefined,
    trend:
      change === undefined || change === 0 ? 'NEUTRAL' : change > 0 ? 'UP' : 'DOWN',
  }
}
