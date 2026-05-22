import { usePortfolios } from '../hooks/usePortfolios'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatMoney } from '@/utils/formatMoney'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deletePortfolio } from '../api'
import { Link } from 'react-router-dom'

export function PortfolioList() {
  const { data: portfolios, isLoading } = usePortfolios()
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deletePortfolio(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
    },
  })

  if (isLoading) return <div>Loading portfolios...</div>

  if (!portfolios || portfolios.length === 0) {
    return (
      <div className="text-center py-10 border-2 border-dashed rounded-lg">
        <p className="text-muted-foreground">No portfolios found. Create your first one!</p>
      </div>
    )
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-2">
      {portfolios.map((portfolio) => (
        <Card key={portfolio.id} className="flex flex-col">
          <CardHeader>
            <div className="flex justify-between items-start">
              <div>
                <Link to={`/portfolio/${portfolio.id}`} className="hover:underline">
                  <CardTitle>{portfolio.name}</CardTitle>
                </Link>
                <CardDescription>{portfolio.description || 'No description'}</CardDescription>
              </div>
              <div className="flex gap-2">
                <Button asChild variant="outline" size="sm">
                  <Link to={`/portfolio/${portfolio.id}`}>Details</Link>
                </Button>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => {
                    if (confirm('Are you sure you want to delete this portfolio?')) {
                      deleteMutation.mutate(portfolio.id)
                    }
                  }}
                >
                  Delete
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent className="mt-auto">
            <div className="space-y-2">
              <p className="text-sm font-medium text-muted-foreground">Total Value</p>
              {portfolio.totals && Object.entries(portfolio.totals).length > 0 ? (
                Object.entries(portfolio.totals).map(([currency, total]) => (
                  <p key={currency} className="text-2xl font-bold">
                    {formatMoney(total, currency)}
                  </p>
                ))
              ) : (
                <p className="text-2xl font-bold">{formatMoney('0', 'USD')}</p>
              )}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
