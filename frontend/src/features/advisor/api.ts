import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'

// ---------------------------------------------------------------------------
// Request / Response types matching backend RecommendationRequest DTO (ALF-32)
// ---------------------------------------------------------------------------

export interface RecommendationRequest {
  portfolioId: string
  riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE'
  investmentHorizon: 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM'
}

export interface RecommendationResponse {
  id: string
  portfolioId: string
  summary: string
  fullText: string
  bulletPoints: string[]
  newsContext: Array<{
    id: string
    headline: string
    source: string
    provider: string
    symbol: string
    url: string | null
    sentiment: string | null
  }>
  riskScore: string | null
  modelId: string
  createdAt: string
}

export async function generateRecommendation(
  payload: RecommendationRequest
): Promise<RecommendationResponse> {
  const { data } = await apiClient.post<RecommendationResponse>(
    `${env.advisorApiUrl}/api/recommendations`,
    payload
  )
  return data
}

export async function fetchRecommendation(id: string): Promise<RecommendationResponse> {
  const { data } = await apiClient.get<RecommendationResponse>(
    `${env.advisorApiUrl}/api/recommendations/${id}`
  )
  return data
}

export interface PagedRecommendations {
  content: RecommendationResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function fetchMyRecommendations(
  page = 0,
  size = 10
): Promise<PagedRecommendations> {
  const { data } = await apiClient.get<PagedRecommendations>(
    `${env.advisorApiUrl}/api/recommendations/me`,
    { params: { page, size } }
  )
  return data
}
