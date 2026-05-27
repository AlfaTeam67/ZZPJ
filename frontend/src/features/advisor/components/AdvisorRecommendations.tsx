import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useRecommendations } from '@/features/advisor/hooks/useRecommendations'
import { Badge } from '@/components/ui/badge'
import { useLanguage } from '@/i18n/hooks/useLanguage'

const RISK_LEVELS = ['LOW', 'MODERATE', 'HIGH', 'AGGRESSIVE'] as const
const HORIZONS = ['SHORT_TERM', 'MID_TERM', 'LONG_TERM'] as const

const RISK_KEYS = {
  LOW: 'risk-low',
  MODERATE: 'risk-moderate',
  HIGH: 'risk-high',
  AGGRESSIVE: 'risk-aggressive',
} as const

const HORIZON_KEYS = {
  SHORT_TERM: 'horizon-short',
  MID_TERM: 'horizon-mid',
  LONG_TERM: 'horizon-long',
} as const

export function AdvisorRecommendations() {
  const { t } = useTranslation('advisor')
  const { locale } = useLanguage()
  const [risk, setRisk] = useState<(typeof RISK_LEVELS)[number]>('MODERATE')
  const [horizon, setHorizon] = useState<(typeof HORIZONS)[number]>('MID_TERM')
  const { data, isLoading, refetch, isFetching } = useRecommendations(risk, horizon)

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{t('insights-title')}</CardTitle>
            <CardDescription>{t('insights-subtitle')}</CardDescription>
          </div>
          <div className="flex flex-col gap-1">
            <div className="flex gap-2 rounded-md bg-muted p-1">
              {RISK_LEVELS.map((r) => (
                <button
                  key={r}
                  onClick={() => setRisk(r)}
                  className={`rounded-sm px-3 py-1 text-xs transition-colors ${
                    risk === r ? 'bg-background font-medium shadow-sm' : 'hover:bg-background/50'
                  }`}
                >
                  {t(RISK_KEYS[r])}
                </button>
              ))}
            </div>
            <div className="flex gap-2 rounded-md bg-muted p-1">
              {HORIZONS.map((h) => (
                <button
                  key={h}
                  onClick={() => setHorizon(h)}
                  className={`rounded-sm px-3 py-1 text-xs transition-colors ${
                    horizon === h ? 'bg-background font-medium shadow-sm' : 'hover:bg-background/50'
                  }`}
                >
                  {t(HORIZON_KEYS[h])}
                </button>
              ))}
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {!data && !isLoading && !isFetching ? (
          <div className="rounded-lg border-2 border-dashed py-10 text-center text-muted-foreground">
            {t('empty')}
          </div>
        ) : isLoading || isFetching ? (
          <div className="animate-pulse py-10 text-center text-muted-foreground">
            {t('analyzing')}
          </div>
        ) : data ? (
          <>
            <div className="mb-2 flex items-center gap-2">
              <Badge variant="secondary">
                {t('risk-score', { score: data.riskScore?.toFixed(1) ?? 'N/A' })}
              </Badge>
              <span className="text-xs text-muted-foreground">
                {data.modelId} · {new Date(data.createdAt).toLocaleString(locale)}
              </span>
            </div>
            {data.summary && <p className="text-sm italic text-muted-foreground">{data.summary}</p>}
            <ul className="grid gap-3">
              {data.bulletPoints?.map((text, idx) => (
                <li
                  key={idx}
                  className="flex items-start gap-3 rounded-lg border bg-card p-3 transition-colors hover:bg-accent/50"
                >
                  <div className="flex size-6 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                    {idx + 1}
                  </div>
                  <p className="text-sm leading-relaxed">{text}</p>
                </li>
              ))}
            </ul>
          </>
        ) : (
          <div className="rounded-lg border-2 border-dashed py-10 text-center">
            <p className="text-muted-foreground">{t('no-recommendations')}</p>
          </div>
        )}

        <Button className="w-full" onClick={() => refetch()} disabled={isLoading || isFetching}>
          {isLoading || isFetching ? t('refreshing') : t('refresh')}
        </Button>
      </CardContent>
    </Card>
  )
}
