export type NewsSentiment = 'POZYTYWNY' | 'NEUTRALNY' | 'NEGATYWNY'

export const SENTIMENT_COLOR: Record<NewsSentiment, string> = {
  POZYTYWNY: 'text-green-600',
  NEUTRALNY: 'text-gray-500',
  NEGATYWNY: 'text-red-600',
}

export interface NewsItem {
  id: string
  headline: string
  source: string
  url?: string
  sentiment?: NewsSentiment
  fetchedAt: string         // ISO datetime
  expiresAt?: string        // opcjonalnie
}

export function isNewsItem(obj: unknown): obj is NewsItem {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>
  return (
    typeof candidate.id === 'string' &&
    typeof candidate.headline === 'string' &&
    typeof candidate.source === 'string' &&
    typeof candidate.fetchedAt === 'string'
  )
}
