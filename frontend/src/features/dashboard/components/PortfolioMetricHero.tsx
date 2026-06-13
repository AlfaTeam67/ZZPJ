import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowUpRight01Icon, Briefcase01Icon } from '@hugeicons/core-free-icons'
import { Link } from 'react-router-dom'

import { usePortfolioMetric } from '@/features/dashboard/hooks/useDashboard'
import { formatCurrency, formatPercent, formatSignedCurrency } from '@/utils/formatNumber'
import { Button } from '@/components/ui/button'

export function PortfolioMetricHero() {
  const { data, isLoading, isError } = usePortfolioMetric()

  if (isLoading) {
    return (
      <div className="space-y-3" aria-busy="true">
        <div className="h-3 w-44 animate-pulse rounded bg-muted" />
        <div className="h-12 w-72 animate-pulse rounded bg-muted" />
        <div className="h-3 w-40 animate-pulse rounded bg-muted" />
      </div>
    )
  }

  if (isError) {
    return (
      <section className="space-y-2">
        <p className="text-sm text-destructive">Nie udało się pobrać danych portfela.</p>
      </section>
    )
  }

  const totalValue = data?.totalValue ?? '0'
  const isEmpty = totalValue === '0' && data?.changeLabel === 'Brak portfela'

  if (isEmpty) {
    return (
      <section className="flex flex-col gap-4 rounded-2xl border border-dashed border-border/50 bg-card/30 p-8">
        <div className="flex items-center gap-3 text-muted-foreground">
          <HugeiconsIcon icon={Briefcase01Icon} className="size-8" aria-hidden />
          <div>
            <p className="text-base font-semibold text-foreground">Brak portfela</p>
            <p className="text-sm">Utwórz pierwszy portfel, aby zobaczyć swoje dane.</p>
          </div>
        </div>
        <Button asChild size="sm" className="w-fit">
          <Link to="/portfolio">Utwórz portfel</Link>
        </Button>
      </section>
    )
  }

  const isUp = !data!.changeAbsolute.startsWith('-') && data!.changeAbsolute !== '0'

  return (
    <section aria-labelledby="portfolio-total-label">
      <p
        id="portfolio-total-label"
        className="text-[11px] font-medium uppercase tracking-[0.2em] text-muted-foreground"
      >
        Całkowita wartość portfela
      </p>
      <h1 className="mt-2 font-heading text-5xl font-semibold tracking-tight">
        {formatCurrency(data!.totalValue, data!.currency)}
      </h1>
      <div className="mt-3 flex items-center gap-3 text-sm">
        {data!.changeAbsolute !== '0' ? (
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
              {formatSignedCurrency(data!.changeAbsolute, data!.currency)} (
              {formatPercent(data!.changePercent)})
            </span>
          </span>
        ) : null}
        <span className="text-muted-foreground">{data!.changeLabel}</span>
      </div>
    </section>
  )
}
