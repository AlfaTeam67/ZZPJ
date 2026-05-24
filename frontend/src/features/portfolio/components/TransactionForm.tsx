import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { createTransaction, type TransactionRequest } from '@/features/portfolio/api'
import { useAssets } from '@/features/portfolio/hooks/useAssets'
import { formatMoney } from '@/utils/formatMoney'

interface TransactionFormProps {
  portfolioId: string | null
}

export function TransactionForm({ portfolioId }: TransactionFormProps) {
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
    },
  })

  if (!portfolioId) {
    return null
  }

  const total = formData.quantity && formData.price ? (parseFloat(formData.quantity) * parseFloat(formData.price)).toFixed(2) : '0.00'

  return (
    <Card>
      <CardHeader>
        <CardTitle>New Transaction</CardTitle>
        <CardDescription>Record a buy or sell transaction.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid gap-4">
          <div>
            <Label htmlFor="asset">Asset</Label>
            <select
              id="asset"
              value={formData.assetId}
              onChange={(e) => {
                const asset = assets?.find((a) => a.id === e.target.value)
                setFormData({
                  ...formData,
                  assetId: e.target.value,
                  currency: asset?.currency || 'USD',
                })
              }}
              className="w-full px-3 py-2 border rounded-md bg-background"
            >
              <option value="">Select asset</option>
              {assets?.map((asset) => (
                <option key={asset.id} value={asset.id}>
                  {asset.symbol} ({asset.type})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="type">Type</Label>
              <select
                id="type"
                value={formData.type}
                onChange={(e) =>
                  setFormData({ ...formData, type: e.target.value as 'BUY' | 'SELL' })
                }
                className="w-full px-3 py-2 border rounded-md bg-background"
              >
                <option value="BUY">Buy</option>
                <option value="SELL">Sell</option>
              </select>
            </div>

            <div>
              <Label htmlFor="quantity">Quantity</Label>
              <Input
                id="quantity"
                type="number"
                step="0.01"
                placeholder="100"
                value={formData.quantity}
                onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="price">Price per unit</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                placeholder="150.50"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
              />
            </div>

            <div>
              <Label htmlFor="currency">Currency</Label>
              <Input
                id="currency"
                type="text"
                value={formData.currency}
                disabled
                className="bg-muted"
              />
            </div>
          </div>

          <div>
            <Label htmlFor="fee">Fee (optional)</Label>
            <Input
              id="fee"
              type="number"
              step="0.01"
              placeholder="0.00"
              value={formData.fee || ''}
              onChange={(e) => setFormData({ ...formData, fee: e.target.value })}
            />
          </div>

          <div className="bg-muted p-3 rounded-md space-y-1">
            <p className="text-sm text-muted-foreground">Total Value</p>
            <p className="text-lg font-bold">{formatMoney(total, formData.currency)}</p>
            {formData.fee && (
              <p className="text-sm text-muted-foreground">
                Fee: {formatMoney(formData.fee, formData.currency)}
              </p>
            )}
          </div>

          <Button
            onClick={() => createMutation.mutate()}
            disabled={!formData.quantity || !formData.price || !formData.assetId || createMutation.isPending}
            className="w-full"
          >
            {createMutation.isPending ? 'Saving...' : 'Record Transaction'}
          </Button>

          {createMutation.isError && (
            <p className="text-sm text-red-600">Error creating transaction. Try again.</p>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
