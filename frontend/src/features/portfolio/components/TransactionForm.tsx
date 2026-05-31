import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import Decimal from 'decimal.js'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { createTransaction, type TransactionRequest } from '@/features/portfolio/api'
import { useAssets } from '@/features/portfolio/hooks/useAssets'
import { formatMoney } from '@/utils/formatMoney'

interface TransactionFormProps {
  portfolioId: string | null
  onSuccess?: () => void
}

export function TransactionForm({ portfolioId, onSuccess }: TransactionFormProps) {
  const { t } = useTranslation('portfolio')
  const queryClient = useQueryClient()
  const { data: assets } = useAssets(portfolioId)
  const [formData, setFormData] = useState<TransactionRequest>({
    type: 'BUY',
    quantity: '',
    price: '',
    currency: 'USD',
    assetId: '',
  })

  const createMutation = useMutation({
    mutationFn: async () => {
      if (!portfolioId) return
      return createTransaction(portfolioId, formData)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolio', 'transactions', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['portfolio-valuation', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['assets', portfolioId] })
      setFormData({ type: 'BUY', quantity: '', price: '', currency: 'USD', assetId: '' })
      onSuccess?.()
    },
  })

  if (!portfolioId) return null

  const total =
    formData.quantity && formData.price
      ? new Decimal(formData.quantity).mul(new Decimal(formData.price)).toFixed(2)
      : '0.00'

  return (
    <section>
      <h3 className="text-base font-semibold">{t('tx-title')}</h3>
      <p className="mt-1 text-sm text-muted-foreground">{t('tx-subtitle')}</p>

      <div className="mt-4 space-y-4">
        <div className="space-y-2">
          <Label htmlFor="tx-asset">{t('tx-asset')}</Label>
          <select
            id="tx-asset"
            value={formData.assetId}
            onChange={(e) => {
              const asset = assets?.find((a) => a.id === e.target.value)
              setFormData({
                ...formData,
                assetId: e.target.value,
                currency: asset?.currency || 'USD',
              })
            }}
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          >
            <option value="">{t('tx-asset-placeholder')}</option>
            {assets?.map((asset) => (
              <option key={asset.id} value={asset.id}>
                {asset.symbol} ({asset.type})
              </option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="tx-type">{t('tx-type')}</Label>
            <select
              id="tx-type"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value as 'BUY' | 'SELL' })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="BUY">{t('tx-type-buy')}</option>
              <option value="SELL">{t('tx-type-sell')}</option>
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="tx-quantity">{t('tx-quantity')}</Label>
            <Input
              id="tx-quantity"
              type="number"
              step="0.01"
              placeholder="100"
              value={formData.quantity}
              onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="tx-price">{t('tx-price')}</Label>
            <Input
              id="tx-price"
              type="number"
              step="0.01"
              placeholder="150.50"
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="tx-currency">{t('tx-currency')}</Label>
            <Input id="tx-currency" type="text" value={formData.currency} disabled />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="tx-fee">{t('tx-fee')}</Label>
          <Input
            id="tx-fee"
            type="number"
            step="0.01"
            placeholder="0.00"
            value={formData.fee || ''}
            onChange={(e) => setFormData({ ...formData, fee: e.target.value })}
          />
        </div>

        <div className="rounded-xl bg-muted/40 p-4">
          <p className="text-xs text-muted-foreground">{t('tx-total')}</p>
          <p className="mt-1 text-lg font-bold tabular-nums">
            {formatMoney(total, formData.currency)}
          </p>
        </div>

        <Button
          onClick={() => createMutation.mutate()}
          disabled={
            !formData.quantity || !formData.price || !formData.assetId || createMutation.isPending
          }
          className="w-full"
        >
          {createMutation.isPending ? t('tx-pending') : t('tx-button')}
        </Button>

        {createMutation.isError && <p className="text-sm text-destructive">{t('tx-error')}</p>}
      </div>
    </section>
  )
}
