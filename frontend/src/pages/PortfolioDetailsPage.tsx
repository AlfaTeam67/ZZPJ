import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { HugeiconsIcon } from '@hugeicons/react'
import { Add01Icon, Cancel01Icon, ChartLineData02Icon } from '@hugeicons/core-free-icons'

import { fetchPortfolio } from '@/features/portfolio/api'
import { AssetList } from '@/features/portfolio/components/AssetList'
import { AddAssetForm } from '@/features/portfolio/components/AddAssetForm'
import { TransactionHistory } from '@/features/portfolio/components/TransactionHistory'
import { TransactionForm } from '@/features/portfolio/components/TransactionForm'
import { SectionSkeleton } from '@/components/ui/SectionSkeleton'
import { Button } from '@/components/ui/button'
import { formatMoney } from '@/utils/formatMoney'
import type { Portfolio } from '@/types/portfolio/portfolio'
import { useLanguage } from '@/i18n/hooks/useLanguage'

export function PortfolioDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const { t } = useTranslation('portfolio')
  const { locale } = useLanguage()
  const [showAddAsset, setShowAddAsset] = useState(false)
  const [showAddTx, setShowAddTx] = useState(false)

  const {
    data: portfolio,
    isLoading,
    error,
  } = useQuery<Portfolio>({
    queryKey: ['portfolio', id],
    queryFn: () => fetchPortfolio(id!),
    enabled: !!id,
  })

  if (isLoading) {
    return (
      <div className="mx-auto max-w-6xl space-y-6">
        <SectionSkeleton lines={2} />
        <div className="grid gap-4 sm:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-24 animate-pulse rounded-2xl bg-muted" />
          ))}
        </div>
        <SectionSkeleton lines={5} />
      </div>
    )
  }

  if (error || !portfolio) {
    return (
      <div className="mx-auto max-w-6xl rounded-2xl border border-destructive/40 bg-destructive/5 p-8 text-center">
        <p className="text-sm font-medium text-destructive">{t('error')}</p>
        <Link to="/portfolio" className="mt-4 inline-block text-sm text-primary hover:underline">
          {t('back-to-list')}
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8">
      <section>
        <Link
          to="/portfolio"
          className="text-sm text-muted-foreground transition-colors hover:text-primary"
        >
          {t('back-to-list')}
        </Link>
        <h1 className="mt-2 text-3xl font-bold tracking-tight">{portfolio.name}</h1>
        {portfolio.description && (
          <p className="mt-1 text-sm text-muted-foreground">{portfolio.description}</p>
        )}
      </section>

      <div className="grid gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            {t('total-value', { ns: 'common' })}
          </p>
          <div className="mt-2">
            {portfolio.totals && Object.entries(portfolio.totals).length > 0 ? (
              Object.entries(portfolio.totals).map(([currency, total]) => (
                <p key={currency} className="text-2xl font-bold tabular-nums">
                  {formatMoney(total, currency)}
                </p>
              ))
            ) : (
              <p className="text-2xl font-bold tabular-nums">{formatMoney('0', 'USD')}</p>
            )}
          </div>
        </div>
        <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            {t('kpi-assets')}
          </p>
          <p className="mt-2 text-2xl font-bold tabular-nums">{portfolio.assets?.length ?? 0}</p>
        </div>
        <div className="rounded-2xl border border-border/40 bg-card/60 p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            {t('kpi-portfolios')}
          </p>
          <p className="mt-2 text-2xl font-bold tabular-nums">
            {portfolio.createdAt ? new Date(portfolio.createdAt).toLocaleDateString(locale) : '—'}
          </p>
        </div>
      </div>

      <div className="space-y-6">
        <div className="space-y-4">
          <div className="flex items-center justify-between gap-4">
            <h2 className="text-lg font-semibold">{t('assets-title')}</h2>
            <Button
              size="sm"
              variant={showAddAsset ? 'outline' : 'default'}
              onClick={() => {
                setShowAddAsset((v) => !v)
                setShowAddTx(false)
              }}
            >
              <HugeiconsIcon
                icon={showAddAsset ? Cancel01Icon : Add01Icon}
                className="size-4"
                aria-hidden
              />
              {showAddAsset ? t('create-cancel') : t('add-asset-title')}
            </Button>
          </div>

          {showAddAsset && (
            <div className="rounded-2xl border border-border/40 bg-card/30 p-6">
              <AddAssetForm
                portfolioId={portfolio.id}
                onSuccess={() => setShowAddAsset(false)}
              />
            </div>
          )}

          <AssetList portfolioId={portfolio.id} assets={portfolio.assets || []} />
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between gap-4">
            <h2 className="text-lg font-semibold">{t('transactions-title')}</h2>
            <Button
              size="sm"
              variant={showAddTx ? 'outline' : 'secondary'}
              onClick={() => {
                setShowAddTx((v) => !v)
                setShowAddAsset(false)
              }}
            >
              <HugeiconsIcon
                icon={showAddTx ? Cancel01Icon : ChartLineData02Icon}
                className="size-4"
                aria-hidden
              />
              {showAddTx ? t('create-cancel') : t('tx-title')}
            </Button>
          </div>

          {showAddTx && (
            <div className="rounded-2xl border border-border/40 bg-card/30 p-6">
              <TransactionForm
                portfolioId={portfolio.id}
                onSuccess={() => setShowAddTx(false)}
              />
            </div>
          )}

          <TransactionHistory portfolioId={portfolio.id} />
        </div>
      </div>
    </div>
  )
}
