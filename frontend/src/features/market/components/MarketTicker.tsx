import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { usePriceTicker } from '@/features/market/hooks/usePriceTicker'

export function MarketTicker() {
  const { data, isLoading } = usePriceTicker()

  return (
    <Card>
      <CardHeader>
        <CardTitle>Market ticker</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading && <p>Loading market data...</p>}
        <ul className="space-y-2">
          {data?.map((item) => (
            <li
              key={item.symbol}
              className="flex items-center justify-between rounded-lg border px-3 py-2"
            >
              <span className="font-medium">{item.symbol}</span>
              <span
                className={
                  item.trend === 'UP'
                    ? 'text-emerald-600'
                    : item.trend === 'DOWN'
                      ? 'text-red-600'
                      : 'text-slate-500'
                }
              >
                {item.trend === 'UP' ? '+' : ''}
                {item.changePct24h ?? '0'}%
              </span>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}
