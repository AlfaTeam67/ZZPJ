export type Signal = 'BUY' | 'HOLD' | 'SELL' | null

const TAG_RE = /^\s*\[(BUY|HOLD|SELL)\]/i
const BUY_RE = /\b(buy|kup|kupuj|warto kupi[ćc]|purchase|accumulate|strong buy)\b/i
const SELL_RE = /\b(sell|sprzedaj|sprzedawa[ćc]|rozwa[żz] sprzeda[żz]|reduce|exit|strong sell)\b/i
// Ticker: 1-5 uppercase letters optionally followed by -USD/-USDT etc.
const TICKER_RE = /\b([A-Z]{1,5}(?:-USD[T]?)?)\b/g

export function detectSignal(text: string): Signal {
  const tag = TAG_RE.exec(text)
  if (tag) return tag[1].toUpperCase() as Signal
  if (SELL_RE.test(text)) return 'SELL'
  if (BUY_RE.test(text)) return 'BUY'
  if (/\b(hold|trzymaj|utrzymuj|maintain)\b/i.test(text)) return 'HOLD'
  return null
}

const NOISE = new Set([
  'BUY',
  'HOLD',
  'SELL',
  'THE',
  'AND',
  'FOR',
  'WITH',
  'FROM',
  'THAT',
  'THIS',
  'YOUR',
  'INTO',
  'OVER',
  'AI',
  'CEO',
  'SEC',
  'FED',
  'FDA',
  'IPO',
  'ETF',
  'GDP',
  'CPI',
  'US',
  'EU',
  'UK',
])

export function extractTicker(text: string): string | null {
  const cleaned = text.replace(TAG_RE, '')
  const matches = cleaned.match(TICKER_RE) ?? []
  const candidate = matches.find((m) => !NOISE.has(m))
  return candidate ?? null
}

export const SIGNAL_STYLES: Record<
  NonNullable<Signal>,
  { card: string; label: string; text: string; badge: string }
> = {
  BUY: {
    card: 'bg-green-500/10 border-green-500/35',
    label: '↑ BUY',
    text: 'text-green-400',
    badge: 'text-green-400',
  },
  HOLD: {
    card: 'bg-yellow-500/10 border-yellow-500/35',
    label: '→ HOLD',
    text: 'text-yellow-400',
    badge: 'text-yellow-400',
  },
  SELL: {
    card: 'bg-red-500/10 border-red-500/35',
    label: '↓ SELL',
    text: 'text-red-400',
    badge: 'text-red-400',
  },
}
