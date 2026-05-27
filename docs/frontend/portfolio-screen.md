# 💼 Portfolio Screen

## Przegląd

Ekran portfela składa się z dwóch widoków:
- **Lista portfeli** (`/portfolio`) — przegląd wszystkich portfeli użytkownika
- **Szczegóły portfela** (`/portfolio/:id`) — aktywa, transakcje, formularze

## Architektura komponentów

```
pages/
├── PortfolioPage.tsx          # Lista portfeli (routing: /portfolio)
└── PortfolioDetailsPage.tsx   # Szczegóły (routing: /portfolio/:id)

features/portfolio/components/
├── PortfolioList.tsx          # Lista kart portfeli (fetch + render)
├── PortfolioCard.tsx          # Pojedyncza karta portfela
├── PortfolioStats.tsx         # KPI hero (suma, liczba portfeli, aktywów)
├── AssetList.tsx              # Lista aktywów w portfelu
├── TransactionHistory.tsx     # Tabela transakcji
├── CreatePortfolioForm.tsx    # Formularz tworzenia portfela
├── AddAssetForm.tsx           # Formularz dodawania aktywa
└── TransactionForm.tsx        # Formularz nowej transakcji
```

## Stany UI

Każdy komponent obsługuje 3 stany:
- **Loading** — `SectionSkeleton` (reużywalny, `src/components/ui/SectionSkeleton.tsx`)
- **Empty** — komunikat z CTA
- **Error** — komunikat z przyciskiem retry lub linkiem powrotu

## Styl wizualny

Wzorzec z DashboardPage:
- Sekcje: `rounded-2xl border border-border/40 bg-card/60 p-6`
- Karty aktywów: `rounded-xl border border-border/30 p-4`
- KPI: grid `sm:grid-cols-3` z osobnymi kartami
- Typografia: `text-3xl font-bold tracking-tight` dla tytułów

## Hooks i API

| Hook | Źródło | Opis |
|------|--------|------|
| `usePortfolios()` | `features/portfolio/hooks/usePortfolios.ts` | Lista portfeli |
| `useAssets(portfolioId)` | `features/portfolio/hooks/useAssets.ts` | Aktywa portfela |
| `useTransactions(portfolioId)` | `features/portfolio/hooks/useTransactions.ts` | Transakcje |

API contract: `features/portfolio/api.ts` — CRUD portfeli, aktywów, transakcji.

## i18n

Namespace: `portfolio`. Wszystkie stringi w `src/i18n/locales/{pl,en}/portfolio.json`.

## Formatowanie walut

Używamy `formatMoney` z `utils/formatMoney.ts` (prosty format) w komponentach portfela.
`formatCurrency` z `utils/formatNumber.ts` (z locale) w Dashboard.
