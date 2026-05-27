# Portfolio Screen (Figma) + i18n PL/EN

**Data:** 2026-05-27 22:27  
**Branch:** `feature/ALF-XX/portfolio-figma-i18n` (do potwierdzenia ID Linear)  
**Status:** _czeka na zatwierdzenie_

---

## 1. Cel zadania

Dwa cele zrealizowane w jednym etapie (decyzja użytkownika):

1. **Portfolio screen zgodny z Figmą** — wizualna spójność z `DashboardPage` (przyjętym jako referencja Figmy w repo, brak zewnętrznego linku do Figmy). Stany loading / empty / error spójne z resztą aplikacji.
2. **i18n PL/EN** — dwujęzyczność całej aplikacji z przełącznikiem języka, persistencja wyboru w `localStorage`.

## 2. Zakres (Definition of Done)

### 2.1 i18n
- [ ] `react-i18next` zainstalowany i skonfigurowany.
- [ ] Lokalizacje `en` + `pl` jako oddzielne JSON (namespacy: `common`, `nav`, `auth`, `portfolio`, `dashboard`, `market`, `advisor`).
- [ ] Język startowy: `i18next-browser-languagedetector` — kolejność detekcji: `localStorage` → `navigator.language` → fallback `pl`. Przy pierwszej wizycie użytkownik z EN-przeglądarką zobaczy EN (zachowanie celowe). Persistencja wyboru w `localStorage` (`fin-insight.lang`).
- [ ] Przełącznik języka w `AppHeader` (mały toggle PL/EN). Działa też na `LoginPage` (osobny mały toggle w prawym górnym).
- [ ] Wszystkie hardkodowane stringi z poniższych plików przeniesione do tłumaczeń:
  - `AppSidebar.tsx`, `AppHeader.tsx`, `LoginPage.tsx`
  - `DashboardPage` + komponenty (`PortfolioMetricHero`, `PerformanceChart`, `WatchlistCard`, `AdvisorSnapshotCard`)
  - Nowy Portfolio screen (pełny, patrz 2.2)
  - `MarketPage`, `AdvisorPage`, `AdvisorRecommendations` — minimum tytuł + nagłówki kart (reszta poza scope, ale sygnalizowana TODO `// i18n: market labels`)
- [ ] Walidacja (build + lint + testy) z zachowaniem polskich stringów w testach (testy ustawiają `i18n.changeLanguage('pl')` w setupie albo asercje robione na dataKey/role).

### 2.2 Portfolio screen — przebudowa wizualna

**Plik `src/pages/PortfolioPage.tsx` (lista portfeli):**
- [ ] Hero z tytułem + opisem (typografia jak `DashboardPage`).
- [ ] Sekcja KPI: agregat sumy wszystkich portfeli (Total Net Worth + liczba portfeli + liczba aktywów).
- [ ] Lista portfeli w stylu `rounded-2xl border border-border/40 bg-card/60` — każda karta: nazwa, opis, totals (`formatCurrency` PL/EN), liczba aktywów, link "Szczegóły / Details", akcja Delete.
- [ ] Sekcja "Nowy portfel" w aside (sticky). 
- [ ] Stany: skeleton (loading), empty (CTA do utworzenia), error (sygnał z retry).

**Plik `src/pages/PortfolioDetailsPage.tsx` (szczegóły):**
- [ ] Breadcrumb + tytuł portfela (typografia hero) + opcjonalny opis.
- [ ] KPI hero: Total value, Profit/loss, Procentowa zmiana, Liczba aktywów, ostatnia aktualizacja. Kolorowanie zysk/strata semantyczne (`text-success` / `text-destructive`).
- [ ] Sekcja `Assets` — lista kart z avatarem (inicjały symbolu w kółku), badge typu, ilość, średnia cena, wartość (preferowane `currentValue` z `valuation` jeśli dostępne), zmiana % vs avg buy. Akcja Remove.
- [ ] Sekcja `Transaction history` — tabela responsywna (przewijana na mobile), kolumny: Data, Typ (badge BUY/SELL semantycznie kolorowane), Symbol, Quantity, Price, Total, Fee. Sticky header.
- [ ] Aside: `AddAssetForm` + `TransactionForm` (przebudowane wizualnie).
- [ ] Stany: skeleton (loading), empty (brak aktywów / brak transakcji — różne komunikaty), error (z linkiem powrotu).

**Komponenty (do przebudowania):**
- [ ] `PortfolioList.tsx` — nowy styl, skeleton, empty, error.
- [ ] `AssetList.tsx` — nowy styl, skeleton, empty z CTA "Dodaj pierwsze aktywo".
- [ ] `TransactionHistory.tsx` — nowy styl, skeleton, empty.
- [ ] `TransactionForm.tsx` — restyle (spójność z formami z `LoginPage`/Dashboard).
- [ ] `AddAssetForm.tsx` — restyle, ustandaryzowane labelki.
- [ ] `CreatePortfolioForm.tsx` — restyle.
- [ ] _Usunąć_ `PortfolioOverview.tsx` (nieużywany w obecnym App.tsx; potwierdzić grep i usunąć — Scout's Rule).

### 2.3 Format liczb i waluty
- [ ] Wycofać `formatMoney` z ekranu Portfela. Używać `formatCurrency` / `formatPercent` / `formatSignedCurrency` z `utils/formatNumber.ts`.
- [ ] `formatCurrency` zaktualizowany — przyjmuje **wymagany** parametr `locale` (`'pl-PL'` / `'en-US'`). Komponenty przekazują go z `i18n.language === 'pl' ? 'pl-PL' : 'en-US'` (przez `useTranslation`). Utils pozostaje czysty — zero importu i18next wewnątrz `formatNumber.ts`.
- [ ] `formatMoney` zostaje w `MarketPage` i `AdvisorRecommendations` (poza scope) — nie ruszamy.

### 2.4 Testy
- [ ] `PortfolioPage.test.tsx` — adaptacja do nowych etykiet (i18n key zamiast literałów; `i18n.changeLanguage('pl')` w setupie).
- [ ] `PortfolioDetailsPage.test.tsx` — adaptacja.
- [ ] Nowy: `i18n.test.tsx` — weryfikuje że przełącznik zmienia widoczne stringi.
- [ ] Nowy: `PortfolioList.skeleton.test.tsx` — weryfikuje rendering skeletonu i empty state.
- [ ] Nowy: `formatNumber.test.ts` — weryfikuje obsługę locale PL/EN.
- [ ] Wszystkie testy zielone (`npm run test -- --run`).

### 2.5 Dokumentacja
- [ ] `docs/frontend/i18n.md` — nowy: jak dodawać tłumaczenia, struktura namespace'ów, jak używać `useTranslation`.
- [ ] `docs/frontend/portfolio-screen.md` — nowy: opis komponentów ekranu Portfel, hierarchia, hooks, API contract.
- [ ] `docs/architecture.md` — krótki dopisek o frontendowym i18n (sekcja "Frontend").
- [ ] `docs/conventions.md` — sekcja "i18n keys" (kebab-case namespacy, dot-notation klucze, brak literałów w JSX).

### 2.6 Zasady (mc-clean-code)
- [ ] Brak importów wewnątrz funkcji.
- [ ] SOLID/DRY: skeletony jako reużywalny komponent (`<SectionSkeleton lines={n} />`) jeśli >1 użycie.
- [ ] KISS: jedna stała `LANGUAGES = ['pl','en']`, brak overengineeringu.
- [ ] Czytelne nazwy: `useLanguage()` hook zamiast bezpośredniego `i18n.changeLanguage`.
- [ ] Brak `console.log`, brak `any`.

## 3. Strategia techniczna

### 3.1 i18n
- **Biblioteka:** `react-i18next` + `i18next` + `i18next-browser-languagedetector`.
- **Struktura plików:**
  ```
  src/i18n/
    config.ts                    # init
    types.ts                     # TS types z resources (typed keys)
    locales/
      pl/
        common.json
        nav.json
        auth.json
        portfolio.json
        dashboard.json
        market.json
        advisor.json
      en/
        common.json
        ...
  ```
- **Init:** w `main.tsx` przed `createRoot` (synchronous).
- **Hook usage:** `const { t } = useTranslation('portfolio')` z namespace per feature.
- **Typed keys:** `react-i18next` z `declare module` dla type safety.

### 3.2 Portfolio screen
- Replikacja wzorca z `DashboardPage`: `<section className="rounded-2xl border border-border/40 bg-card/60 p-6">` jako container.
- Skeleton inspiration: `PortfolioMetricHero` / `WatchlistCard` (`animate-pulse rounded bg-muted`).
- Error state: rozsądnie spójny z resztą — komunikat + przycisk "Spróbuj ponownie / Try again".
- Empty state: ikona z `@hugeicons/core-free-icons` + tytuł + opis + CTA.

### 3.3 Routing zmiany
- Bez zmian. `/portfolio` lista, `/portfolio/:id` szczegóły.

## 4. Lista plików (zmodyfikowanych / nowych)

### Nowe (26):
1. `frontend/src/i18n/config.ts`
2. `frontend/src/i18n/types.ts`
3. `frontend/src/i18n/hooks/useLanguage.ts`
4. `frontend/src/i18n/locales/pl/common.json`
5. `frontend/src/i18n/locales/pl/nav.json`
6. `frontend/src/i18n/locales/pl/auth.json`
7. `frontend/src/i18n/locales/pl/portfolio.json`
8. `frontend/src/i18n/locales/pl/dashboard.json`
9. `frontend/src/i18n/locales/pl/market.json`
10. `frontend/src/i18n/locales/pl/advisor.json`
11. `frontend/src/i18n/locales/en/common.json`
12. `frontend/src/i18n/locales/en/nav.json`
13. `frontend/src/i18n/locales/en/auth.json`
14. `frontend/src/i18n/locales/en/portfolio.json`
15. `frontend/src/i18n/locales/en/dashboard.json`
16. `frontend/src/i18n/locales/en/market.json`
17. `frontend/src/i18n/locales/en/advisor.json`
18. `frontend/src/components/layout/LanguageSwitcher.tsx`
19. `frontend/src/features/portfolio/components/PortfolioStats.tsx` (KPI hero szczegółów)
20. `frontend/src/features/portfolio/components/PortfolioCard.tsx` (pojedyncza karta na liście)
21. `frontend/src/features/portfolio/components/SectionSkeleton.tsx` (reużywalny)
22. `frontend/src/i18n/i18n.test.tsx`
23. `frontend/src/features/portfolio/PortfolioList.skeleton.test.tsx`
24. `frontend/src/utils/formatNumber.test.ts`
25. `docs/frontend/i18n.md`
26. `docs/frontend/portfolio-screen.md`

### Zmodyfikowane (~16):
- `frontend/package.json` (+3 deps)
- `frontend/src/main.tsx` (import konfigu i18n)
- `frontend/src/App.tsx` (bez zmian funkcjonalnych — sprawdzić)
- `frontend/src/components/layout/AppSidebar.tsx` (i18n)
- `frontend/src/components/layout/AppHeader.tsx` (i18n + LanguageSwitcher)
- `frontend/src/pages/LoginPage.tsx` (i18n + LanguageSwitcher mini)
- `frontend/src/pages/DashboardPage.tsx` (i18n)
- `frontend/src/pages/PortfolioPage.tsx` (rewrite + i18n)
- `frontend/src/pages/PortfolioDetailsPage.tsx` (rewrite + i18n)
- `frontend/src/pages/MarketPage.tsx` (i18n minimum)
- `frontend/src/pages/AdvisorPage.tsx` (i18n minimum)
- `frontend/src/features/portfolio/components/PortfolioList.tsx` (rewrite)
- `frontend/src/features/portfolio/components/AssetList.tsx` (rewrite)
- `frontend/src/features/portfolio/components/TransactionHistory.tsx` (rewrite)
- `frontend/src/features/portfolio/components/TransactionForm.tsx` (restyle + i18n)
- `frontend/src/features/portfolio/components/AddAssetForm.tsx` (restyle + i18n)
- `frontend/src/features/portfolio/components/CreatePortfolioForm.tsx` (restyle + i18n)
- `frontend/src/features/dashboard/components/*` (4 pliki — i18n)
- `frontend/src/utils/formatNumber.ts` (parametr locale)
- `frontend/src/features/portfolio/PortfolioPage.test.tsx`
- `frontend/src/features/portfolio/PortfolioDetailsPage.test.tsx`
- `docs/architecture.md` (+ sekcja i18n)
- `docs/conventions.md` (+ sekcja i18n keys)

### Usunięte (1):
- `frontend/src/features/portfolio/components/PortfolioOverview.tsx` (martwy kod, niegdzie nieimportowany)

**Łącznie:** ~49 plików, szacunkowo **1400–1800 zmienionych/dodanych linii**.

## 5. Wyzwania i decyzje

| Wyzwanie | Decyzja | Alternatywa |
|---|---|---|
| Wybór biblioteki i18n | `react-i18next` (najpopularniejsza, dobra dokumentacja, type-safe) | `react-intl` (FormatJS) — bardziej formalny, ale cięższy |
| Persistencja języka | `localStorage` + key `fin-insight.lang` | Cookie (niepotrzebne, frontend SPA) |
| Domyślny język | Browser language wins (`navigator.language` → `pl` fallback). EN-user widzi EN od razu. Wybór persystuje w localStorage. | Hardcode `pl` jako default (ignoruje browser language) |
| Locale dla `formatCurrency` | Explicit `locale` param — komponenty przekazują z `i18n.language`. Utils czysty, zero importu i18next. | Czytać `i18next.language` wewnątrz utils (coupling warstw) |
| Brak Figmy | Replikujemy wzorzec `DashboardPage` (decyzja użytkownika z rozmowy) | — |
| Skeleton loadery | Nowy reużywalny `SectionSkeleton` jeśli >1 użycie; lokalne jeśli unikalne | DRY na siłę |
| Testy istniejące używają polskich stringów | Setup testowy: `i18n.changeLanguage('pl')` przed renderem; asercje na unikalne fragmenty (np. nazwy portfeli) | Refaktor wszystkich testów na klucze i18n (overkill) |
| `MarketPage` / `AdvisorPage` nie są w scope | Tłumaczymy tylko nagłówki ekranów (sygnalizujemy TODO w komentarzu) | Pełne tłumaczenie (rozszerzenie scope) |

## 6. Ryzyka

1. **Type safety i18n** — `resolveJsonModule: true` **NIE jest** w `tsconfig.app.json` — trzeba dodać przed implementacją. Bez tego TS nie inferuje typów kluczy z JSON-ów. Vite obsłuży JSON runtime bez tego, ale type-safety kluczy i18n wymaga tej flagi.
2. **MSW + tests** — `i18n.changeLanguage` jest async; testy potrzebują `await` w setupie. Mitigacja: synchronous `i18n.use(initReactI18next).init({ ..., react: { useSuspense: false } })`.
3. **Vite HMR a static JSON** — JSON-y muszą być importowane statycznie. Mitigacja: import w `config.ts`, bez `lazy`.
4. **Bundle size** — i18next dodaje ~30KB gz. Akceptowalne dla SPA.
5. **Regresja istniejących testów Dashboard** — Dashboard używa polskich stringów hardcoded. Po migracji jego testy też muszą zaaplikować changeLanguage. Zakres: 1–2 testy. Mitigacja: jednorazowy update.

## 7. Verification (Definition of Done)

```bash
# Build
cd frontend && npm run build

# Lint
npm run lint

# Format check
npm run format:check

# Testy
npm run test -- --run

# Manualna weryfikacja
npm run dev
# → http://localhost:5173
# → przełącznik PL/EN w prawym górnym rogu działa
# → /portfolio i /portfolio/:id wyglądają jak Dashboard (rounded-2xl, bg-card/60, skeletony)
# → loading/empty/error states działają (można zasymulować odłączeniem backend)
```

## 8. Rollback plan

Pojedynczy commit `feat: portfolio screen redesign + i18n PL/EN` na branchu feature → revert PR przy problemach. Brak migracji DB, zmiany tylko frontendowe.

## 9. Out of scope (świadomie)

- Pełne tłumaczenie `MarketPage` / `AdvisorPage` / `AdvisorRecommendations` (tylko nagłówki).
- Tłumaczenie błędów backendu (zostają w EN z API).
- E2E (`playwright`) — istniejący `e2e/portfolio.spec.ts` musi być sprawdzony, ale rozszerzanie e2e poza scope.
- Lazy-loading translation files (nie potrzeba dla 2 języków).
- RTL languages (nie dotyczy PL/EN).

---

## Akceptacja

- [ ] **Proceed** — implementuj zgodnie z planem
- [ ] **Hold** — wstrzymaj
- [ ] **Modify** — uwagi do planu: _____
