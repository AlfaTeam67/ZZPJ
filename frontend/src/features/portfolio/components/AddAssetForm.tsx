import { useState, useRef, useEffect, useMemo } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { addAsset } from '../api'
import { useSymbols } from '@/features/market/hooks/useSymbols'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import { formatMoney } from '@/utils/formatMoney'

interface AddAssetFormProps {
  portfolioId: string
  onSuccess?: () => void
}

type AssetType = 'STOCK' | 'CRYPTO' | 'BOND'

function inferType(symbolName: string, symbolsType?: string): AssetType {
  if (symbolsType === 'CRYPTO') return 'CRYPTO'
  if (symbolsType === 'STOCK') return 'STOCK'
  const upper = symbolName.toUpperCase()
  if (
    upper.includes('BTC') ||
    upper.includes('ETH') ||
    upper.includes('USDT') ||
    upper.includes('SOL') ||
    upper.includes('ADA') ||
    upper.includes('BNB')
  )
    return 'CRYPTO'
  return 'STOCK'
}

export function AddAssetForm({ portfolioId, onSuccess }: AddAssetFormProps) {
  const { t } = useTranslation('portfolio')
  const { data: tickers } = usePriceTicker()
  const { data: symbols } = useSymbols()

  const [query, setQuery] = useState('')
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const [selectedType, setSelectedType] = useState<AssetType>('STOCK')
  const [selectedCurrency, setSelectedCurrency] = useState('USD')
  const [quantity, setQuantity] = useState('')
  const [price, setPrice] = useState('')
  const [open, setOpen] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const queryClient = useQueryClient()

  const symbolTypeMap = useMemo(() => {
    if (!symbols) return new Map<string, string>()
    return new Map(symbols.map((s) => [s.symbol, s.type]))
  }, [symbols])

  const tickerList = useMemo(() => tickers ?? [], [tickers])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return tickerList.slice(0, 10)
    return tickerList.filter((tk) => tk.symbol.toLowerCase().includes(q)).slice(0, 10)
  }, [tickerList, query])

  const isValidSymbol = selectedSymbol !== null
  const selectedTicker = tickers?.find((tk) => tk.symbol === selectedSymbol)

  useEffect(() => {
    function onPointerDown(e: PointerEvent) {
      if (
        dropdownRef.current?.contains(e.target as Node) ||
        inputRef.current?.contains(e.target as Node)
      )
        return
      setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [])

  const handleSelect = (symbol: string) => {
    const tk = tickers?.find((t) => t.symbol === symbol)
    if (!tk) return
    const type = inferType(symbol, symbolTypeMap.get(symbol))
    setSelectedSymbol(symbol)
    setQuery(symbol)
    setSelectedType(type)
    setSelectedCurrency(tk.currency)
    const raw = typeof tk.price === 'number' ? tk.price : parseFloat(String(tk.price))
    if (Number.isFinite(raw)) setPrice(String(raw))
    setOpen(false)
  }

  const handleQueryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value)
    setSelectedSymbol(null)
    setOpen(true)
  }

  const mutation = useMutation({
    mutationFn: () =>
      addAsset(portfolioId, {
        symbol: selectedSymbol!,
        quantity,
        avgBuyPrice: price,
        currency: selectedCurrency,
        type: selectedType,
      }),
    onSuccess: () => {
      setQuery('')
      setSelectedSymbol(null)
      setQuantity('')
      setPrice('')
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] })
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
      onSuccess?.()
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!isValidSymbol || !quantity || !price) return
    mutation.mutate()
  }

  return (
    <section>
      <h3 className="text-base font-semibold">{t('add-asset-title')}</h3>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4">
        <div className="space-y-2">
          <Label htmlFor="asset-symbol">{t('add-asset-symbol')}</Label>
          <div className="relative">
            <Input
              ref={inputRef}
              id="asset-symbol"
              placeholder={t('add-asset-symbol-placeholder')}
              value={query}
              onChange={handleQueryChange}
              onFocus={() => setOpen(true)}
              autoComplete="off"
              className={cn(
                isValidSymbol && 'border-emerald-500/50',
                !isValidSymbol && query.length > 0 && 'border-amber-500/50'
              )}
              required
            />

            {open && filtered.length > 0 && (
              <div
                ref={dropdownRef}
                className="absolute left-0 right-0 top-full z-50 mt-1 max-h-60 overflow-y-auto rounded-xl border border-border bg-popover shadow-lg"
              >
                {filtered.map((tk) => {
                  const symType = symbolTypeMap.get(tk.symbol) ?? inferType(tk.symbol)
                  const isCrypto = symType === 'CRYPTO'
                  return (
                    <button
                      key={tk.symbol}
                      type="button"
                      onPointerDown={(e) => {
                        e.preventDefault()
                        handleSelect(tk.symbol)
                      }}
                      className="flex w-full items-center gap-3 px-3 py-2.5 text-left transition-colors hover:bg-muted/60 focus:bg-muted/60 focus:outline-none"
                    >
                      <div
                        className={cn(
                          'flex size-8 shrink-0 items-center justify-center rounded-lg text-xs font-bold',
                          isCrypto
                            ? 'bg-amber-500/15 text-amber-400'
                            : 'bg-blue-500/15 text-blue-400'
                        )}
                      >
                        {tk.symbol.slice(0, 2)}
                      </div>
                      <div className="min-w-0 flex-1">
                        <span className="text-sm font-semibold">{tk.symbol}</span>
                      </div>
                      <Badge
                        variant="secondary"
                        className={cn(
                          'shrink-0 text-[10px]',
                          isCrypto ? 'text-amber-400' : 'text-blue-400'
                        )}
                      >
                        {symType}
                      </Badge>
                      <span className="shrink-0 text-xs tabular-nums text-muted-foreground">
                        {formatMoney(tk.price, tk.currency)}
                      </span>
                    </button>
                  )
                })}
              </div>
            )}
          </div>

          {isValidSymbol && selectedTicker && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Badge variant="secondary" className="text-[10px]">
                {symbolTypeMap.get(selectedSymbol!) ?? inferType(selectedSymbol!)}
              </Badge>
              <span>
                {t('add-asset-market-price')}:{' '}
                {formatMoney(selectedTicker.price, selectedTicker.currency)}
              </span>
            </div>
          )}

          {!isValidSymbol && query.length > 0 && (
            <p className="text-xs text-amber-500">{t('add-asset-symbol-invalid')}</p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="asset-quantity">{t('add-asset-quantity')}</Label>
            <Input
              id="asset-quantity"
              type="number"
              step="0.000001"
              min="0"
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
              min="0"
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
            placeholder="USD"
            value={selectedCurrency}
            onChange={(e) => setSelectedCurrency(e.target.value.toUpperCase())}
            required
          />
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={mutation.isPending || !isValidSymbol || !quantity || !price}
        >
          {mutation.isPending ? t('add-asset-pending') : t('add-asset-button')}
        </Button>
      </form>
    </section>
  )
}
