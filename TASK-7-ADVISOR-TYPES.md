# 📋 TASK #7 - Typy TypeScript dla AI Advisor Service

## 🎯 CEL
Refaktoryzować i rozszerzyć typy TypeScript dla domeny `ai-advisor-service` w katalogu `frontend/src/types/advisor/`, tworząc strukturę spójną z całym projektem i API backendowym.

---

## 📊 KONTEKST PROJEKTU

### Istniejące typy (DO REFAKTORYZACJI):
- `frontend/src/types/advisor.ts` — zbyt uproszczone, mix DTO
- `frontend/src/features/advisor/types.ts` — tylko `RecommendationSort`
- `frontend/src/features/advisor/api.ts` — zawiera API types, powinny być w `/types`

### Backend API (BASE TRUTH):
- **Endpoint**: `POST /api/recommendations` (ai-advisor-service)
- **Response**: Zawiera `RecommendationResponse` z llmResponse, news context, riskScore, createdAt
- **Dokumentacja**: `docs/api/recommendations.md` (przejrzyj!)

### Konwencje w PROJEKCIE:
- ✅ Struktura: `frontend/src/types/{serwis}/` (np. `market/`, `portfolio/`)
- ✅ Brak sufiksu `.types.ts` — proste nazwy: `llm.ts`, `news.ts`
- ✅ Guard functions (type guards): `isPortfolio()` z `portfolio.ts` — wzór do naśladowania
- ✅ Stałe z mapowań: `SENTIMENT_COLOR` musi być w `news.ts`
- ✅ Re-export: `index.ts` w każdym folderze, potem agregacja w `frontend/src/types/index.ts`

### Istniejące struktury DO INSPIRACJI:
```
frontend/src/types/
├── portfolio/
│   ├── portfolio.ts (z guard function)
│   ├── asset.ts
│   ├── transaction.ts
│   ├── user.ts
│   └── index.ts
├── market/
│   ├── symbol.ts
│   ├── price.ts
│   └── index.ts
└── index.ts (agregacja)
```

---

## 📝 SZCZEGÓŁOWE WYMAGANIA

### 1️⃣ Nowa struktura folderów
```
frontend/src/types/advisor/
├── llm.ts
├── news.ts
├── recommendation.ts
├── index.ts
```

---

### 2️⃣ llm.ts — Dostawcy LLM

```typescript
export interface LlmProvider {
  id: number
  name: string      // np. "Google Gemini Flash", "Claude Opus"
  modelId: string   // np. "gemini-1.5-flash", "claude-3-opus"
  active: boolean
  priority?: number // wyższa liczba = wyższy priorytet
}

export function isLlmProvider(obj: unknown): obj is LlmProvider {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'number' &&
    typeof candidate.name === 'string' &&
    typeof candidate.modelId === 'string' &&
    typeof candidate.active === 'boolean'
  )
}
```

---

### 3️⃣ news.ts — Newsy z kontekstem

```typescript
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
```

---

### 4️⃣ recommendation.ts — Rekomendacje od AI

```typescript
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
  modelName: string  // ekstrakcja z llmProvider.name
  llmResponse: string // pierwsze 200 znaków? — czytaj backend response
}

export function isRecommendation(obj: unknown): obj is Recommendation {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.portfolioId === 'string' &&
    typeof candidate.llmProvider === 'object' &&
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
```

---

### 5️⃣ advisor/index.ts — Re-export z folderu advisor/

```typescript
export type { LlmProvider } from './llm'
export { isLlmProvider } from './llm'

export type { NewsSentiment, NewsItem } from './news'
export { SENTIMENT_COLOR, isNewsItem } from './news'

export type { Recommendation, RecommendationRequest, RecommendationSummary } from './recommendation'
export { isRecommendation, isRecommendationSummary } from './recommendation'
```

---

### 6️⃣ Aktualizacja frontend/src/types/index.ts

W pliku `frontend/src/types/index.ts` zmień:

```typescript
// Istniejące:
export * from '@/types/portfolio'
export * from '@/types/market'

// DODAJ:
export * from '@/types/advisor'  // ← NOWY RE-EXPORT
```

---

## ✅ CHECKLIST IMPLEMENTACJI

- [ ] Stworzyć folder `frontend/src/types/advisor/`
- [ ] Zdefiniować `llm.ts` z `LlmProvider` i guard function
- [ ] Zdefiniować `news.ts` z `NewsSentiment`, `SENTIMENT_COLOR`, `NewsItem` i guard function
- [ ] Zdefiniować `recommendation.ts` z `Recommendation`, `RecommendationRequest`, `RecommendationSummary` i guard functions
- [ ] Stworzyć `index.ts` z re-exportem wszystkich typów i funkcji
- [ ] Dodać re-export do `frontend/src/types/index.ts`
- [ ] Usunąć lub oznaczyć jako deprecated istniejący `frontend/src/types/advisor.ts`
- [ ] Sprawdzić czy `frontend/src/features/advisor/api.ts` poprawnie importuje typy z nowej lokalizacji
- [ ] Sprawdzić czy komponenty (`AdvisorRecommendations.tsx`, etc.) poprawnie używają nowych typów
- [ ] Uruchomić `tsc --noEmit` — brak błędów kompilacji ✅

---

## 🔗 DODATKOWE WYTYCZNE

1. **Guard Functions** — OBOWIĄZKOWE. Wzór: `portfolio.ts` ma `isPortfolio()`
2. **Importy w recommendation.ts** — Pamiętaj o relative imports do `./llm` i `./news`
3. **SENTIMENT_COLOR** — To map dla Tailwind — musi być exportowana (używana w komponentach)
4. **BigDecimal → string** — `riskScore` jest stringiem, bo BigDecimal nie istnieje w TypeScript (precyzja)
5. **Opcjonalne pola** — Oznacz `?:` (sentiment, url, expiresAt, promptSummary, riskScore, news, priority)
6. **Backward compatibility** — Jeśli coś używa starego `frontend/src/types/advisor.ts`, dostosuj importy

---

## 📦 OUTPUT (1 Commit)

```bash
git add frontend/src/types/advisor/ frontend/src/types/index.ts
git commit -m "chore(types): restructure advisor types with llm, news, recommendation entities"
```

Powinno zmienić:
- ✅ Usunąć/oznaczyć `frontend/src/types/advisor.ts` (deprecated)
- ✅ Stworzyć `frontend/src/types/advisor/llm.ts`
- ✅ Stworzyć `frontend/src/types/advisor/news.ts`
- ✅ Stworzyć `frontend/src/types/advisor/recommendation.ts`
- ✅ Stworzyć `frontend/src/types/advisor/index.ts`
- ✅ Aktualizować `frontend/src/types/index.ts`
- ✅ Aktualizować importy w `frontend/src/features/advisor/api.ts` (jeśli trzeba)
- ✅ Aktualizować importy w komponentach advisor (jeśli trzeba)

---

**Gotowy do wdrażania!** 🚀
