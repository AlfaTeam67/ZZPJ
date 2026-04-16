import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { usePortfolio } from '@/features/portfolio/hooks/usePortfolio'

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

  return (
    <Card>
      <CardHeader>
        <CardTitle>Portfolio overview</CardTitle>
        <CardDescription>Backend-aligned portfolio snapshot.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex flex-wrap items-center gap-2">
          <Badge>Portfolio ID: {data.id}</Badge>
          <Badge variant="secondary">User ID: {data.userId}</Badge>
        </div>
        <div className="space-y-2 rounded-lg border px-3 py-2">
          <p className="font-medium">{data.name}</p>
          <p className="text-sm text-muted-foreground">Total value: {data.totalValue}</p>
          {data.description ? (
            <p className="text-sm text-muted-foreground">{data.description}</p>
          ) : null}
        </div>
      </CardContent>
    </Card>
  )
}
