import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowUpRight01Icon, ArrowDownRight01Icon } from '@hugeicons/core-free-icons'
import Decimal from 'decimal.js'

import { usePortfolioMetric } from '@/features/dashboard/hooks/useDashboard'
import { formatCurrency, formatPercent, formatSignedCurrency } from '@/utils/formatNumber'

/**
 * Hero z gigantyczną wartością portfela u góry kokpitu.
 * Idzie 1:1 z designem - nagłówek caps + wartość + delta dzienna.
 */
export function PortfolioHeroValue() {
  const { data, isLoading } = usePortfolioMetric()

  if (isLoading || !data) {
    return (
      <div className="space-y-3">
        <p className="text-xs uppercase tracking-[0.25em] text-muted-foreground">
          Całkowita wartość portfela
        </p>
        <div className="h-12 w-72 animate-pulse rounded-md bg-muted/40" />
        <div className="h-4 w-48 animate-pulse rounded-md bg-muted/30" />
      </div>
    )
  }

  const change = new Decimal(data.changePercent)
  const isUp = change.greaterThanOrEqualTo(0)
  const trendColor = isUp ? 'text-success' : 'text-destructive'

  return (
    <section aria-labelledby="hero-title" className="space-y-3">
      <p
        id="hero-title"
        className="text-xs uppercase tracking-[0.25em] text-muted-foreground"
      >
        Całkowita wartość portfela
      </p>
      <p className="font-heading text-5xl font-semibold tracking-tight md:text-6xl">
        {formatCurrency(data.totalValue, data.currency)}
      </p>
      <div className="flex flex-wrap items-center gap-3 text-sm">
        <span className={`inline-flex items-center gap-1 font-medium ${trendColor}`}>
          <HugeiconsIcon
            icon={isUp ? ArrowUpRight01Icon : ArrowDownRight01Icon}
            className="size-4"
            aria-hidden
          />
          {formatSignedCurrency(data.changeAbsolute, data.currency)} ({formatPercent(data.changePercent)})
        </span>
        <span className="text-muted-foreground">{data.changeLabel}</span>
      </div>
    </section>
  )
}
