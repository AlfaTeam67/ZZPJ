import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchPortfolio } from '@/features/portfolio/api'
import { AssetList } from '@/features/portfolio/components/AssetList'
import { AddAssetForm } from '@/features/portfolio/components/AddAssetForm'
import { TransactionHistory } from '@/features/portfolio/components/TransactionHistory'
import { TransactionForm } from '@/features/portfolio/components/TransactionForm'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatMoney } from '@/utils/formatMoney'
import type { Portfolio } from '@/types/portfolio/portfolio'

export function PortfolioDetailsPage() {
  const { id } = useParams<{ id: string }>()

  const {
    data: portfolio,
    isLoading,
    error,
  } = useQuery<Portfolio>({
    queryKey: ['portfolio', id],
    queryFn: () => fetchPortfolio(id!),
    enabled: !!id,
  })

  if (isLoading) return <div className="p-8 text-center">Loading portfolio details...</div>

  if (error || !portfolio) {
    return (
      <div className="p-8 text-center">
        <p className="text-destructive font-semibold">Error loading portfolio.</p>
        <Link to="/portfolio" className="text-primary hover:underline mt-4 inline-block">
          Back to Portfolios
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <Link
            to="/portfolio"
            className="text-sm text-muted-foreground hover:text-primary mb-2 inline-block"
          >
            ← Back to Portfolios
          </Link>
          <h1 className="text-3xl font-bold tracking-tight">{portfolio.name}</h1>
          {portfolio.description && (
            <p className="text-muted-foreground">{portfolio.description}</p>
          )}
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-4">
        {portfolio.totals &&
          Object.entries(portfolio.totals).map(([currency, total]) => (
            <Card key={currency}>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Total Value ({currency})
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{formatMoney(total, currency)}</div>
              </CardContent>
            </Card>
          ))}
        {(!portfolio.totals || Object.keys(portfolio.totals).length === 0) && (
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Total Value
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatMoney('0', 'USD')}</div>
            </CardContent>
          </Card>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr,350px]">
        <div className="space-y-6">
          <AssetList portfolioId={portfolio.id} assets={portfolio.assets || []} />
          <TransactionHistory portfolioId={portfolio.id} />
        </div>
        <aside className="space-y-6">
          <AddAssetForm portfolioId={portfolio.id} />
          <TransactionForm portfolioId={portfolio.id} />
        </aside>
      </div>
    </div>
  )
}
