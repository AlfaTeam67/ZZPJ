import type { Asset } from './asset';

export interface Portfolio {
  id: string;
  userId: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
  assets?: Asset[];
  totalValue?: string; // wyliczana przez backend
}

export function isPortfolio(obj: any): obj is Portfolio {
  return obj !== null && typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.userId === 'string' &&
    typeof obj.name === 'string' &&
    typeof obj.createdAt === 'string';
}
