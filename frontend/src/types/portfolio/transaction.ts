export type TransactionType = 'BUY' | 'SELL'

export interface Transaction {
  id: string
  assetId: string
  portfolioId: string
  type: TransactionType
  quantity: string // string — BigDecimal
  price: string // string — BigDecimal
  currency: string
  fee?: string | null
  executedAt: string
  notes?: string | null
}

export function isTransaction(obj: unknown): obj is Transaction {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.assetId === 'string' &&
    typeof candidate.portfolioId === 'string' &&
    ['BUY', 'SELL'].includes(candidate.type as string) &&
    typeof candidate.quantity === 'string' &&
    typeof candidate.price === 'string' &&
    typeof candidate.currency === 'string' &&
    typeof candidate.executedAt === 'string'
  )
}
