import { type Asset } from '@/types/portfolio/asset'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { formatMoney } from '@/utils/formatMoney'
import { Button } from '@/components/ui/button'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { removeAsset } from '../api'

interface AssetListProps {
  portfolioId: string
  assets: Asset[]
}

export function AssetList({ portfolioId, assets }: AssetListProps) {
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: (assetId: string) => removeAsset(portfolioId, assetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
    },
  })

  if (assets.length === 0) {
    return (
      <Card>
        <CardContent className="py-6 text-center text-muted-foreground">
          No assets in this portfolio.
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Assets</h3>
      <div className="grid gap-4">
        {assets.map((asset) => (
          <Card key={asset.id}>
            <CardContent className="flex items-center justify-between p-4">
              <div className="flex items-center gap-4">
                <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center font-bold text-primary">
                  {asset.symbol.substring(0, 2).toUpperCase()}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold">{asset.symbol}</span>
                    <Badge variant="secondary">{asset.type}</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {asset.quantity} units @ {formatMoney(asset.avgBuyPrice, asset.currency)} avg
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="text-right">
                  <p className="font-bold">
                    {formatMoney((parseFloat(asset.quantity) * parseFloat(asset.avgBuyPrice)).toString(), asset.currency)}
                  </p>
                  <p className="text-xs text-muted-foreground">Total Invested</p>
                </div>
                <Button 
                  variant="ghost" 
                  size="sm" 
                  className="text-destructive hover:text-destructive hover:bg-destructive/10"
                  onClick={() => {
                    if (confirm('Remove this asset?')) {
                      deleteMutation.mutate(asset.id)
                    }
                  }}
                >
                  Remove
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
