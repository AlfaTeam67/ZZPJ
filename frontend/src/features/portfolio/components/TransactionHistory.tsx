import { useTranslation } from 'react-i18next'
import Decimal from 'decimal.js'

import { Badge } from '@/components/ui/badge'
import { useTransactions } from '@/features/portfolio/hooks/useTransactions'
import { formatMoney } from '@/utils/formatMoney'
import { SectionSkeleton } from '@/components/ui/SectionSkeleton'

interface TransactionHistoryProps {
  portfolioId: string | null
}

export function TransactionHistory({ portfolioId }: TransactionHistoryProps) {
  const { t } = useTranslation('portfolio')
  const { data: transactions, isLoading } = useTransactions(portfolioId!)

  if (!portfolioId) return null

  return (
    <section className="rounded-2xl border border-border/40 bg-card/60 p-6">
      <h2 className="text-base font-semibold">{t('transactions-title')}</h2>
      <p className="mt-1 text-sm text-muted-foreground">{t('transactions-subtitle')}</p>

      {isLoading ? (
        <SectionSkeleton lines={4} className="mt-4" />
      ) : !transactions || transactions.length === 0 ? (
        <p className="mt-4 text-sm text-muted-foreground">{t('no-transactions')}</p>
      ) : (
        <div className="mt-4 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border/30">
                <th className="px-2 py-2 text-left font-medium text-muted-foreground">
                  {t('col-date')}
                </th>
                <th className="px-2 py-2 text-left font-medium text-muted-foreground">
                  {t('col-type')}
                </th>
                <th className="px-2 py-2 text-left font-medium text-muted-foreground">
                  {t('col-symbol')}
                </th>
                <th className="px-2 py-2 text-right font-medium text-muted-foreground">
                  {t('col-quantity')}
                </th>
                <th className="px-2 py-2 text-right font-medium text-muted-foreground">
                  {t('col-price')}
                </th>
                <th className="px-2 py-2 text-right font-medium text-muted-foreground">
                  {t('col-total')}
                </th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.id} className="border-b border-border/20 hover:bg-muted/30">
                  <td className="px-2 py-2 tabular-nums">
                    {new Date(tx.executedAt).toLocaleDateString()}
                  </td>
                  <td className="px-2 py-2">
                    <Badge
                      variant={tx.type === 'BUY' ? 'default' : 'outline'}
                      className={tx.type === 'BUY' ? 'bg-success/20 text-success' : ''}
                    >
                      {tx.type}
                    </Badge>
                  </td>
                  <td className="px-2 py-2 font-medium">{tx.symbol}</td>
                  <td className="px-2 py-2 text-right tabular-nums">{tx.quantity}</td>
                  <td className="px-2 py-2 text-right tabular-nums">
                    {formatMoney(tx.price, tx.currency)}
                  </td>
                  <td className="px-2 py-2 text-right font-medium tabular-nums">
                    {formatMoney(
                      new Decimal(tx.quantity).mul(new Decimal(tx.price)).toString(),
                      tx.currency
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
