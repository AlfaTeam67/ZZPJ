import { useState, useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts'
import { HugeiconsIcon } from '@hugeicons/react'
import {
  ChartUpIcon,
  ChartDownIcon,
  ArrowRight01Icon,
  ActivityIcon,
  Search01Icon,
} from '@hugeicons/core-free-icons'

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'
import { usePriceHistory } from '@/features/market/hooks/usePriceHistory'
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { formatMoney } from '@/utils/formatMoney'
import { cn } from '@/lib/utils'
import type { PriceTicker } from '@/types/market'

function toChartPoint(snap: { price: string | number; fetchedAt: string }) {
  const raw = typeof snap.price === 'number' ? snap.price : parseFloat(snap.price)
  return {
    time: new Date(snap.fetchedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    price: Number.isFinite(raw) ? raw : 0,
  }
}

const SYMBOL_COLORS = [
  { bg: 'bg-violet-500/15', text: 'text-violet-400', bar: 'bg-violet-500' },
  { bg: 'bg-emerald-500/15', text: 'text-emerald-400', bar: 'bg-emerald-500' },
  { bg: 'bg-amber-500/15', text: 'text-amber-400', bar: 'bg-amber-500' },
  { bg: 'bg-rose-500/15', text: 'text-rose-400', bar: 'bg-rose-500' },
  { bg: 'bg-cyan-500/15', text: 'text-cyan-400', bar: 'bg-cyan-500' },
  { bg: 'bg-lime-500/15', text: 'text-lime-400', bar: 'bg-lime-500' },
  { bg: 'bg-pink-500/15', text: 'text-pink-400', bar: 'bg-pink-500' },
  { bg: 'bg-sky-500/15', text: 'text-sky-400', bar: 'bg-sky-500' },
]

function symbolColor(symbol: string) {
  const hash = symbol.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0)
  return SYMBOL_COLORS[hash % SYMBOL_COLORS.length]
}

interface SymbolRowProps {
  item: PriceTicker
  isSelected: boolean
  isMine: boolean
  onClick: () => void
}

function SymbolRow({ item, isSelected, isMine, onClick }: SymbolRowProps) {
  const { t } = useTranslation('market')
  const pct = item.changePct24h ? parseFloat(item.changePct24h) : 0
  const up = item.trend === 'UP'
  const down = item.trend === 'DOWN'
  const color = symbolColor(item.symbol)

  return (
    <button
      onClick={onClick}
      className={cn(
        'group relative flex w-full items-center gap-3 px-4 py-3 text-left transition-all duration-150',
        isSelected ? 'bg-primary/8' : 'hover:bg-muted/30'
      )}
    >
      <div
        className={cn(
          'absolute left-0 top-1/2 h-8 w-0.5 -translate-y-1/2 rounded-r transition-all duration-150',
          isSelected
            ? up
              ? 'bg-emerald-400'
              : down
                ? 'bg-red-400'
                : 'bg-primary'
            : 'opacity-0 group-hover:opacity-40 ' + color.bar
        )}
      />

      <div
        className={cn(
          'flex size-9 shrink-0 items-center justify-center rounded-xl text-xs font-bold tracking-wide transition-all duration-150',
          isSelected ? 'bg-primary/20 text-primary' : cn(color.bg, color.text)
        )}
      >
        {item.symbol.slice(0, 2)}
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1.5">
          <span className="text-sm font-semibold">{item.symbol}</span>
          {isMine && (
            <Badge
              variant="secondary"
              className="h-4 rounded px-1 py-0 text-[10px] font-medium leading-none"
            >
              {t('mine')}
            </Badge>
          )}
          {isSelected && (
            <HugeiconsIcon icon={ArrowRight01Icon} className="size-3 text-primary" aria-hidden />
          )}
        </div>
        <span className="text-xs text-muted-foreground">{item.currency}</span>
      </div>

      <div className="flex flex-col items-end gap-0.5">
        <span className="text-sm font-semibold tabular-nums">
          {formatMoney(item.price, item.currency)}
        </span>
        <span
          className={cn(
            'flex items-center gap-0.5 text-xs font-medium tabular-nums',
            up ? 'text-emerald-400' : down ? 'text-red-400' : 'text-muted-foreground'
          )}
        >
          {up && <HugeiconsIcon icon={ChartUpIcon} className="size-3" aria-hidden />}
          {down && <HugeiconsIcon icon={ChartDownIcon} className="size-3" aria-hidden />}
          {up ? '+' : ''}
          {pct.toFixed(2)}%
        </span>
      </div>
    </button>
  )
}

export function MarketPage() {
  const { t } = useTranslation('market')
  const { data: prices, isLoading, isLive } = usePriceTicker()
  const { data: portfolios } = usePortfolios()
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [view, setView] = useState<'stocks' | 'crypto' | 'mine'>('stocks')
  const { data: history } = usePriceHistory(selectedSymbol)

  const mySymbols = useMemo(() => {
    if (!portfolios) return new Set<string>()
    return new Set(portfolios.flatMap((p) => p.assets?.map((a) => a.symbol) ?? []))
  }, [portfolios])

  const filtered = useMemo(() => {
    if (!prices) return []
    const q = search.trim().toLowerCase()
    return q ? prices.filter((p) => p.symbol.toLowerCase().includes(q)) : prices
  }, [prices, search])

  const myPrices = useMemo(() => {
    const fromApi = filtered.filter((p) => mySymbols.has(p.symbol))
    const fromApiSet = new Set(fromApi.map((p) => p.symbol))

    // add missing
    const missing = Array.from(mySymbols)
      .filter((s) => !fromApiSet.has(s) && s.toLowerCase().includes(search.trim().toLowerCase()))
      .map(
        (s) =>
          ({
            symbol: s,
            price: 0,
            currency: 'USD',
            trend: 'NEUTRAL',
            fetchedAt: new Date().toISOString(),
          }) as PriceTicker
      )

    return [...fromApi, ...missing]
  }, [filtered, mySymbols, search])
  const otherPrices = useMemo(
    () => filtered.filter((p) => !mySymbols.has(p.symbol)),
    [filtered, mySymbols]
  )
  const otherStocks = useMemo(
    () => otherPrices.filter((p) => !p.symbol.startsWith('BINANCE:')),
    [otherPrices]
  )
  const otherCryptos = useMemo(
    () => otherPrices.filter((p) => p.symbol.startsWith('BINANCE:')),
    [otherPrices]
  )

  const selectedPrice = prices?.find((p) => p.symbol === selectedSymbol)

  const chartData = useMemo(
    () =>
      history
        ? [...history]
            .sort((a, b) => new Date(a.fetchedAt).getTime() - new Date(b.fetchedAt).getTime())
            .map(toChartPoint)
        : [],
    [history]
  )

  const isPositive = selectedPrice
    ? (selectedPrice.changePct24h ? parseFloat(selectedPrice.changePct24h) : 0) >= 0
    : true

  const handleSelect = (symbol: string) =>
    setSelectedSymbol((prev) => (prev === symbol ? null : symbol))

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t('title')}</h1>
          <p className="mt-1 text-sm text-muted-foreground">{t('subtitle')}</p>
        </div>
        <div
          className={cn(
            'mt-1 flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-medium',
            isLive
              ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-400'
              : 'border-border/40 bg-muted/30 text-muted-foreground'
          )}
        >
          <span
            className={cn(
              'size-1.5 rounded-full',
              isLive ? 'animate-pulse bg-emerald-400' : 'bg-muted-foreground'
            )}
          />
          {isLive ? t('live') : t('loading')}
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr,380px]">
        <Card className="overflow-hidden">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between gap-3">
              <div>
                <CardTitle className="text-base">{t('all-symbols')}</CardTitle>
                <CardDescription>{t('all-symbols-desc')}</CardDescription>
              </div>
              <div className="flex shrink-0 gap-1 rounded-lg bg-muted p-1">
                <button
                  onClick={() => setView('stocks')}
                  className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                    view === 'stocks'
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-background/50'
                  }`}
                >
                  {t('market-stocks', 'Akcje')}
                </button>
                <button
                  onClick={() => setView('crypto')}
                  className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                    view === 'crypto'
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-background/50'
                  }`}
                >
                  {t('market-crypto', 'Krypto')}
                </button>
                <button
                  onClick={() => setView('mine')}
                  className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                    view === 'mine'
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-background/50'
                  }`}
                >
                  {t('view-mine', 'Moje')} ({mySymbols.size})
                </button>
              </div>
            </div>
            <div className="relative mt-1">
              <HugeiconsIcon
                icon={Search01Icon}
                className="absolute left-3 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground"
                aria-hidden
              />
              <Input
                placeholder={t('search-placeholder')}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="h-8 pl-8 text-sm"
              />
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="space-y-1 px-4 pb-4">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="h-14 animate-pulse rounded-xl bg-muted/50" />
                ))}
              </div>
            ) : !prices || prices.length === 0 ? (
              <p className="px-4 pb-4 text-sm text-muted-foreground">{t('no-data')}</p>
            ) : (
              <div className="divide-y divide-border/20">
                {view === 'mine' ? (
                  myPrices.length > 0 ? (
                    myPrices.map((item) => (
                      <SymbolRow
                        key={item.symbol}
                        item={item}
                        isSelected={selectedSymbol === item.symbol}
                        isMine={true}
                        onClick={() => handleSelect(item.symbol)}
                      />
                    ))
                  ) : (
                    <p className="px-4 py-6 text-center text-sm text-muted-foreground">
                      {t('no-my-assets', 'Brak aktywów w portfelu')}
                    </p>
                  )
                ) : view === 'stocks' ? (
                  otherStocks.length > 0 ? (
                    otherStocks.map((item) => (
                      <SymbolRow
                        key={item.symbol}
                        item={item}
                        isSelected={selectedSymbol === item.symbol}
                        isMine={mySymbols.has(item.symbol)}
                        onClick={() => handleSelect(item.symbol)}
                      />
                    ))
                  ) : (
                    <p className="px-4 py-6 text-center text-sm text-muted-foreground">
                      {t('no-results')}
                    </p>
                  )
                ) : otherCryptos.length > 0 ? (
                  otherCryptos.map((item) => (
                    <SymbolRow
                      key={item.symbol}
                      item={item}
                      isSelected={selectedSymbol === item.symbol}
                      isMine={mySymbols.has(item.symbol)}
                      onClick={() => handleSelect(item.symbol)}
                    />
                  ))
                ) : (
                  <p className="px-4 py-6 text-center text-sm text-muted-foreground">
                    {t('no-results')}
                  </p>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        <aside className="space-y-4">
          {selectedPrice ? (
            <>
              <Card>
                <CardHeader className="pb-2">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <CardTitle className="text-2xl">{selectedPrice.symbol}</CardTitle>
                        {mySymbols.has(selectedPrice.symbol) && (
                          <Badge variant="secondary" className="text-xs">
                            {t('mine')}
                          </Badge>
                        )}
                      </div>
                      <CardDescription>{t('price-details')}</CardDescription>
                    </div>
                    <HugeiconsIcon
                      icon={ActivityIcon}
                      className="mt-1 size-5 text-muted-foreground"
                      aria-hidden
                    />
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <p className="text-xs text-muted-foreground">{t('current-price')}</p>
                    <p className="mt-0.5 text-3xl font-bold tabular-nums">
                      {formatMoney(selectedPrice.price, selectedPrice.currency)}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">{t('change-24h')}</p>
                    <p
                      className={cn(
                        'mt-0.5 text-xl font-bold tabular-nums',
                        selectedPrice.trend === 'UP'
                          ? 'text-emerald-400'
                          : selectedPrice.trend === 'DOWN'
                            ? 'text-red-400'
                            : 'text-muted-foreground'
                      )}
                    >
                      {selectedPrice.trend === 'UP' ? '+' : ''}
                      {selectedPrice.changePct24h ?? '0'}%
                    </p>
                  </div>
                </CardContent>
              </Card>

              {chartData.length > 1 && (
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-base">{t('chart-title')}</CardTitle>
                  </CardHeader>
                  <CardContent className="px-2 pb-4">
                    <ResponsiveContainer width="100%" height={180}>
                      <AreaChart data={chartData} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
                        <defs>
                          <linearGradient id="priceGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop
                              offset="5%"
                              stopColor={isPositive ? '#34d399' : '#f87171'}
                              stopOpacity={0.25}
                            />
                            <stop
                              offset="95%"
                              stopColor={isPositive ? '#34d399' : '#f87171'}
                              stopOpacity={0}
                            />
                          </linearGradient>
                        </defs>
                        <CartesianGrid
                          strokeDasharray="3 3"
                          vertical={false}
                          stroke="rgba(255,255,255,0.06)"
                        />
                        <XAxis
                          dataKey="time"
                          tick={{ fontSize: 10, fill: 'var(--color-muted-foreground)' }}
                          tickLine={false}
                          axisLine={false}
                          interval="preserveStartEnd"
                        />
                        <YAxis
                          domain={['auto', 'auto']}
                          tick={{ fontSize: 10, fill: 'var(--color-muted-foreground)' }}
                          tickLine={false}
                          axisLine={false}
                          width={48}
                          tickFormatter={(v: number) => v.toLocaleString()}
                        />
                        <Tooltip
                          contentStyle={{
                            background: 'var(--color-card)',
                            border: '1px solid var(--color-border)',
                            borderRadius: '8px',
                            fontSize: '12px',
                          }}
                          labelStyle={{ color: 'var(--color-muted-foreground)' }}
                          itemStyle={{ color: isPositive ? '#34d399' : '#f87171' }}
                        />
                        <Area
                          type="monotone"
                          dataKey="price"
                          stroke={isPositive ? '#34d399' : '#f87171'}
                          strokeWidth={2}
                          fill="url(#priceGrad)"
                          dot={false}
                          activeDot={{ r: 3 }}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>
              )}

              {history && history.length > 0 && (
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-base">{t('price-history')}</CardTitle>
                  </CardHeader>
                  <CardContent className="p-0">
                    <div className="max-h-[220px] divide-y divide-border/30 overflow-y-auto">
                      {history.slice(0, 15).map((snap) => (
                        <div
                          key={snap.id}
                          className="flex items-center justify-between px-6 py-2.5"
                        >
                          <span className="text-xs text-muted-foreground">
                            {new Date(snap.fetchedAt).toLocaleString([], {
                              month: 'short',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </span>
                          <span className="text-sm font-medium tabular-nums">
                            {formatMoney(snap.price, snap.currency)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </>
          ) : (
            <div className="flex h-full min-h-[200px] items-center justify-center rounded-2xl border border-dashed border-border/40">
              <p className="text-sm text-muted-foreground">{t('no-symbol-selected')}</p>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
