import { useTranslation } from 'react-i18next'

import { PortfolioList } from '@/features/portfolio/components/PortfolioList'
import { PortfolioStats } from '@/features/portfolio/components/PortfolioStats'
import { CreatePortfolioForm } from '@/features/portfolio/components/CreatePortfolioForm'
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { SectionSkeleton } from '@/components/ui/SectionSkeleton'

export function PortfolioPage() {
  const { t } = useTranslation('portfolio')
  const { data: portfolios, isLoading, error } = usePortfolios()

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8">
      <section>
        <h1 className="text-3xl font-bold tracking-tight">{t('title')}</h1>
        <p className="mt-1 text-sm text-muted-foreground">{t('subtitle')}</p>
      </section>

      {isLoading ? (
        <SectionSkeleton lines={4} />
      ) : error ? (
        <div className="rounded-2xl border border-destructive/40 bg-destructive/5 p-6 text-center">
          <p className="text-sm font-medium text-destructive">{t('error')}</p>
        </div>
      ) : (
        <>
          {portfolios && portfolios.length > 0 && <PortfolioStats portfolios={portfolios} />}

          <div className="grid gap-8 lg:grid-cols-[1fr,350px]">
            <PortfolioList />
            <aside className="lg:sticky lg:top-8 lg:self-start">
              <CreatePortfolioForm />
            </aside>
          </div>
        </>
      )}
    </div>
  )
}
