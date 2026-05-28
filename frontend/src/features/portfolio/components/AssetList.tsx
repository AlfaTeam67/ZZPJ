import { useTranslation } from 'react-i18next'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import Decimal from 'decimal.js'

import { type Asset } from '@/types/portfolio/asset'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatMoney } from '@/utils/formatMoney'
import { removeAsset } from '../api'

interface AssetListProps {
  portfolioId: string
  assets: Asset[]
}

export function AssetList({ portfolioId, assets }: AssetListProps) {
  const { t } = useTranslation('portfolio')
  const queryClient = useQueryClient()

  const deleteMutation = useMutation({
    mutationFn: (assetId: string) => removeAsset(portfolioId, assetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
    },
  })

  return (
    <section className="rounded-2xl border border-border/40 bg-card/60 p-6">
      <h2 className="text-base font-semibold">{t('assets-title')}</h2>

      {assets.length === 0 ? (
        <p className="mt-4 text-sm text-muted-foreground">{t('no-assets')}</p>
      ) : (
        <div className="mt-4 space-y-3">
          {assets.map((asset) => (
            <div
              key={asset.id}
              className="flex items-center justify-between rounded-xl border border-border/30 p-4"
            >
              <div className="flex items-center gap-4">
                <div className="flex size-10 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                  {asset.symbol.substring(0, 2).toUpperCase()}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{asset.symbol}</span>
                    <Badge variant="secondary">{asset.type}</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {t('units', { price: formatMoney(asset.avgBuyPrice, asset.currency) })}
                    {' · '}
                    {asset.quantity}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="text-right">
                  <p className="font-semibold tabular-nums">
                    {formatMoney(
                      new Decimal(asset.quantity).mul(new Decimal(asset.avgBuyPrice)).toString(),
                      asset.currency
                    )}
                  </p>
                  <p className="text-xs text-muted-foreground">{t('total-invested')}</p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                  onClick={() => {
                    if (confirm(t('remove-asset-confirm'))) {
                      deleteMutation.mutate(asset.id)
                    }
                  }}
                >
                  {t('remove-asset')}
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}
