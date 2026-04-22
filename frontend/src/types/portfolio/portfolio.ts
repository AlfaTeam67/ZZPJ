import type { Asset } from './asset'

export interface Portfolio {
  id: string
  userId: string
  name: string
  description?: string
  currency: string
  createdAt: string
  updatedAt?: string
  assets?: Asset[]
  totalValue?: string // calculated by the backend
}

export function isPortfolio(obj: unknown): obj is Portfolio {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.name === 'string' &&
    typeof candidate.currency === 'string' &&
    typeof candidate.createdAt === 'string'
  )
}
