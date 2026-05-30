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
} from '@hugeicons/core-free-icons'

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'
import { usePriceHistory } from '@/features/market/hooks/usePriceHistory'
import { formatMoney } from '@/utils/formatMoney'
import { cn } from '@/lib/utils'

function toChartPoint(snap: { price: string | number; fetchedAt: string }) {
  const raw = typeof snap.price === 'number' ? snap.price : parseFloat(snap.price)
  return {
    time: new Date(snap.fetchedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    price: Number.isFinite(raw) ? raw : 0,
  }
}

export function MarketPage() {
  const { t } = useTranslation('market')
  const { data: prices, isLoading, isLive } = usePriceTicker()
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const { data: history } = usePriceHistory(selectedSymbol)

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
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base">{t('all-symbols')}</CardTitle>
            <CardDescription>{t('all-symbols-desc')}</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="space-y-1 px-6 pb-4">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="h-12 animate-pulse rounded-lg bg-muted/50" />
                ))}
              </div>
            ) : !prices || prices.length === 0 ? (
              <p className="px-6 pb-4 text-sm text-muted-foreground">{t('no-data')}</p>
            ) : (
              <div className="divide-y divide-border/30">
                <div className="grid grid-cols-[1fr,auto,auto] gap-4 px-6 py-2">
                  <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    {t('symbol')}
                  </span>
                  <span className="text-right text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    {t('current-price')}
                  </span>
                  <span className="w-16 text-right text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    {t('change-24h')}
                  </span>
                </div>
                {prices.map((item) => {
                  const pct = item.changePct24h ? parseFloat(item.changePct24h) : 0
                  const up = item.trend === 'UP'
                  const down = item.trend === 'DOWN'
                  const isSelected = selectedSymbol === item.symbol
                  return (
                    <button
                      key={item.symbol}
                      onClick={() =>
                        setSelectedSymbol(isSelected ? null : item.symbol)
                      }
                      className={cn(
                        'grid w-full grid-cols-[1fr,auto,auto] items-center gap-4 px-6 py-3 text-left transition-colors',
                        isSelected
                          ? 'bg-primary/8 text-foreground'
                          : 'hover:bg-muted/40 text-foreground'
                      )}
                    >
                      <div className="flex items-center gap-3">
                        <div
                          className={cn(
                            'flex size-8 shrink-0 items-center justify-center rounded-md text-xs font-bold',
                            isSelected
                              ? 'bg-primary/20 text-primary'
                              : 'bg-muted text-muted-foreground'
                          )}
                        >
                          {item.symbol.slice(0, 2)}
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-sm font-semibold">{item.symbol}</span>
                          {isSelected && (
                            <HugeiconsIcon
                              icon={ArrowRight01Icon}
                              className="size-3 text-primary"
                              aria-hidden
                            />
                          )}
                        </div>
                      </div>
                      <span className="text-sm font-medium tabular-nums">
                        {formatMoney(item.price, item.currency)}
                      </span>
                      <span
                        className={cn(
                          'flex w-16 items-center justify-end gap-1 text-right text-sm font-medium tabular-nums',
                          up ? 'text-emerald-400' : down ? 'text-red-400' : 'text-muted-foreground'
                        )}
                      >
                        {up && (
                          <HugeiconsIcon icon={ChartUpIcon} className="size-3" aria-hidden />
                        )}
                        {down && (
                          <HugeiconsIcon icon={ChartDownIcon} className="size-3" aria-hidden />
                        )}
                        {up ? '+' : ''}
                        {pct.toFixed(2)}%
                      </span>
                    </button>
                  )
                })}
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
                      <CardTitle className="text-2xl">{selectedPrice.symbol}</CardTitle>
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
                    <div className="max-h-[220px] overflow-y-auto divide-y divide-border/30">
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
