import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowUpRight01Icon } from '@hugeicons/core-free-icons'

import { usePortfolioMetric } from '@/features/dashboard/hooks/useDashboard'
import { formatCurrency, formatPercent, formatSignedCurrency } from '@/utils/formatNumber'

export function PortfolioMetricHero() {
  const { data, isLoading } = usePortfolioMetric()

  if (isLoading || !data) {
    return (
      <div className="space-y-3">
        <div className="h-3 w-44 animate-pulse rounded bg-muted" />
        <div className="h-12 w-72 animate-pulse rounded bg-muted" />
        <div className="h-3 w-40 animate-pulse rounded bg-muted" />
      </div>
    )
  }

  const isUp = !data.changeAbsolute.startsWith('-')

  return (
    <section aria-labelledby="portfolio-total-label">
      <p
        id="portfolio-total-label"
        className="text-[11px] font-medium uppercase tracking-[0.2em] text-muted-foreground"
      >
        Całkowita wartość portfela
      </p>
      <h1 className="mt-2 font-heading text-5xl font-semibold tracking-tight">
        {formatCurrency(data.totalValue, data.currency)}
      </h1>
      <div className="mt-3 flex items-center gap-3 text-sm">
        <span
          className={
            isUp
              ? 'flex items-center gap-1 text-success'
              : 'flex items-center gap-1 text-destructive'
          }
        >
          <HugeiconsIcon
            icon={ArrowUpRight01Icon}
            className={isUp ? 'size-4' : 'size-4 rotate-90'}
            aria-hidden
          />
          <span>
            {formatSignedCurrency(data.changeAbsolute, data.currency)} (
            {formatPercent(data.changePercent)})
          </span>
        </span>
        <span className="text-muted-foreground">{data.changeLabel}</span>
      </div>
    </section>
  )
}
