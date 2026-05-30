import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowRight01Icon, Delete02Icon } from '@hugeicons/core-free-icons'

import { formatMoney } from '@/utils/formatMoney'
import type { Portfolio } from '@/types/portfolio/portfolio'

interface PortfolioCardProps {
  portfolio: Portfolio
  onDelete: (id: string) => void
}

export function PortfolioCard({ portfolio, onDelete }: PortfolioCardProps) {
  const { t } = useTranslation('portfolio')

  const assetsCount = portfolio.assets?.length ?? 0

  return (
    <div className="group relative rounded-2xl border border-border/40 bg-card/60 transition-all hover:border-border/70 hover:bg-card/80 hover:shadow-md">
      <Link
        to={`/portfolio/${portfolio.id}`}
        className="block p-6 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 rounded-2xl"
      >
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <h3 className="truncate text-base font-semibold tracking-tight group-hover:text-primary transition-colors">
              {portfolio.name}
            </h3>
            <p className="mt-1 text-sm text-muted-foreground line-clamp-2">
              {portfolio.description || t('no-description')}
            </p>
          </div>
          <HugeiconsIcon
            icon={ArrowRight01Icon}
            className="mt-0.5 size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:text-primary"
            aria-hidden
          />
        </div>

        <div className="mt-4 flex items-end justify-between">
          <div>
            <p className="text-xs text-muted-foreground">{t('total-value', { ns: 'common' })}</p>
            {portfolio.totals && Object.entries(portfolio.totals).length > 0 ? (
              Object.entries(portfolio.totals).map(([currency, total]) => (
                <p key={currency} className="text-xl font-bold tabular-nums">
                  {formatMoney(total, currency)}
                </p>
              ))
            ) : (
              <p className="text-xl font-bold tabular-nums">{formatMoney('0', 'USD')}</p>
            )}
          </div>
          <p className="text-xs text-muted-foreground">
            {t('assets-count', { count: assetsCount })}
          </p>
        </div>
      </Link>

      <button
        type="button"
        onClick={() => {
          if (confirm(t('delete-confirm'))) {
            onDelete(portfolio.id)
          }
        }}
        aria-label={t('delete', { ns: 'common' })}
        className="absolute right-4 top-4 flex size-7 items-center justify-center rounded-md text-muted-foreground opacity-0 transition-opacity hover:bg-destructive/10 hover:text-destructive group-hover:opacity-100"
      >
        <HugeiconsIcon icon={Delete02Icon} className="size-3.5" aria-hidden />
      </button>
    </div>
  )
}
