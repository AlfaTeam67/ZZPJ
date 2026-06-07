import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useRecommendations } from '@/features/advisor/hooks/useRecommendations'
import { useLanguage } from '@/i18n/hooks/useLanguage'
import { detectSignal, SIGNAL_STYLES } from '@/utils/bulletSignal'
import { cn } from '@/lib/utils'

const RISK_LEVELS = ['LOW', 'MODERATE', 'HIGH', 'AGGRESSIVE'] as const
const HORIZONS = ['SHORT_TERM', 'MID_TERM', 'LONG_TERM'] as const

const RISK_KEYS = {
  LOW: 'risk-low',
  MODERATE: 'risk-moderate',
  HIGH: 'risk-high',
  AGGRESSIVE: 'risk-aggressive',
} as const

const RISK_DESC_KEYS = {
  LOW: 'risk-low-desc',
  MODERATE: 'risk-moderate-desc',
  HIGH: 'risk-high-desc',
  AGGRESSIVE: 'risk-aggressive-desc',
} as const

const HORIZON_KEYS = {
  SHORT_TERM: 'horizon-short',
  MID_TERM: 'horizon-mid',
  LONG_TERM: 'horizon-long',
} as const

const HORIZON_DESC_KEYS = {
  SHORT_TERM: 'horizon-short-desc',
  MID_TERM: 'horizon-mid-desc',
  LONG_TERM: 'horizon-long-desc',
} as const

interface AdvisorRecommendationsProps {
  portfolioId: string
}

export function AdvisorRecommendations({ portfolioId }: AdvisorRecommendationsProps) {
  const { t } = useTranslation('advisor')
  const { locale } = useLanguage()
  const [risk, setRisk] = useState<(typeof RISK_LEVELS)[number]>('MODERATE')
  const [horizon, setHorizon] = useState<(typeof HORIZONS)[number]>('MID_TERM')
  const [enabled, setEnabled] = useState(false)
  const { data, isLoading, refetch, isFetching } = useRecommendations(
    portfolioId,
    risk,
    horizon,
    enabled
  )

  const handleGenerate = () => {
    if (!enabled) {
      setEnabled(true)
    } else {
      void refetch()
    }
  }

  const handleFilterChange = <T,>(setter: (v: T) => void, value: T) => {
    setter(value)
    setEnabled(false)
  }

  const busy = isLoading || isFetching

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('insights-title')}</CardTitle>
        <CardDescription>{t('insights-subtitle')}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {t('risk-label')}
            </p>
            <p className="text-xs text-muted-foreground">{t(RISK_DESC_KEYS[risk])}</p>
            <div className="flex flex-wrap gap-1 rounded-lg bg-muted p-1">
              {RISK_LEVELS.map((r) => (
                <button
                  key={r}
                  onClick={() => handleFilterChange(setRisk, r)}
                  className={`rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                    risk === r
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-background/50'
                  }`}
                >
                  {t(RISK_KEYS[r])}
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {t('horizon-label')}
            </p>
            <p className="text-xs text-muted-foreground">{t(HORIZON_DESC_KEYS[horizon])}</p>
            <div className="flex flex-wrap gap-1 rounded-lg bg-muted p-1">
              {HORIZONS.map((h) => (
                <button
                  key={h}
                  onClick={() => handleFilterChange(setHorizon, h)}
                  className={`rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                    horizon === h
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-background/50'
                  }`}
                >
                  {t(HORIZON_KEYS[h])}
                </button>
              ))}
            </div>
          </div>
        </div>

        <Button className="w-full" onClick={handleGenerate} disabled={busy}>
          {busy ? t('refreshing') : enabled ? t('refresh') : t('generate')}
        </Button>

        {busy ? (
          <div className="animate-pulse py-10 text-center text-muted-foreground">
            {t('analyzing')}
          </div>
        ) : data ? (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Badge variant="secondary">
                {t('risk-score', { score: data.riskScore?.toFixed(1) ?? 'N/A' })}
              </Badge>
              <span className="text-xs text-muted-foreground">
                {data.modelId} · {new Date(data.createdAt).toLocaleString(locale)}
              </span>
            </div>
            {data.summary && <p className="text-sm italic text-muted-foreground">{data.summary}</p>}
            <ul className="grid gap-3">
              {data.bulletPoints?.map((text, idx) => {
                const signal = detectSignal(text)
                const styles = signal ? SIGNAL_STYLES[signal] : null
                const displayText = text.replace(/^\s*\[(BUY|HOLD|SELL)\]\s*/i, '')
                return (
                  <li
                    key={idx}
                    className={cn(
                      'rounded-xl border p-3',
                      styles ? styles.card : 'bg-card border-border/40'
                    )}
                  >
                    <div className="flex items-start gap-2.5">
                      {styles ? (
                        <span className={cn('mt-0.5 shrink-0 rounded-md px-2 py-0.5 text-[11px] font-extrabold tracking-wider border', styles.badge,
                          signal === 'BUY'  && 'border-green-500/50 bg-green-500/15',
                          signal === 'SELL' && 'border-red-500/50 bg-red-500/15',
                          signal === 'HOLD' && 'border-yellow-500/50 bg-yellow-500/15',
                        )}>
                          {styles.label}
                        </span>
                      ) : (
                        <span className="mt-0.5 flex size-5 shrink-0 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary">
                          {idx + 1}
                        </span>
                      )}
                      <p className="text-sm leading-relaxed">{displayText}</p>
                    </div>
                  </li>
                )
              })}
            </ul>
            {data.newsContext && data.newsContext.length > 0 && (
              <div className="space-y-2 pt-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {t('sources')}
                </p>
                <ul className="space-y-1">
                  {data.newsContext.slice(0, 4).map((item) => (
                    <li key={item.id}>
                      <a
                        href={item.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-baseline gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
                      >
                        <span className="shrink-0 font-semibold text-foreground/70">{item.symbol}</span>
                        <span className="truncate">{item.headline}</span>
                        <span className="shrink-0 text-muted-foreground/50">— {item.source}</span>
                      </a>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        ) : enabled ? null : (
          <div className="rounded-xl border-2 border-dashed border-border/40 py-12 text-center">
            <p className="text-sm text-muted-foreground">{t('prompt')}</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
