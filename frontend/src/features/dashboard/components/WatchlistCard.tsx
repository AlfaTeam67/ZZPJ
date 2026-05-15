import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowRight01Icon } from '@hugeicons/core-free-icons'
import { Link } from 'react-router-dom'

import { useWatchlist } from '@/features/dashboard/hooks/useDashboard'
import { cn } from '@/lib/utils'
import { formatCurrency, formatPercent } from '@/utils/formatNumber'

export function WatchlistCard() {
  const { data, isLoading } = useWatchlist()

  return (
    <section
      aria-labelledby="watchlist-title"
      className="rounded-2xl border border-border/40 bg-card/60 p-6"
    >
      <header className="flex items-center justify-between">
        <h2 id="watchlist-title" className="text-base font-semibold">
          Twoje akcje
        </h2>
        <Link
          to="/portfolio"
          className="flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
        >
          Zobacz wszystkie
          <HugeiconsIcon icon={ArrowRight01Icon} className="size-3" aria-hidden />
        </Link>
      </header>

      <ul className="mt-5 divide-y divide-border/30">
        {isLoading
          ? Array.from({ length: 5 }).map((_, i) => (
              <li key={i} className="flex items-center justify-between py-3">
                <div className="space-y-2">
                  <div className="h-3 w-20 animate-pulse rounded bg-muted" />
                  <div className="h-2 w-24 animate-pulse rounded bg-muted/70" />
                </div>
                <div className="space-y-2 text-right">
                  <div className="ml-auto h-3 w-24 animate-pulse rounded bg-muted" />
                  <div className="ml-auto h-2 w-16 animate-pulse rounded bg-muted/70" />
                </div>
              </li>
            ))
          : data?.map((row) => {
              const isUp = !row.changePercent.startsWith('-')
              return (
                <li key={row.symbol} className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-semibold tracking-tight">{row.symbol}</p>
                    <p className="text-xs text-muted-foreground">{row.name}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold tabular-nums">
                      {formatCurrency(row.value, row.currency)}
                    </p>
                    <p
                      className={cn(
                        'text-xs tabular-nums',
                        isUp ? 'text-success' : 'text-destructive'
                      )}
                    >
                      {formatPercent(row.changePercent)}
                    </p>
                  </div>
                </li>
              )
            })}
      </ul>
    </section>
  )
}
