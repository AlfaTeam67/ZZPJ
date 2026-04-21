export type TransactionType = 'BUY' | 'SELL';

export interface Transaction {
  id: string;
  assetId: string;
  portfolioId: string;
  type: TransactionType;
  quantity: string;  // string — BigDecimal
  price: string;     // string — BigDecimal
  currency: string;
  fee?: string;
  executedAt: string;
  notes?: string;
}

export function isTransaction(obj: any): obj is Transaction {
  return obj !== null && typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.assetId === 'string' &&
    typeof obj.portfolioId === 'string' &&
    ['BUY', 'SELL'].includes(obj.type) &&
    typeof obj.quantity === 'string' &&
    typeof obj.price === 'string' &&
    typeof obj.currency === 'string' &&
    typeof obj.executedAt === 'string';
}
