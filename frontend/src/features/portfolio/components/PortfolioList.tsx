import { useTranslation } from 'react-i18next'
import { useMutation, useQueryClient } from '@tanstack/react-query'

import { usePortfolios } from '../hooks/usePortfolios'
import { deletePortfolio } from '../api'
import { PortfolioCard } from './PortfolioCard'
import { SectionSkeleton } from '@/components/ui/SectionSkeleton'

export function PortfolioList() {
  const { data: portfolios, isLoading } = usePortfolios()
  const { t } = useTranslation('portfolio')
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deletePortfolio(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
    },
  })

  if (isLoading) return <SectionSkeleton lines={4} />

  if (!portfolios || portfolios.length === 0) {
    return (
      <div className="rounded-2xl border-2 border-dashed border-border/40 py-10 text-center">
        <p className="text-sm text-muted-foreground">{t('no-portfolios')}</p>
      </div>
    )
  }

  return (
    <div className="grid gap-4">
      {portfolios.map((portfolio) => (
        <PortfolioCard
          key={portfolio.id}
          portfolio={portfolio}
          onDelete={(id) => deleteMutation.mutate(id)}
        />
      ))}
    </div>
  )
}
