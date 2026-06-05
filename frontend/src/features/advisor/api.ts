import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'
import type { Recommendation, RecommendationRequest } from '@/types/advisor'

export type { RecommendationRequest } from '@/types/advisor'

export async function fetchRecommendations(
  payload: RecommendationRequest
): Promise<Recommendation> {
  const { data } = await apiClient.post<Recommendation>(
    `${env.advisorApiUrl}/api/recommendations`,
    payload
  )
  return data
}
