import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'

export interface RecommendationRequest {
  portfolioId: string
  riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE'
  investmentHorizon: 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM'
}

export interface NewsItem {
  id: string
  headline: string
  source: string
  provider: string
  symbol: string
  url: string
  sentiment: string
}

export interface RecommendationResponse {
  id: string
  portfolioId: string
  summary: string
  fullText: string
  bulletPoints: string[]
  newsContext: NewsItem[]
  riskScore: number
  modelId: string
  createdAt: string
}

export async function fetchRecommendations(
  payload: RecommendationRequest
): Promise<RecommendationResponse> {
  const { data } = await apiClient.post<RecommendationResponse>(
    `${env.apiUrl}/api/recommendations`,
    payload
  )
  return data
}
