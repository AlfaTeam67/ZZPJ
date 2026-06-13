import { useState, useMemo } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'
import { usePriceHistory } from '@/features/market/hooks/usePriceHistory'
import { formatMoney } from '@/utils/formatMoney'
import type { PriceTicker } from '@/types/market'

type AssetTypeFilter = 'ALL' | 'STOCK' | 'CRYPTO' | 'FOREX'

const TYPE_LABELS: Record<AssetTypeFilter, string> = {
  ALL: 'Wszystkie',
  STOCK: 'Akcje',
  CRYPTO: 'Krypto',
  FOREX: 'Waluty',
}

function inferType(symbol: string): AssetTypeFilter {
  const s = symbol.toUpperCase()
  const cryptos = ['BTC', 'ETH', 'SOL', 'ADA', 'DOT', 'USDT', 'BNB', 'XRP', 'DOGE', 'AVAX']
  const forex = ['EURUSD', 'GBPUSD', 'USDJPY', 'USDPLN', 'EURPLN']
  if (cryptos.some((c) => s.includes(c))) return 'CRYPTO'
  if (forex.some((f) => s === f)) return 'FOREX'
  return 'STOCK'
}

interface PriceRowProps {
  item: PriceTicker
  isSelected: boolean
  onSelect: (symbol: string) => void
}

function PriceRow({ item, isSelected, onSelect }: PriceRowProps) {
  const changeNum = item.changePct24h ? parseFloat(item.changePct24h) : 0
  const isUp = item.trend === 'UP'
  const isDown = item.trend === 'DOWN'

  return (
    <button
      type="button"
      onClick={() => onSelect(item.symbol)}
      className={`w-full flex items-center justify-between rounded-lg border p-3 text-left transition-colors ${
        isSelected ? 'border-ring bg-muted' : 'border-border/40 hover:bg-muted/50'
      }`}
      aria-pressed={isSelected}
    >
      <div className="flex items-center gap-3">
        <div className="flex size-9 items-center justify-center rounded-md bg-muted/60 text-xs font-bold tracking-tight">
          {item.symbol.slice(0, 3)}
        </div>
        <div>
          <p className="font-semibold text-sm">{item.symbol}</p>
          <p className="text-xs text-muted-foreground">{inferType(item.symbol)}</p>
        </div>
      </div>
      <div className="text-right">
        <p className="font-semibold text-sm tabular-nums">
          {formatMoney(item.price, item.currency)}
        </p>
        <p
          className={`text-xs tabular-nums ${
            isUp ? 'text-success' : isDown ? 'text-destructive' : 'text-muted-foreground'
          }`}
        >
          {isUp ? '+' : ''}
          {changeNum.toFixed(2)}%
        </p>
      </div>
    </button>
  )
}

export function MarketPage() {
  const { data: prices, isLoading, isError } = usePriceTicker()
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<AssetTypeFilter>('ALL')

  const { data: history, isLoading: historyLoading } = usePriceHistory(selectedSymbol)

  const filtered = useMemo(() => {
    if (!prices) return []
    return prices.filter((p) => {
      const matchesSearch = p.symbol.toLowerCase().includes(search.toLowerCase())
      const matchesType = typeFilter === 'ALL' || inferType(p.symbol) === typeFilter
      return matchesSearch && matchesType
    })
  }, [prices, search, typeFilter])

  const selectedPrice = prices?.find((p) => p.symbol === selectedSymbol)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Rynek</h1>
        <p className="text-muted-foreground">Aktualne ceny i historia instrumentów finansowych.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr,340px]">
        {/* Left panel: list */}
        <div className="space-y-4">
          {/* Search + filters */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <Input
              type="search"
              placeholder="Szukaj symbolu…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="sm:w-56"
              aria-label="Szukaj symbolu"
            />
            <div className="flex gap-2 flex-wrap" role="group" aria-label="Filtr typu">
              {(Object.keys(TYPE_LABELS) as AssetTypeFilter[]).map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => setTypeFilter(t)}
                  className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                    typeFilter === t
                      ? 'bg-foreground text-background'
                      : 'border border-border/50 text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {TYPE_LABELS[t]}
                </button>
              ))}
            </div>
          </div>

          {/* Price list */}
          <Card>
            <CardHeader>
              <CardTitle>
                Instrumenty
                {!isLoading && (
                  <Badge variant="secondary" className="ml-2 text-xs font-normal">
                    {filtered.length}
                  </Badge>
                )}
              </CardTitle>
              <CardDescription>Aktualne ceny i zmiana 24h.</CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading && (
                <div className="space-y-2">
                  {Array.from({ length: 6 }).map((_, i) => (
                    <div key={i} className="h-14 animate-pulse rounded-lg bg-muted" />
                  ))}
                </div>
              )}
              {isError && (
                <p className="text-sm text-destructive py-4 text-center">
                  Nie udało się pobrać danych rynkowych.
                </p>
              )}
              {!isLoading && !isError && filtered.length === 0 && (
                <p className="text-sm text-muted-foreground py-6 text-center">
                  {search ? `Brak wyników dla "${search}".` : 'Brak danych rynkowych.'}
                </p>
              )}
              {!isLoading && !isError && filtered.length > 0 && (
                <div className="space-y-1.5">
                  {filtered.map((item) => (
                    <PriceRow
                      key={item.symbol}
                      item={item}
                      isSelected={selectedSymbol === item.symbol}
                      onSelect={setSelectedSymbol}
                    />
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right panel: detail */}
        <aside className="space-y-4">
          {!selectedPrice ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center gap-3 py-16 text-center">
                <p className="text-muted-foreground text-sm">
                  Kliknij instrument, aby zobaczyć szczegóły.
                </p>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-2xl">{selectedPrice.symbol}</CardTitle>
                    <CardDescription>{inferType(selectedPrice.symbol)}</CardDescription>
                  </div>
                  <Badge
                    variant={
                      selectedPrice.trend === 'UP'
                        ? 'default'
                        : selectedPrice.trend === 'DOWN'
                          ? 'destructive'
                          : 'secondary'
                    }
                  >
                    {selectedPrice.trend === 'UP' ? '▲' : selectedPrice.trend === 'DOWN' ? '▼' : '—'}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Current price */}
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider">
                    Aktualna cena
                  </p>
                  <p className="text-4xl font-bold tabular-nums mt-1">
                    {formatMoney(selectedPrice.price, selectedPrice.currency)}
                  </p>
                </div>

                {/* 24h change */}
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider">
                    Zmiana 24h
                  </p>
                  <p
                    className={`text-2xl font-bold tabular-nums mt-1 ${
                      selectedPrice.trend === 'UP'
                        ? 'text-success'
                        : selectedPrice.trend === 'DOWN'
                          ? 'text-destructive'
                          : 'text-muted-foreground'
                    }`}
                  >
                    {selectedPrice.trend === 'UP' ? '+' : ''}
                    {selectedPrice.changePct24h ? parseFloat(selectedPrice.changePct24h).toFixed(2) : '0.00'}%
                  </p>
                </div>

                {/* Price history */}
                {historyLoading ? (
                  <div className="space-y-2">
                    <p className="text-xs text-muted-foreground uppercase tracking-wider">
                      Historia cen
                    </p>
                    {Array.from({ length: 4 }).map((_, i) => (
                      <div key={i} className="h-8 animate-pulse rounded bg-muted" />
                    ))}
                  </div>
                ) : history && history.length > 0 ? (
                  <div>
                    <p className="text-xs text-muted-foreground uppercase tracking-wider mb-3">
                      Ostatnie wpisy
                    </p>
                    <div className="max-h-64 space-y-1.5 overflow-y-auto pr-1">
                      {history.slice(0, 20).map((snapshot) => (
                        <div
                          key={snapshot.id}
                          className="flex items-center justify-between rounded-md bg-muted/30 px-3 py-2 text-sm"
                        >
                          <span className="text-muted-foreground text-xs">
                            {new Date(snapshot.fetchedAt).toLocaleString('pl-PL', {
                              day: '2-digit',
                              month: '2-digit',
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </span>
                          <span className="font-semibold tabular-nums">
                            {formatMoney(snapshot.price, snapshot.currency)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">Brak historii cen.</p>
                )}
              </CardContent>
            </Card>
          )}
        </aside>
      </div>
    </div>
  )
}
