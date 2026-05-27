import { useTranslation } from 'react-i18next'

import { AdvisorRecommendations } from '@/features/advisor/components/AdvisorRecommendations'

export function AdvisorPage() {
  const { t } = useTranslation('advisor')

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">{t('title')}</h1>
      <AdvisorRecommendations />
    </div>
  )
}
