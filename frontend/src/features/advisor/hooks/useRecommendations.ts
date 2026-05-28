import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { fetchRecommendations } from '@/features/advisor/api'
import { fetchFirstPortfolio } from '@/features/portfolio/api'

export function useRecommendations(
  riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE' = 'MODERATE',
  investmentHorizon: 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM' = 'MID_TERM'
) {
  const { i18n } = useTranslation()
  const language = i18n.language

  return useQuery({
    queryKey: ['advisor', 'recommendations', riskTolerance, investmentHorizon, language],
    queryFn: async () => {
      const portfolio = await fetchFirstPortfolio()
      if (!portfolio) return null

      return fetchRecommendations({
        portfolioId: portfolio.id,
        riskTolerance,
        investmentHorizon,
        language,
      })
    },
  })
}
