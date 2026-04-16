export type MoneyValue = string

export interface Portfolio {
  id: string
  userId: string
  name: string
  description?: string | null
  totalValue: MoneyValue
  createdAt: string
}

export type AssetType = 'STOCK' | 'CRYPTO' | 'BOND'
export type TransactionType = 'BUY' | 'SELL'

export interface Transaction {
  id: string
  assetId: string
  portfolioId: string
  type: TransactionType
  quantity: string
  price: MoneyValue
  currency: string
  fee?: MoneyValue | null
  notes?: string | null
  assetType?: AssetType
  symbol?: string
  executedAt: string
}
