import type { Portfolio, Transaction, Asset } from '@/types/portfolio'
import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'

export interface CreatePortfolioRequest {
  name: string
  description?: string
}

export interface UpdatePortfolioRequest {
  name: string
  description?: string
}

export interface AddAssetRequest {
  type: 'STOCK' | 'CRYPTO' | 'BOND'
  symbol: string
  quantity: string
  avgBuyPrice: string
  currency: string
}

export interface TransactionRequest {
  assetId?: string
  type: 'BUY' | 'SELL'
  quantity: string
  price: string
  currency: string
  fee?: string
  executedAt?: string
  notes?: string
}

export interface PortfolioValuation {
  portfolioId: string
  totalValue: Record<string, string>
  assetValuations: Array<{
    assetId: string
    symbol: string
    currentPrice: string
    currentValue: string
    profitPercentage: string
  }>
}

export async function fetchPortfolios(): Promise<Portfolio[]> {
  // Backend (po ALF-87) zwraca Page<PortfolioResponse>, obsługujemy oba formaty.
  const { data } = await apiClient.get<Portfolio[] | { content: Portfolio[] }>(
    `${env.portfolioApiUrl}/api/portfolios`
  )
  if (Array.isArray(data)) return data
  return data.content ?? []
}

/**
 * Convenience method to fetch the first portfolio for the current user.
 * Many UI parts currently assume a single portfolio exists.
 */
export async function fetchFirstPortfolio(): Promise<Portfolio | null> {
  const portfolios = await fetchPortfolios()
  return portfolios[0] || null
}

export async function fetchPortfolio(id: string): Promise<Portfolio> {
  const { data } = await apiClient.get<Portfolio>(`${env.portfolioApiUrl}/api/portfolios/${id}`)
  return data
}

export async function createPortfolio(request: CreatePortfolioRequest): Promise<Portfolio> {
  const { data } = await apiClient.post<Portfolio>(`${env.portfolioApiUrl}/api/portfolios`, request)
  return data
}

export async function updatePortfolio(
  id: string,
  request: UpdatePortfolioRequest
): Promise<Portfolio> {
  const { data } = await apiClient.put<Portfolio>(
    `${env.portfolioApiUrl}/api/portfolios/${id}`,
    request
  )
  return data
}

export async function deletePortfolio(id: string): Promise<void> {
  await apiClient.delete(`${env.portfolioApiUrl}/api/portfolios/${id}`)
}

export async function fetchPortfolioValuation(id: string): Promise<PortfolioValuation> {
  const { data } = await apiClient.get<PortfolioValuation>(
    `${env.portfolioApiUrl}/api/portfolios/${id}/valuation`
  )
  return data
}

export async function addAsset(portfolioId: string, request: AddAssetRequest): Promise<Asset> {
  const { data } = await apiClient.post<Asset>(
    `${env.portfolioApiUrl}/api/portfolios/${portfolioId}/assets`,
    request
  )
  return data
}

export async function removeAsset(portfolioId: string, assetId: string): Promise<void> {
  await apiClient.delete(`${env.portfolioApiUrl}/api/portfolios/${portfolioId}/assets/${assetId}`)
}

export async function fetchTransactions(portfolioId: string): Promise<Transaction[]> {
  const { data } = await apiClient.get<Transaction[]>(
    `${env.portfolioApiUrl}/api/portfolios/${portfolioId}/transactions`
  )
  return data
}

export async function createTransaction(
  portfolioId: string,
  request: TransactionRequest
): Promise<Transaction> {
  const { data } = await apiClient.post<Transaction>(
    `${env.portfolioApiUrl}/api/portfolios/${portfolioId}/transactions`,
    request
  )
  return data
}
