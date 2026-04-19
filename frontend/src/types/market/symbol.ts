export type SymbolType = 'STOCK' | 'CRYPTO' | 'FOREX'

export interface SupportedSymbol {
  symbol: string
  type: SymbolType
  apiSource: string
  active: boolean
  baseCurrency?: string
  addedAt?: string
}
