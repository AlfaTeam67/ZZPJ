export type AssetType = 'STOCK' | 'CRYPTO' | 'BOND'

export interface Asset {
  id: string
  portfolioId: string
  type: AssetType
  symbol: string
  quantity: string // string — BigDecimal from backend, NEVER a number
  avgBuyPrice: string // string — BigDecimal
  currency: string
  addedAt: string
  updatedAt?: string
  currentValue?: string // optional, calculated by the backend
}

export function isAsset(obj: unknown): obj is Asset {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.portfolioId === 'string' &&
    ['STOCK', 'CRYPTO', 'BOND'].includes(candidate.type as string) &&
    typeof candidate.symbol === 'string' &&
    typeof candidate.quantity === 'string' &&
    typeof candidate.avgBuyPrice === 'string' &&
    typeof candidate.currency === 'string' &&
    typeof candidate.addedAt === 'string'
  )
}
