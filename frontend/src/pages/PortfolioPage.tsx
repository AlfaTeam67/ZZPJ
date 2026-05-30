import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { HugeiconsIcon } from '@hugeicons/react'
import { Add01Icon, Cancel01Icon } from '@hugeicons/core-free-icons'

import { PortfolioList } from '@/features/portfolio/components/PortfolioList'
import { PortfolioStats } from '@/features/portfolio/components/PortfolioStats'
import { CreatePortfolioForm } from '@/features/portfolio/components/CreatePortfolioForm'
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { SectionSkeleton } from '@/components/ui/SectionSkeleton'
import { Button } from '@/components/ui/button'

export function PortfolioPage() {
  const { t } = useTranslation('portfolio')
  const { data: portfolios, isLoading, error } = usePortfolios()
  const [showCreate, setShowCreate] = useState(false)

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8">
      <section className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t('title')}</h1>
          <p className="mt-1 text-sm text-muted-foreground">{t('subtitle')}</p>
        </div>
        <Button
          variant={showCreate ? 'outline' : 'default'}
          size="sm"
          className="mt-1 shrink-0"
          onClick={() => setShowCreate((v) => !v)}
        >
          <HugeiconsIcon
            icon={showCreate ? Cancel01Icon : Add01Icon}
            className="size-4"
            aria-hidden
          />
          {showCreate ? t('create-cancel') : t('create-title')}
        </Button>
      </section>

      {showCreate && (
        <div className="rounded-2xl border border-border/40 bg-card/30 p-6">
          <CreatePortfolioForm onSuccess={() => setShowCreate(false)} />
        </div>
      )}

      {isLoading ? (
        <SectionSkeleton lines={4} />
      ) : error ? (
        <div className="rounded-2xl border border-destructive/40 bg-destructive/5 p-6 text-center">
          <p className="text-sm font-medium text-destructive">{t('error')}</p>
        </div>
      ) : (
        <>
          {portfolios && portfolios.length > 0 && <PortfolioStats portfolios={portfolios} />}
          <PortfolioList />
        </>
      )}
    </div>
  )
}
