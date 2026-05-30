import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { Button } from '@/components/ui/button'
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
    <div className="rounded-2xl border border-border/40 bg-card/60 p-6 transition-colors hover:bg-card/80">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <Link to={`/portfolio/${portfolio.id}`} className="hover:underline">
            <h3 className="truncate text-base font-semibold tracking-tight">{portfolio.name}</h3>
          </Link>
          <p className="mt-1 text-sm text-muted-foreground">
            {portfolio.description || t('no-description')}
          </p>
        </div>
        <div className="flex shrink-0 gap-2">
          <Button asChild variant="outline" size="sm">
            <Link to={`/portfolio/${portfolio.id}`}>{t('details', { ns: 'common' })}</Link>
          </Button>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => {
              if (confirm(t('delete-confirm'))) {
                onDelete(portfolio.id)
              }
            }}
          >
            {t('delete', { ns: 'common' })}
          </Button>
        </div>
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
        <p className="text-xs text-muted-foreground">{t('assets-count', { count: assetsCount })}</p>
      </div>
    </div>
  )
}
