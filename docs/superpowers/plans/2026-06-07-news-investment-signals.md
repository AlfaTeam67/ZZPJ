# News Investment Signals Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace plain news links in AI Advisor with color-coded signal cards (↑ BUY / → HOLD / ↓ SELL) derived from the `sentiment` field.

**Architecture:** New `NewsSignalCard` component maps `sentiment` → visual signal config and renders a colored card. `AdvisorRecommendations` replaces its existing `<ul>` news list with `<NewsSignalCard>` per item. No backend changes.

**Tech Stack:** React 18, TypeScript, Tailwind CSS, Vitest + React Testing Library

---

## File Map

| Action | Path |
|---|---|
| **Create** | `frontend/src/features/advisor/components/NewsSignalCard.tsx` |
| **Create** | `frontend/src/features/advisor/components/NewsSignalCard.test.tsx` |
| **Modify** | `frontend/src/features/advisor/components/AdvisorRecommendations.tsx` |

---

### Task 1: NewsSignalCard — tests first

**Files:**
- Create: `frontend/src/features/advisor/components/NewsSignalCard.test.tsx`

- [ ] **Step 1: Write the failing tests**

Create `frontend/src/features/advisor/components/NewsSignalCard.test.tsx`:

```tsx
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { NewsSignalCard } from './NewsSignalCard'
import type { NewsItem } from '@/features/advisor/api'

const base: NewsItem = {
  id: '1',
  headline: 'Apple reports record Q2 results',
  source: 'Reuters',
  provider: 'finnhub',
  symbol: 'AAPL',
  url: 'https://reuters.com/apple-q2',
  sentiment: 'POSITIVE',
}

describe('NewsSignalCard', () => {
  it('renders ↑ BUY label for POSITIVE sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'POSITIVE' }} />)
    expect(screen.getByText('↑ BUY')).toBeInTheDocument()
  })

  it('renders → HOLD label for NEUTRAL sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'NEUTRAL' }} />)
    expect(screen.getByText('→ HOLD')).toBeInTheDocument()
  })

  it('renders ↓ SELL label for NEGATIVE sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'NEGATIVE' }} />)
    expect(screen.getByText('↓ SELL')).toBeInTheDocument()
  })

  it('falls back to HOLD for unknown sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'UNKNOWN' }} />)
    expect(screen.getByText('→ HOLD')).toBeInTheDocument()
  })

  it('renders symbol badge and headline', () => {
    render(<NewsSignalCard item={base} />)
    expect(screen.getByText('AAPL')).toBeInTheDocument()
    expect(screen.getByText('Apple reports record Q2 results')).toBeInTheDocument()
  })

  it('renders as a link when url is provided', () => {
    render(<NewsSignalCard item={base} />)
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', 'https://reuters.com/apple-q2')
    expect(link).toHaveAttribute('target', '_blank')
  })

  it('renders without href when url is empty', () => {
    render(<NewsSignalCard item={{ ...base, url: '' }} />)
    const link = screen.queryByRole('link')
    expect(link).toBeNull()
  })
})
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
cd frontend && npx vitest run src/features/advisor/components/NewsSignalCard.test.tsx
```

Expected: all tests FAIL with `Cannot find module './NewsSignalCard'`

---

### Task 2: NewsSignalCard — implementation

**Files:**
- Create: `frontend/src/features/advisor/components/NewsSignalCard.tsx`

- [ ] **Step 1: Create the component**

Create `frontend/src/features/advisor/components/NewsSignalCard.tsx`:

```tsx
import { cn } from '@/lib/utils'
import type { NewsItem } from '@/features/advisor/api'

interface SignalConfig {
  label: string
  cardClass: string
  labelClass: string
}

function sentimentToSignal(sentiment: string): SignalConfig {
  switch (sentiment.toUpperCase()) {
    case 'POSITIVE':
      return {
        label: '↑ BUY',
        cardClass: 'bg-green-500/10 border-green-500/35',
        labelClass: 'text-green-400',
      }
    case 'NEGATIVE':
      return {
        label: '↓ SELL',
        cardClass: 'bg-red-500/10 border-red-500/35',
        labelClass: 'text-red-400',
      }
    default:
      return {
        label: '→ HOLD',
        cardClass: 'bg-yellow-500/10 border-yellow-500/35',
        labelClass: 'text-yellow-400',
      }
  }
}

interface NewsSignalCardProps {
  item: NewsItem
}

export function NewsSignalCard({ item }: NewsSignalCardProps) {
  const signal = sentimentToSignal(item.sentiment ?? '')

  const content = (
    <div className={cn('rounded-xl border p-3 transition-colors hover:brightness-110', signal.cardClass)}>
      <div className="mb-1.5 flex items-center justify-between gap-2">
        <span className={cn('text-xs font-bold tracking-wide', signal.labelClass)}>
          {signal.label}
        </span>
        <span className="rounded px-1.5 py-0.5 text-[10px] font-semibold text-blue-300 bg-blue-900/40">
          {item.symbol}
        </span>
      </div>
      <p className="text-xs leading-relaxed text-foreground">{item.headline}</p>
      <p className="mt-1 text-[10px] text-muted-foreground">{item.source}</p>
    </div>
  )

  if (!item.url) return content

  return (
    <a href={item.url} target="_blank" rel="noopener noreferrer" className="block">
      {content}
    </a>
  )
}
```

- [ ] **Step 2: Run tests — all should pass**

```bash
cd frontend && npx vitest run src/features/advisor/components/NewsSignalCard.test.tsx
```

Expected: 7 tests PASS

- [ ] **Step 3: Commit**

```bash
cd frontend && git add src/features/advisor/components/NewsSignalCard.tsx src/features/advisor/components/NewsSignalCard.test.tsx
git commit -m "feat: add NewsSignalCard component with BUY/HOLD/SELL signals"
```

---

### Task 3: Wire into AdvisorRecommendations

**Files:**
- Modify: `frontend/src/features/advisor/components/AdvisorRecommendations.tsx`

- [ ] **Step 1: Replace the newsContext rendering**

In `AdvisorRecommendations.tsx`, find the `newsContext` block (around line 118–133):

```tsx
{data.newsContext && data.newsContext.length > 0 && (
  <div className="space-y-2 pt-2">
    <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
      {t('news-context')}
    </p>
    <ul className="space-y-1.5">
      {data.newsContext.slice(0, 4).map((item) => (
        <li key={item.id}>
          <a
            href={item.url}
            target="_blank"
            rel="noopener noreferrer"
            className="block rounded-md border bg-muted/30 px-3 py-2 text-xs transition-colors hover:bg-muted/60"
          >
            <span className="font-medium">{item.symbol}</span>
            <span className="mx-1.5 text-muted-foreground">·</span>
            {item.headline}
            <span className="ml-1.5 text-muted-foreground/60">({item.source})</span>
          </a>
        </li>
      ))}
    </ul>
  </div>
)}
```

Replace with:

```tsx
{data.newsContext && data.newsContext.length > 0 && (
  <div className="space-y-2 pt-2">
    <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
      {t('news-context')}
    </p>
    <ul className="space-y-2">
      {data.newsContext.slice(0, 4).map((item) => (
        <li key={item.id}>
          <NewsSignalCard item={item} />
        </li>
      ))}
    </ul>
  </div>
)}
```

Also add the import at the top of the file (after existing imports):

```tsx
import { NewsSignalCard } from './NewsSignalCard'
```

- [ ] **Step 2: Run full advisor-related tests**

```bash
cd frontend && npx vitest run src/features/advisor
```

Expected: all tests PASS (no regressions)

- [ ] **Step 3: Run the full test suite**

```bash
cd frontend && npx vitest run
```

Expected: all tests PASS

- [ ] **Step 4: Commit**

```bash
cd frontend && git add src/features/advisor/components/AdvisorRecommendations.tsx
git commit -m "feat: wire NewsSignalCard into AdvisorRecommendations"
```

---

## Done ✓

After Task 3 completes, the news section in AI Advisor displays colored BUY/HOLD/SELL signal cards. Verify manually by generating a recommendation with at least one news item in the response.
