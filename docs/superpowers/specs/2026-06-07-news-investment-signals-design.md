# News Investment Signals — Design Spec

**Date:** 2026-06-07  
**Feature:** Visual investment signals (BUY / HOLD / SELL) in the AI Advisor news section  
**Status:** Approved

---

## Problem

The AI Advisor returns news articles (`newsContext`) with a `sentiment` field, but the current UI renders them as plain links with no visual weight. Users cannot quickly scan which news items suggest buying, holding, or selling an asset.

---

## Goal

Each news card in the `newsContext` section of `AdvisorRecommendations` displays a prominent color-coded investment signal derived from the article's sentiment. Users should understand the signal at a glance without reading the headline.

---

## Design Decisions

| Question | Decision |
|---|---|
| Visual prominence | Full colored card — background + border tinted by signal color |
| Signal source | Frontend maps `sentiment` string → signal (no backend changes) |
| Labels | `↑ BUY` (green) / `→ HOLD` (yellow) / `↓ SELL` (red) |

### Sentiment → Signal mapping

| `sentiment` value | Signal | Label | Color |
|---|---|---|---|
| `POSITIVE` | BUY | `↑ BUY` | `text-green-400`, `bg-green-500/10`, `border-green-500/35` |
| `NEUTRAL` | HOLD | `→ HOLD` | `text-yellow-400`, `bg-yellow-500/10`, `border-yellow-500/35` |
| `NEGATIVE` | SELL | `↓ SELL` | `text-red-400`, `bg-red-500/10`, `border-red-500/35` |
| anything else | HOLD | `→ HOLD` | yellow (safe fallback) |

Matching is case-insensitive. Unknown values fall back to HOLD.

---

## Architecture

### New file: `NewsSignalCard.tsx`

```
frontend/src/features/advisor/components/NewsSignalCard.tsx
```

Single-responsibility component. Accepts one `NewsItem` and renders the colored card. Contains the `sentimentToSignal` mapping function (not extracted — it's only used here).

**Props:**
```ts
interface NewsSignalCardProps {
  item: NewsItem  // imported from @/features/advisor/api
}
```

**Internal signal config shape:**
```ts
interface SignalConfig {
  label: string       // e.g. "↑ BUY"
  cardClass: string   // Tailwind classes for card background + border
  labelClass: string  // Tailwind classes for label text color
}
```

### Modified file: `AdvisorRecommendations.tsx`

Replace the existing `newsContext` `<ul>` rendering with `<NewsSignalCard>` per item. The section header ("News Context") stays unchanged.

---

## Card Layout

```
┌─────────────────────────────────────────────┐  ← tinted bg + border
│ ↑ BUY                              [AAPL]  │  ← signal label + symbol badge
│ Apple reports record Q2 results            │  ← headline (12px)
│ Reuters                                    │  ← source (10px muted)
└─────────────────────────────────────────────┘
```

The card is a clickable `<a>` tag (opens article URL in new tab). Hover darkens the background slightly.

---

## Error Handling

- Empty `newsContext` array: section not rendered (existing behavior, unchanged).
- Missing `sentiment` field: treated as `NEUTRAL` → HOLD signal.
- Missing `url`: card renders without href (non-clickable, no broken link).

---

## Testing

Add `NewsSignalCard.test.tsx` alongside the component:

1. Renders `↑ BUY` label and green styling for `sentiment: "POSITIVE"`
2. Renders `→ HOLD` label and yellow styling for `sentiment: "NEUTRAL"`
3. Renders `↓ SELL` label and red styling for `sentiment: "NEGATIVE"`
4. Falls back to HOLD for unknown sentiment values
5. Renders symbol badge and headline text correctly
6. Card is a link when `url` is provided; renders without href when `url` is absent

---

## Out of Scope

- Backend changes (no new fields, no API modifications)
- Filtering/sorting news by signal
- Tooltips or explanations of what BUY/HOLD/SELL means
- i18n for signal labels (intentionally English — standard market terminology)
