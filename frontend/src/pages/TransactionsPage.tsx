import { useState, useMemo } from 'react'
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { useTransactions } from '@/features/portfolio/hooks/useTransactions'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { formatMoney } from '@/utils/formatMoney'
import type { Transaction } from '@/types/portfolio/transaction'

type TxTypeFilter = 'ALL' | 'BUY' | 'SELL'

function exportToCsv(transactions: Transaction[], portfolioName: string) {
  const header = 'Data,Typ,Symbol,Ilość,Cena,Waluta,Opłata,Wartość,Notatki'
  const rows = transactions.map((tx) => {
    const total = (parseFloat(tx.quantity) * parseFloat(tx.price)).toFixed(2)
    const fields = [
      new Date(tx.executedAt).toLocaleDateString('pl-PL'),
      tx.type,
      tx.symbol ?? '',
      tx.quantity,
      tx.price,
      tx.currency,
      tx.fee ?? '0',
      total,
      tx.notes ?? '',
    ]
    // Wrap every field in quotes and escape embedded quotes to keep the CSV
    // structure intact for values containing commas, quotes or newlines.
    return fields.map((field) => `"${String(field).replace(/"/g, '""')}"`).join(',')
  })
  const csv = [header, ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `transakcje-${portfolioName}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export function TransactionsPage() {
  const { data: portfolios, isLoading: portfoliosLoading } = usePortfolios()
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string>('')
  const [typeFilter, setTypeFilter] = useState<TxTypeFilter>('ALL')
  const [search, setSearch] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  const portfolioId = selectedPortfolioId || portfolios?.[0]?.id || ''
  const { data: transactions, isLoading: txLoading } = useTransactions(portfolioId)

  const filtered = useMemo(() => {
    if (!transactions) return []
    return transactions.filter((tx) => {
      if (typeFilter !== 'ALL' && tx.type !== typeFilter) return false
      if (search && !tx.symbol?.toLowerCase().includes(search.toLowerCase())) return false
      // executedAt and the date inputs are both YYYY-MM-DD prefixed, so comparing
      // as strings avoids timezone shifts and cross-browser Date parsing quirks.
      if (dateFrom || dateTo) {
        const txDateStr = tx.executedAt.slice(0, 10)
        if (dateFrom && txDateStr < dateFrom) return false
        if (dateTo && txDateStr > dateTo) return false
      }
      return true
    })
  }, [transactions, typeFilter, search, dateFrom, dateTo])

  const selectedPortfolio = portfolios?.find((p) => p.id === portfolioId)

  const handleExport = () => {
    exportToCsv(filtered, selectedPortfolio?.name ?? 'portfel')
  }

  const isLoading = portfoliosLoading || txLoading

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Transakcje</h1>
        <p className="text-muted-foreground">Historia wszystkich transakcji kupna i sprzedaży.</p>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-5">
          <div className="flex flex-wrap gap-3 items-end">
            {/* Portfolio selector */}
            {portfolios && portfolios.length > 1 && (
              <div className="flex flex-col gap-1">
                <label className="text-xs text-muted-foreground font-medium">Portfel</label>
                <select
                  value={selectedPortfolioId}
                  onChange={(e) => setSelectedPortfolioId(e.target.value)}
                  className="rounded-md border border-border/50 bg-background px-3 py-2 text-sm"
                >
                  {portfolios.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Symbol search */}
            <div className="flex flex-col gap-1">
              <label className="text-xs text-muted-foreground font-medium">Symbol</label>
              <Input
                placeholder="Szukaj symbolu…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-36"
              />
            </div>

            {/* Type filter */}
            <div className="flex flex-col gap-1">
              <label className="text-xs text-muted-foreground font-medium">Typ</label>
              <div className="flex gap-1" role="group">
                {(['ALL', 'BUY', 'SELL'] as TxTypeFilter[]).map((t) => (
                  <button
                    key={t}
                    type="button"
                    onClick={() => setTypeFilter(t)}
                    className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                      typeFilter === t
                        ? 'bg-foreground text-background'
                        : 'border border-border/50 text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    {t === 'ALL' ? 'Wszystkie' : t}
                  </button>
                ))}
              </div>
            </div>

            {/* Date from */}
            <div className="flex flex-col gap-1">
              <label className="text-xs text-muted-foreground font-medium">Od</label>
              <Input
                type="date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                className="w-36"
              />
            </div>

            {/* Date to */}
            <div className="flex flex-col gap-1">
              <label className="text-xs text-muted-foreground font-medium">Do</label>
              <Input
                type="date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                className="w-36"
              />
            </div>

            {/* Export */}
            <button
              type="button"
              onClick={handleExport}
              disabled={filtered.length === 0}
              className="rounded-md border border-border/50 px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground disabled:opacity-40"
            >
              Eksportuj CSV
            </button>
          </div>
        </CardContent>
      </Card>

      {/* Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Historia
            {!isLoading && (
              <Badge variant="secondary" className="ml-2 text-xs font-normal">
                {filtered.length}
              </Badge>
            )}
          </CardTitle>
          <CardDescription>
            {selectedPortfolio ? `Portfel: ${selectedPortfolio.name}` : 'Wczytywanie…'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading && (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="h-10 animate-pulse rounded bg-muted" />
              ))}
            </div>
          )}
          {!isLoading && filtered.length === 0 && (
            <p className="py-10 text-center text-sm text-muted-foreground">
              Brak transakcji spełniających kryteria.
            </p>
          )}
          {!isLoading && filtered.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-muted-foreground">
                    <th className="text-left py-2 px-2 font-medium">Data</th>
                    <th className="text-left py-2 px-2 font-medium">Typ</th>
                    <th className="text-left py-2 px-2 font-medium">Symbol</th>
                    <th className="text-right py-2 px-2 font-medium">Ilość</th>
                    <th className="text-right py-2 px-2 font-medium">Cena</th>
                    <th className="text-right py-2 px-2 font-medium hidden sm:table-cell">
                      Opłata
                    </th>
                    <th className="text-right py-2 px-2 font-medium">Wartość</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((tx) => {
                    const total = (parseFloat(tx.quantity) * parseFloat(tx.price)).toFixed(2)
                    return (
                      <tr key={tx.id} className="border-b hover:bg-muted/30 transition-colors">
                        <td className="py-3 px-2 text-muted-foreground whitespace-nowrap">
                          {new Date(tx.executedAt).toLocaleDateString('pl-PL')}
                        </td>
                        <td className="py-3 px-2">
                          <Badge
                            variant={tx.type === 'BUY' ? 'default' : 'secondary'}
                            className="text-xs"
                          >
                            {tx.type}
                          </Badge>
                        </td>
                        <td className="py-3 px-2 font-semibold">{tx.symbol ?? '—'}</td>
                        <td className="py-3 px-2 text-right tabular-nums">{tx.quantity}</td>
                        <td className="py-3 px-2 text-right tabular-nums">
                          {formatMoney(tx.price, tx.currency)}
                        </td>
                        <td className="py-3 px-2 text-right tabular-nums text-muted-foreground hidden sm:table-cell">
                          {tx.fee ? formatMoney(tx.fee, tx.currency) : '—'}
                        </td>
                        <td className="py-3 px-2 text-right tabular-nums font-semibold">
                          {formatMoney(total, tx.currency)}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
