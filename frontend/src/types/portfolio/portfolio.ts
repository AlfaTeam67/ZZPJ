import type { Asset } from './asset'

export interface Portfolio {
  id: string
  userId: string
  name: string
  description?: string | null
  totals: Record<string, string>
  createdAt: string
  updatedAt?: string
  assets?: Asset[]
}

export function isPortfolio(obj: unknown): obj is Portfolio {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.name === 'string' &&
    candidate.totals !== null &&
    typeof candidate.totals === 'object' &&
    typeof candidate.createdAt === 'string'
  )
}
