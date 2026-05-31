import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { addAsset } from '../api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

interface AddAssetFormProps {
  portfolioId: string
  onSuccess?: () => void
}

export function AddAssetForm({ portfolioId, onSuccess }: AddAssetFormProps) {
  const { t } = useTranslation('portfolio')
  const [symbol, setSymbol] = useState('')
  const [quantity, setQuantity] = useState('')
  const [price, setPrice] = useState('')
  const [currency, setCurrency] = useState('USD')
  const [type, setType] = useState<'STOCK' | 'CRYPTO'>('STOCK')

  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () =>
      addAsset(portfolioId, {
        symbol: symbol.toUpperCase(),
        quantity,
        avgBuyPrice: price,
        currency,
        type,
      }),
    onSuccess: () => {
      setSymbol('')
      setQuantity('')
      setPrice('')
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
      onSuccess?.()
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!symbol || !quantity || !price) return
    mutation.mutate()
  }

  return (
    <section>
      <h3 className="text-base font-semibold">{t('add-asset-title')}</h3>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="asset-symbol">{t('add-asset-symbol')}</Label>
            <Input
              id="asset-symbol"
              placeholder={t('add-asset-symbol-placeholder')}
              value={symbol}
              onChange={(e) => setSymbol(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="asset-type">{t('add-asset-type')}</Label>
            <select
              id="asset-type"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={type}
              onChange={(e) => setType(e.target.value as 'STOCK' | 'CRYPTO')}
            >
              <option value="STOCK">{t('add-asset-type-stock')}</option>
              <option value="CRYPTO">{t('add-asset-type-crypto')}</option>
            </select>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="asset-quantity">{t('add-asset-quantity')}</Label>
            <Input
              id="asset-quantity"
              type="number"
              step="0.000001"
              placeholder="0.00"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="asset-price">{t('add-asset-price')}</Label>
            <Input
              id="asset-price"
              type="number"
              step="0.01"
              placeholder="0.00"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
            />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="asset-currency">{t('add-asset-currency')}</Label>
          <Input
            id="asset-currency"
            placeholder={t('add-asset-currency-placeholder')}
            value={currency}
            onChange={(e) => setCurrency(e.target.value.toUpperCase())}
            required
          />
        </div>
        <Button type="submit" className="w-full" disabled={mutation.isPending}>
          {mutation.isPending ? t('add-asset-pending') : t('add-asset-button')}
        </Button>
      </form>
    </section>
  )
}
