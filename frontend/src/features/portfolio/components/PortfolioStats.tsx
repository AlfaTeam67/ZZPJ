import { useTranslation } from 'react-i18next'
import Decimal from 'decimal.js'

import { formatMoney } from '@/utils/formatMoney'
import type { Portfolio } from '@/types/portfolio/portfolio'

interface PortfolioStatsProps {
  portfolios: Portfolio[]
}

export function PortfolioStats({ portfolios }: PortfolioStatsProps) {
  const { t } = useTranslation('portfolio')

  const totalAssets = portfolios.reduce((sum, p) => sum + (p.assets?.length ?? 0), 0)

  // Aggregate totals across all portfolios by currency
  const aggregated: Record<string, Decimal> = {}
  for (const p of portfolios) {
    if (p.totals) {
      for (const [currency, value] of Object.entries(p.totals)) {
        const current = aggregated[currency] ?? new Decimal(0)
        aggregated[currency] = current.plus(new Decimal(value))
      }
    }
  }

  const entries = Object.entries(aggregated)

  return (
    <div className="grid gap-4 sm:grid-cols-3">
      <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {t('kpi-total')}
        </p>
        <div className="mt-2">
          {entries.length > 0 ? (
            entries.map(([currency, value]) => (
              <p key={currency} className="text-2xl font-bold tabular-nums">
                {formatMoney(value.toString(), currency)}
              </p>
            ))
          ) : (
            <p className="text-2xl font-bold tabular-nums">{formatMoney('0', 'USD')}</p>
          )}
        </div>
      </div>
      <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {t('kpi-portfolios')}
        </p>
        <p className="mt-2 text-2xl font-bold tabular-nums">{portfolios.length}</p>
      </div>
      <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {t('kpi-assets')}
        </p>
        <p className="mt-2 text-2xl font-bold tabular-nums">{totalAssets}</p>
      </div>
    </div>
  )
}
