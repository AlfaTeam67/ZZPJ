import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { HugeiconsIcon } from '@hugeicons/react'
import { Briefcase01Icon } from '@hugeicons/core-free-icons'

import { AdvisorRecommendations } from '@/features/advisor/components/AdvisorRecommendations'
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { cn } from '@/lib/utils'

export function AdvisorPage() {
  const { t } = useTranslation('advisor')
  const { data: portfolios, isLoading } = usePortfolios()
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const activeId = selectedId ?? portfolios?.[0]?.id ?? null

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">{t('title')}</h1>
        <p className="mt-1 text-sm text-muted-foreground">{t('subtitle')}</p>
      </div>

      {isLoading ? (
        <div className="flex gap-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-10 w-32 animate-pulse rounded-xl bg-muted/50" />
          ))}
        </div>
      ) : !portfolios || portfolios.length === 0 ? (
        <div className="flex min-h-[200px] items-center justify-center rounded-2xl border border-dashed border-border/40">
          <p className="text-sm text-muted-foreground">{t('empty')}</p>
        </div>
      ) : (
        <>
          <div className="flex flex-wrap gap-2">
            {portfolios.map((p) => (
              <button
                key={p.id}
                onClick={() => setSelectedId(p.id)}
                className={cn(
                  'flex items-center gap-2 rounded-xl border px-4 py-2 text-sm font-medium transition-all duration-150',
                  activeId === p.id
                    ? 'border-primary/50 bg-primary/10 text-primary shadow-sm'
                    : 'border-border/40 bg-card text-muted-foreground hover:border-border hover:text-foreground'
                )}
              >
                <HugeiconsIcon icon={Briefcase01Icon} className="size-3.5" aria-hidden />
                {p.name}
              </button>
            ))}
          </div>

          {activeId && <AdvisorRecommendations portfolioId={activeId} />}
        </>
      )}
    </div>
  )
}
