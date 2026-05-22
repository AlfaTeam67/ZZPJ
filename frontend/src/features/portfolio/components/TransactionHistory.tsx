import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useTransactions } from '@/features/portfolio/hooks/useTransactions'
import { formatMoney } from '@/utils/formatMoney'

interface TransactionHistoryProps {
  portfolioId: string | null
}

export function TransactionHistory({ portfolioId }: TransactionHistoryProps) {
  const { data: transactions, isLoading } = useTransactions(portfolioId)

  if (!portfolioId) {
    return null
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Transaction History</CardTitle>
        <CardDescription>All buy and sell transactions for this portfolio.</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading && <p>Loading transactions...</p>}
        {!transactions || transactions.length === 0 ? (
          <p className="text-sm text-muted-foreground">No transactions yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="text-left py-2 px-2 font-medium">Date</th>
                  <th className="text-left py-2 px-2 font-medium">Type</th>
                  <th className="text-left py-2 px-2 font-medium">Symbol</th>
                  <th className="text-right py-2 px-2 font-medium">Quantity</th>
                  <th className="text-right py-2 px-2 font-medium">Price</th>
                  <th className="text-right py-2 px-2 font-medium">Total</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id} className="border-b hover:bg-muted/50">
                    <td className="py-2 px-2">{new Date(tx.executedAt).toLocaleDateString()}</td>
                    <td className="py-2 px-2">
                      <Badge variant={tx.type === 'BUY' ? 'default' : 'destructive'}>
                        {tx.type}
                      </Badge>
                    </td>
                    <td className="py-2 px-2 font-medium">{tx.symbol}</td>
                    <td className="py-2 px-2 text-right">{tx.quantity}</td>
                    <td className="py-2 px-2 text-right">{formatMoney(tx.price, tx.currency)}</td>
                    <td className="py-2 px-2 text-right font-medium">
                      {formatMoney(
                        (parseFloat(tx.quantity) * parseFloat(tx.price)).toString(),
                        tx.currency
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
