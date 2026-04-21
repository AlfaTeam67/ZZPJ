import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { usePortfolio } from '@/features/portfolio/hooks/usePortfolio'
import { formatMoney } from '@/utils/formatMoney'

export function PortfolioOverview() {
  const { data, isLoading } = usePortfolio()

  if (isLoading) {
    return (
      <Card>
        <CardContent className="pt-6">Loading portfolio...</CardContent>
      </Card>
    )
  }

  if (!data) {
    return (
      <Card>
        <CardContent className="pt-6">No portfolio data.</CardContent>
      </Card>
    )
  }

  const hasTotals = Object.keys(data.totals).length > 0

  return (
    <Card>
      <CardHeader>
        <CardTitle>Portfolio overview</CardTitle>
        <CardDescription>Multi-currency portfolio breakdown.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant="outline">Portfolio ID: {data.id}</Badge>
          <Badge variant="outline">User ID: {data.userId}</Badge>
        </div>
        <div className="space-y-3 rounded-lg border p-4 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-lg font-semibold">{data.name}</p>
              {data.description && (
                <p className="text-sm text-muted-foreground">{data.description}</p>
              )}
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-muted-foreground">Balances</p>
              <div className="space-y-1">
                {hasTotals ? (
                  Object.entries(data.totals).map(([currency, value]) => (
                    <p key={currency} className="text-xl font-bold">
                      {formatMoney(value, currency)}
                    </p>
                  ))
                ) : (
                  <p className="text-xl font-bold">0.00</p>
                )}
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
