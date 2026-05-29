# ALF-85 — Automatyczne pobieranie danych: Scheduler + Fallback + SSE + Backfill

**Data**: 2026-05-28  
**Branch**: `feature/ALF-85/auto-market-data-scheduler-sse-backfill`

---

## Decyzje architektoniczne (Q&A)

| Kwestia | Decyzja |
|---------|---------|
| Fallback provider | Stub/Mock — loguje `"fallback triggered"`, nie pobiera danych. Wartość: circuit breaker pattern + metryki per provider. |
| SSE + auth | `permitAll()` — ceny rynkowe nie są danymi prywatnymi. Zero komplikacji z JWT/EventSource. |
| Backfill źródło | Admin endpoint `POST /api/prices/refresh/{symbol}` — wyzwala natychmiastowy fetch dla symbolu. Działa na Finnhub free tier. |
| Kolejność | Faza 1 → Faza 2 → Faza 3 (wszystkie) |

---

## Stan wyjściowy (ALF-66 done)

| Element | Status |
|---------|--------|
| `MarketDataRefreshScheduler` — 2 crony (5 min trading, 10 min always-on) | ✅ |
| `FinnhubClient` + `MarketDataProvider` interface + `@Retry` | ✅ |
| Redis cache, PriceCacheService | ✅ |
| Indeks `(symbol, fetched_at DESC)` | ✅ |
| `SymbolType` enum (STOCK / CRYPTO / FOREX) w encji | ✅ |

---

## Faza 1 — TASK-01.1 fix + TASK-01.2 Fallback + Circuit Breaker

### Co się zmienia

**Gap TASK-01.1**: Scheduler wywołuje `findByActiveTrue()` — nie rozróżnia `STOCK` vs `CRYPTO`. Krypto powinno być odświeżane co 1 min, nie co 10 min. Scheduler musi filtrować po `SymbolType`.

**TASK-01.2**: Dodajemy circuit breaker wokół Finnhub. Gdy Finnhub jest niedostępny (circuit open), fallback loguje ostrzeżenie + metryki i nie próbuje alternatywnego źródła (stub pattern).

### Pliki

1. **`SupportedSymbolRepository.java`**  
   Dodaj: `List<SupportedSymbol> findByActiveTrueAndTypeIn(Collection<SymbolType> types);`

2. **`MarketDataRefreshScheduler.java`**  
   Zamień `refreshTradingHours()` → `refreshStocks()` (filtr: STOCK + FOREX, cron 5 min MON-FRI 9-23 UTC)  
   Zamień `refreshAlwaysOn()` → `refreshCrypto()` (filtr: CRYPTO, cron 1 min 24/7)  
   Parametr `refreshAllActiveSymbols()` → `refreshByTypes(Set<SymbolType> types)`  
   Dodaj obsługę `MarketDataUnavailableException` → log WARN i skip (nie mylić z "symbol not found")

3. **`MarketDataSchedulerProperties.java`**  
   Dodaj `cronCrypto` property (default: `"0 */1 * * * *"`)

4. **`FallbackMarketDataProvider.java`** *(nowy)*  
   Implementuje `MarketDataProvider`. Wraps `FinnhubClient`.  
   `@CircuitBreaker(name = "finnhub-circuit", fallbackMethod = "fallbackQuote")`  
   `fallbackQuote(String symbol, Throwable t)` → log WARN + metryki + throw `MarketDataUnavailableException`  
   `@Primary` bean — scheduler automatycznie go dostaje
 
 5. **`MarketDataUnavailableException.java`** *(nowy)*  
    Wyjątek domenowy na brak dostępu do providera (circuit open / outage)

6. **`MarketDataConfig.java`**  
   Zarejestruj `FallbackMarketDataProvider` jako `@Primary @Bean`  
   Dodaj `AlphaVantageProperties` → NIE (stub, nie potrzeba)

7. **`application.yml`**  
   Dodaj Resilience4j circuit breaker config:
   ```yaml
   resilience4j:
     circuitbreaker:
       instances:
         finnhub-circuit:
           slidingWindowSize: 5
           failureRateThreshold: 60
           waitDurationInOpenState: 30s
           permittedNumberOfCallsInHalfOpenState: 2
   ```
   Dodaj `market-data.scheduler.cron-crypto`

8. **`FallbackMarketDataProviderTest.java`** *(nowy)*  
   Unit test: primary OK → dane, primary throws → fallback triggered, circuit open → fallback triggered

**Szacunek: ~7 plików, ~150 linii**

---

## Faza 2 — TASK-01.3 SSE Real-Time Prices

### Co się zmienia

Backend dodaje endpoint SSE. Frontend przestaje pollować REST co 15 s i subskrybuje stream.

### Pliki

1. **`PriceSseBroadcaster.java`** *(nowy)*  
   `CopyOnWriteArrayList<SseEmitter>` — rejestr subskrybentów.  
   `addEmitter(SseEmitter)` — rejestracja + completion/timeout/error callbacks do usunięcia.  
   `broadcast(List<MarketPriceResponse>)` — wysyła domyślny event `message` do wszystkich emiterów.  
   Heartbeat/ping co ~25s, aby utrzymać połączenie (np. komentarz SSE).
   `@Component`

2. **`PriceSseController.java`** *(nowy)*  
   `GET /api/prices/stream` → `SseEmitter` (timeout ≥ 5 min lub `0` + heartbeat).  
   Wysyła initial event z aktualnymi cenami zaraz po połączeniu.  
   Rejestruje się w `PriceSseBroadcaster`.

3. **`SecurityConfig.java`**  
   Dodaj `/api/prices/stream` do `permitAll()`.

4. **`MarketDataRefreshScheduler.java`**  
   Po `refreshByTypes(...)` → `broadcaster.broadcast(latestPrices)`.  
   Inject `PriceSseBroadcaster` + `MarketPriceService`.

5. **Frontend `useEventSource.ts`** *(nowy hook)*  
   ```ts
   export function useEventSource<T>(url: string): T[] | undefined
   ```  
   `new EventSource(url)` w `useEffect`, `onmessage` → `setState`, cleanup `close()`.

6. **Frontend `usePriceTicker.ts`**  
   Zamień `useQuery` z `refetchInterval: 15_000` → `useEventSource('/api/prices/stream')`.  
   Fallback do REST jeśli SSE niedostępny (opcjonalne).

**Szacunek: ~6 plików, ~130 linii**

---

## Faza 3 — TASK-01.4 Backfill (Admin Trigger)

### Co się zmienia

Dodajemy endpoint do ręcznego wyzwolenia odświeżenia — zastępuje koncepcję "backfill" w kontekście free-tier API. EOD scheduler codziennie o 23:00 UTC.

### Pliki

1. **`MarketPriceController.java`**  
   `POST /api/prices/refresh/{symbol}` — `hasRole('ADMIN')`, wyzwala `fetchAndPersist(symbol)`.  
   `POST /api/prices/refresh/all` — wyzwala `refreshByTypes(Set.of(STOCK))` + `refreshByTypes(Set.of(CRYPTO))`.

2. **`MarketDataRefreshScheduler.java`**  
   Dodaj publiczną metodę `triggerImmediateFetch(String symbol)` — używana przez controller.  
   Dodaj EOD cron: `@Scheduled(cron = "0 0 23 * * MON-FRI", zone = "UTC")` → `refreshByTypes(Set.of(STOCK))`.

3. **Docs: `docs/api/market-data-refresh.md`** *(nowy)*  
   Opis endpointów refresh + SSE stream.

**Szacunek: ~3 pliki, ~60 linii**

---

## Łączny zakres

| Faza | Pliki | Linie | Status |
|------|-------|-------|--------|
| Faza 1 | 7 | ~150 | do zrobienia |
| Faza 2 | 6 | ~130 | do zrobienia |
| Faza 3 | 3 | ~60 | do zrobienia |
| **Razem** | **~16** | **~340** | |

---

## Definition of Done

- [ ] Build bez błędów
- [ ] Testy Fazy 1 zielone (FallbackMarketDataProviderTest)
- [ ] SSE endpoint odpowiada `text/event-stream`
- [ ] Frontend MarketTicker aktualizuje się przez SSE (bez pollingu)
- [ ] `/api/prices/refresh/{symbol}` działa i zapisuje snapshot
- [ ] Docs zaktualizowane
