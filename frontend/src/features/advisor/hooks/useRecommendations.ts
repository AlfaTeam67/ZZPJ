import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { fetchRecommendations } from '@/features/advisor/api'

export function useRecommendations(
  portfolioId: string | null,
  riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE' = 'MODERATE',
  investmentHorizon: 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM' = 'MID_TERM',
  enabled = false
) {
  const { i18n } = useTranslation()
  const language = i18n.language

  return useQuery({
    queryKey: [
      'advisor',
      'recommendations',
      portfolioId,
      riskTolerance,
      investmentHorizon,
      language,
    ],
    enabled: enabled && portfolioId !== null,
    queryFn: () =>
      fetchRecommendations({
        portfolioId: portfolioId!,
        riskTolerance,
        investmentHorizon,
        language,
      }),
  })
}
