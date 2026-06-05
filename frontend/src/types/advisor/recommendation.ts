import type { LlmProvider } from './llm'
import type { NewsItem } from './news'

/**
 * Pełna rekomendacja z kontekstem LLM i newsami.
 * Odpowiada RecommendationResponse z backendu.
 */
export interface Recommendation {
  id: string
  userId: string
  portfolioId: string
  llmProvider: LlmProvider        // nie string enum, pełny obiekt
  promptSummary?: string          // podsumowanie co było w promptu
  llmResponse: string             // pełna odpowiedź modelu AI
  riskScore?: string              // BigDecimal (0.00–10.00), string dla precyzji
  createdAt: string               // ISO datetime
  news?: NewsItem[]               // kontekst newsów użyty do generacji
}

/**
 * Request do generowania rekomendacji.
 * Minimalistyczny — wystarczy portfolioId.
 */
export interface RecommendationRequest {
  portfolioId: string
}

/**
 * Skrócona wersja do list/preview.
 * Używana w dashbordzie, listach rekomendacji.
 */
export interface RecommendationSummary {
  id: string
  createdAt: string
  riskScore?: string
  modelName: string   // ekstrakcja z llmProvider.name
  llmResponse: string // pierwsze 200 znaków odpowiedzi modelu
}

export function isRecommendation(obj: unknown): obj is Recommendation {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>
  return (
    typeof candidate.id === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.portfolioId === 'string' &&
    typeof candidate.llmProvider === 'object' &&
    candidate.llmProvider !== null &&
    typeof candidate.llmResponse === 'string' &&
    typeof candidate.createdAt === 'string'
  )
}

export function isRecommendationSummary(obj: unknown): obj is RecommendationSummary {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>
  return (
    typeof candidate.id === 'string' &&
    typeof candidate.createdAt === 'string' &&
    typeof candidate.modelName === 'string' &&
    typeof candidate.llmResponse === 'string'
  )
}
