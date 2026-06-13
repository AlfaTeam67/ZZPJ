import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { generateRecommendation, fetchMyRecommendations } from '@/features/advisor/api'
import { fetchFirstPortfolio } from '@/features/portfolio/api'
import type { RecommendationRequest } from '@/features/advisor/api'

/**
 * Hook do listowania ostatnich rekomendacji zalogowanego użytkownika.
 */
export function useMyRecommendations(page = 0) {
  return useQuery({
    queryKey: ['advisor', 'recommendations', 'me', page],
    queryFn: () => fetchMyRecommendations(page, 10),
  })
}

/**
 * Hook do generowania nowej rekomendacji.
 */
export function useGenerateRecommendation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (params: {
      riskTolerance: RecommendationRequest['riskTolerance']
      investmentHorizon: RecommendationRequest['investmentHorizon']
    }) => {
      const portfolio = await fetchFirstPortfolio()
      if (!portfolio) throw new Error('Brak portfela — utwórz portfel przed wygenerowaniem rekomendacji.')

      return generateRecommendation({
        portfolioId: portfolio.id,
        riskTolerance: params.riskTolerance,
        investmentHorizon: params.investmentHorizon,
      })
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['advisor', 'recommendations', 'me'] })
    },
  })
}
