export type AssetType = 'STOCK' | 'CRYPTO' | 'BOND';

export interface Asset {
  id: string;
  portfolioId: string;
  type: AssetType;
  symbol: string;
  quantity: string;      // string — BigDecimal z backendu, NIGDY number
  avgBuyPrice: string;   // string — BigDecimal
  currency: string;
  addedAt: string;
  updatedAt?: string;
  currentValue?: string; // opcjonalne, wyliczane przez backend
}

export function isAsset(obj: any): obj is Asset {
  return obj !== null && typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.portfolioId === 'string' &&
    ['STOCK', 'CRYPTO', 'BOND'].includes(obj.type) &&
    typeof obj.symbol === 'string' &&
    typeof obj.quantity === 'string' &&
    typeof obj.avgBuyPrice === 'string' &&
    typeof obj.currency === 'string' &&
    typeof obj.addedAt === 'string';
}
