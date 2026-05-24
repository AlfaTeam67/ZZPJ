import {
  toPriceTicker,
  type PriceSnapshot,
  type PriceTicker,
  type SupportedSymbol,
} from '@/types/market'
import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'

export interface SymbolDto {
  symbol: string
  type: 'STOCK' | 'CRYPTO' | 'FOREX'
  apiSource: string
  active: boolean
  baseCurrency?: string
}

export interface MarketPriceDto {
  symbol: string
  source: string
  price: string
  currency: string
  changePct24h?: string
  volume24h?: string
  fetchedAt?: string
}

export async function fetchMarketSnapshots(): Promise<PriceSnapshot[]> {
  const { data } = await apiClient.get<PriceSnapshot[]>(`${env.apiUrl}/api/market-prices/latest`)
  return data
}

export async function fetchPriceTicker(): Promise<PriceTicker[]> {
  const snapshots = await fetchMarketSnapshots()
  return snapshots.map(toPriceTicker)
}

export async function fetchPriceHistory(ticker: string): Promise<PriceSnapshot[]> {
  const { data } = await apiClient.get<PriceSnapshot[]>(
    `${env.apiUrl}/api/market-prices/symbol/${ticker}`
  )
  return data
}

export async function addMarketPrice(priceDto: MarketPriceDto): Promise<PriceSnapshot> {
  const { data } = await apiClient.post<PriceSnapshot>(`${env.apiUrl}/api/market-prices`, priceDto)
  return data
}

export async function fetchSymbols(): Promise<SupportedSymbol[]> {
  const { data } = await apiClient.get<SupportedSymbol[]>(`${env.apiUrl}/api/symbols`)
  return data
}

export async function fetchSymbolDetails(symbol: string): Promise<SupportedSymbol> {
  const { data } = await apiClient.get<SupportedSymbol>(`${env.apiUrl}/api/symbols/${symbol}`)
  return data
}

export async function createSymbol(symbolDto: SymbolDto): Promise<SupportedSymbol> {
  const { data } = await apiClient.post<SupportedSymbol>(`${env.apiUrl}/api/symbols`, symbolDto)
  return data
}
