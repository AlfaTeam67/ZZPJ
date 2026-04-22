export type UserRole = 'USER' | 'PREMIUM_ADVISOR'

export interface User {
  id: string // UUID — Keycloak subject
  email: string
  role: UserRole
  createdAt: string // ISO 8601
  updatedAt?: string
}

export function isUser(obj: unknown): obj is User {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.email === 'string' &&
    ['USER', 'PREMIUM_ADVISOR'].includes(candidate.role as string) &&
    typeof candidate.createdAt === 'string'
  )
}
