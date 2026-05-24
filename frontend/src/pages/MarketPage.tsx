import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'
import { usePriceHistory } from '@/features/market/hooks/usePriceHistory'
import { formatMoney } from '@/utils/formatMoney'

export function MarketPage() {
  const { data: prices, isLoading } = usePriceTicker()
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const { data: history } = usePriceHistory(selectedSymbol)

  const selectedPrice = prices?.find((p) => p.symbol === selectedSymbol)

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Market Data</h1>
        <p className="text-muted-foreground">
          Browse market prices and view price history for different assets.
        </p>
      </div>

      <div className="grid gap-8 lg:grid-cols-[1fr,350px]">
        {/* Market Prices */}
        <div>
          <Card>
            <CardHeader>
              <CardTitle>All Symbols</CardTitle>
              <CardDescription>Current market prices and 24h change.</CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading && <p>Loading market data...</p>}
              {!prices || prices.length === 0 ? (
                <p className="text-sm text-muted-foreground">No market data available.</p>
              ) : (
                <div className="space-y-2">
                  {prices.map((item) => (
                    <button
                      key={item.symbol}
                      onClick={() => setSelectedSymbol(item.symbol)}
                      className={`w-full flex items-center justify-between rounded-lg border p-3 transition-colors ${selectedSymbol === item.symbol ? 'bg-muted border-primary' : 'hover:bg-muted/50'
                        }`}
                    >
                      <div className="text-left">
                        <p className="font-medium">{item.symbol}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium">{formatMoney(item.price, item.currency)}</p>
                        <p
                          className={`text-sm ${item.trend === 'UP'
                              ? 'text-green-600'
                              : item.trend === 'DOWN'
                                ? 'text-red-600'
                                : 'text-slate-500'
                            }`}
                        >
                          {item.trend === 'UP' ? '+' : ''}
                          {item.changePct24h ?? '0'}%
                        </p>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Selected Symbol Details */}
        {selectedPrice && (
          <aside className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>{selectedPrice.symbol}</CardTitle>
                <CardDescription>Price Details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">Current Price</p>
                  <p className="text-3xl font-bold">{formatMoney(selectedPrice.price, selectedPrice.currency)}</p>
                </div>

                <div>
                  <p className="text-sm text-muted-foreground">24h Change</p>
                  <p
                    className={`text-2xl font-bold ${selectedPrice.trend === 'UP'
                        ? 'text-green-600'
                        : selectedPrice.trend === 'DOWN'
                          ? 'text-red-600'
                          : 'text-slate-500'
                      }`}
                  >
                    {selectedPrice.trend === 'UP' ? '+' : ''}
                    {selectedPrice.changePct24h ?? '0'}%
                  </p>
                </div>

                {history && history.length > 0 && (
                  <div className="border-t pt-4">
                    <p className="text-sm font-medium mb-3">Price History</p>
                    <div className="space-y-2 max-h-[300px] overflow-y-auto">
                      {history.slice(0, 10).map((snapshot) => (
                        <div key={snapshot.id} className="flex justify-between text-sm">
                          <span className="text-muted-foreground">
                            {new Date(snapshot.fetchedAt).toLocaleString()}
                          </span>
                          <span className="font-medium">{formatMoney(snapshot.price, snapshot.currency)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </aside>
        )}
      </div>
    </div>
  )
}
