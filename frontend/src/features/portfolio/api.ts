import type { Portfolio, Transaction } from '@/types/portfolio'

const portfolioMock: Portfolio = {
  id: 'b2470ee8-f8d1-4dfc-8e62-2cf7c7bb8ae1',
  userId: '11111111-1111-1111-1111-111111111111',
  name: 'Growth Portfolio',
  description: 'Long-term diversified holdings',
  totalValue: '19630.58',
  createdAt: '2026-04-01T10:00:00.000Z',
}

const transactionsMock: Transaction[] = [
  {
    id: 'd07fd84f-a409-4f03-83b2-78a2f557f9b4',
    assetId: 'a75f6467-4bcd-40bc-b501-6ab8f621b950',
    portfolioId: 'b2470ee8-f8d1-4dfc-8e62-2cf7c7bb8ae1',
    type: 'BUY',
    quantity: '2',
    price: '181.01',
    currency: 'USD',
    fee: '1.99',
    notes: 'Monthly DCA',
    executedAt: '2026-04-01T10:00:00.000Z',
  },
]

export async function fetchPortfolio(): Promise<Portfolio> {
  return portfolioMock
}

export async function fetchTransactions(): Promise<Transaction[]> {
  return transactionsMock
}
