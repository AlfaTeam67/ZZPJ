import type { Recommendation, NewsItem } from '@/types/advisor'
import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'

export interface RecommendationRequest {
  userId: string
  portfolioId?: string
  riskTolerance: 'LOW' | 'MEDIUM' | 'HIGH'
  includeNews?: boolean
}

export interface RecommendationResponse {
  recommendations: string[]
  confidence: string
  timestamp: string
}

export async function fetchRecommendations(payload: RecommendationRequest): Promise<RecommendationResponse> {
  const { data } = await apiClient.post<RecommendationResponse>(`${env.advisorApiUrl}/api/recommendations`, payload)
  return data
}
