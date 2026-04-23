import type { Asset } from './asset'

export interface Portfolio {
  id: string
  userId: string
  name: string
  description?: string | null
  createdAt: string
  updatedAt?: string
  assets?: Asset[]
  totals?: Record<string, string> // Map of currency to total value
}

export function isPortfolio(obj: unknown): obj is Portfolio {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.name === 'string' &&
    typeof candidate.createdAt === 'string'
  )
}
