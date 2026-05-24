# 🎬 Fin-Insight Demo Scenario (5-10 minut)

## Przygotowanie (2 minuty)

### 1. **Uruchom Docker Compose**
```bash
cd backend
docker-compose up -d
# Czekaj 2-3 minuty aż wszystkie serwisy się uruchomią
docker-compose ps  # Sprawdź status
```

### 2. **Uruchom Frontend**
```bash
cd frontend
npm run dev
# Frontend dostępny na: http://localhost:5173
```

### 3. **Przygotuj przeglądarki/tabele**
- Tab 1: Frontend (http://localhost:5173)
- Tab 2: Keycloak Admin (http://localhost:8080/admin) - opcjonalnie
- Tab 3: Eureka Dashboard (http://localhost:8761)

---

## 🎯 DEMO SCENARIUSZ (5-10 minut)

### **CZĘŚĆ 1: Logowanie przez Keycloak (1-2 min)**

#### Krok 1.1: Otwórz Frontend
```
URL: http://localhost:5173
```
**Ekran:** Login page

#### Krok 1.2: Kliknij "Login with Keycloak"
```
Powinna otworzyć okno logowania Keycloak
```

#### Krok 1.3: Zaloguj się
```
Username: testuser
Password: test123
```

**Narracja:**
> "Logowanie odbywa się przez Keycloak - bezpieczny OAuth2 serwer tożsamości. 
> Aplikacja jest chroniona tokenami JWT, które są wydawane przez Keycloak.
> Wszystkie żądania do API muszą mieć poprawny token w nagłówku Authorization."

**Po zalogowaniu:** Przekierowanie na Dashboard

---

### **CZĘŚĆ 2: Dashboard z Metrykami Portfela (2-3 min)**

#### Krok 2.1: Pokaż Overview
```
Główny ekran wyświetla:
- Total Portfolio Value: ~$150,000
- Total Invested: ~$45,000
- Current P&L: +$50,000 (trend up)
- Allocation Chart (Stocks/Crypto)
```

**Narracja:**
> "Dashboard pokazuje całkowitą wartość portfela użytkownika.
> Widzisz 3 predefiniowane portfele z danymi demo:
> 1. Growth Portfolio - akcje (AAPL, GOOGL, MSFT)
> 2. Crypto Holdings - kryptowaluty (BTC, ETH)
> 3. Diversified Mixed - mieszany portfel"

#### Krok 2.2: Pokaż Portfele (List View)
```
Tablica pokazuje:
┌─────────────────────────────────────────┐
│ Portfolio Name  │ Value    │ Assets │ P&L │
├─────────────────────────────────────────┤
│ Growth Port.    │ $89,500  │   3    │ +12%│
│ Crypto Hold.    │ $38,200  │   2    │ +25%│
│ Diversified     │ $22,300  │   3    │ +8% │
└─────────────────────────────────────────┘
```

**Kliknij na jeden portfel** → Pokaż szczegóły aktywów

```
GROWTH PORTFOLIO Details:
├─ AAPL: 50 units @ $175.45 = $8,772.50 (+16.6%)
├─ GOOGL: 30 units @ $138.20 = $4,146.00 (+10.3%)
└─ MSFT: 25 units @ $424.50 = $10,612.50 (+11.5%)
```

**Narracja:**
> "Każdy portfel ma swoje aktywa. 
> Transakcje historyczne są przechowywane w bazie danych (SQL),
> a ceny rynkowe są aktualizowane co 15 minut z API."

---

### **CZĘŚĆ 3: Przeglądanie Danych Rynkowych (1-2 min)**

#### Krok 3.1: Nawiguj do "Market Data"
```
URL: /market-data lub kliknij menu "Market"
```

**Ekran:** Tablica symboli i cen
```
┌────────────┬────────┬───────────┬────────────┐
│ Symbol     │ Price  │ Change24h │ Volume     │
├────────────┼────────┼───────────┼────────────┤
│ AAPL       │ $175.45│ +1.25%    │ 52.0M      │
│ GOOGL      │ $138.20│ -0.85%    │ 28.5M      │
│ MSFT       │ $424.50│ +2.10%    │ 22.0M      │
│ BTC-USD    │ $62,543│ +3.75%    │ 31.5B      │
│ ETH-USD    │ $3,428 │ +2.50%    │ 14.8B      │
└────────────┴────────┴───────────┴────────────┘
```

#### Krok 3.2: Kliknij na jeden symbol (np. AAPL)
```
Wyświetl:
- Bieżąca cena: $175.45
- 6-miesięczna historia (wykres)
- Statystyka: Min/Max/Avg
- Wolumen handlu
```

**Narracja:**
> "Dane rynkowe pochodzą z serwisu Market Data Service.
> Wszystkie dane historyczne (6 miesięcy wstecz) są zapisane w bazie.
> Service komunikuje się z Eureka Discovery Service,
> żeby znaleźć instancje portfolio-managera."

---

### **CZĘŚĆ 4: Tworzenie Portfela + Dodawanie Aktywów (2-3 min)**

#### Krok 4.1: Kliknij "Create New Portfolio"
```
Formularz:
┌─────────────────────────────┐
│ Portfolio Name              │
│ [New Investment Strategy]   │
├─────────────────────────────┤
│ Description                 │
│ [Conservative approach...]  │
├─────────────────────────────┤
│ [CREATE] button             │
└─────────────────────────────┘
```

#### Krok 4.2: Wypełnij dane
```
Name: "Test Portfolio"
Description: "Portfolio demo dla prezentacji"
Kliknij: CREATE
```

**Narracja:**
> "Nowy portfel jest tworzony w portfolio-manager service.
> Dane są zapisywane w PostgreSQL z Flyway migrations."

#### Krok 4.3: Dodaj Asset do Portfela
```
Portfolio: Test Portfolio
Kliknij: [+ Add Asset]

Formularz:
┌──────────────────────────────┐
│ Select Symbol                │
│ [AAPL ▼]                     │
├──────────────────────────────┤
│ Quantity                     │
│ [10.00]                      │
├──────────────────────────────┤
│ Average Buy Price            │
│ [$175.45]                    │
├──────────────────────────────┤
│ [ADD ASSET] button           │
└──────────────────────────────┘
```

#### Krok 4.4: Dodaj Transakcję (BUY)
```
Asset: AAPL
Quantity: 10
Price: $175.45
Fee: $5.00
Date: Today
Type: BUY

[CONFIRM TRANSACTION]
```

**Narracja:**
> "Transakcja jest rejestrowana w historii.
> System oblicza average buy price na podstawie wszystkich transakcji.
> P&L (profit/loss) jest automatycznie liczony na podstawie ceny rynkowej."

---

### **CZĘŚĆ 5: AI Rekomendacje (1-2 min)**

#### Krok 5.1: Nawiguj do "AI Advisor"
```
URL: /advisor lub kliknij menu "AI Advisor"
```

**Ekran:** AI Recommendations
```
┌──────────────────────────────────────────┐
│ 🤖 AI Portfolio Advisor                  │
├──────────────────────────────────────────┤
│                                          │
│ ✅ RECOMMENDATION 1:                     │
│ Symbol: BTC-USD                          │
│ Action: INCREASE allocation              │
│ Confidence: 78%                          │
│ Reason: Strong uptrend, low correlation  │
│                                          │
│ ⚠️ RECOMMENDATION 2:                     │
│ Symbol: MSFT                             │
│ Action: HOLD                             │
│ Confidence: 65%                          │
│ Reason: Neutral signal, diversify instead│
│                                          │
│ 🔔 RECOMMENDATION 3:                     │
│ Symbol: TSLA                             │
│ Action: REDUCE exposure                  │
│ Confidence: 72%                          │
│ Reason: High volatility detected         │
│                                          │
└──────────────────────────────────────────┘
```

#### Krok 5.2: Pokaż Details Rekomendacji
```
Kliknij na jedną rekomendację:
- AI Model Used: LLM (Claude/GPT)
- Market Analysis: Technical + Fundamental
- Historical Data: 6 months analyzed
- Risk Level: Medium
- Suggested Action: Increase allocation by 5%
```

**Narracja:**
> "AI Advisor service wykorzystuje LLM (duże modele językowe)
> do analizy danych rynkowych i portfela.
> System patrzy na 6-miesięczną historię cen,
> korelacje między aktywami, i tren.
> Rekomendacje są generowane dynamicznie w oparciu o bieżące dane."

---

### **CZĘŚĆ 6: Architektura (1-2 min)**

#### Krok 6.1: Pokaż Eureka Dashboard
```
URL: http://localhost:8761
```

**Ekran:** Service Registry
```
┌────────────────────────────────────────────┐
│ EUREKA SERVER - Service Registry            │
├────────────────────────────────────────────┤
│                                            │
│ ✅ PORTFOLIO-MANAGER (1 instance)          │
│    Host: portfolio-manager:8081            │
│    Status: UP                              │
│    Uptime: 5 minutes                       │
│                                            │
│ ✅ MARKET-DATA-SERVICE (1 instance)        │
│    Host: market-data-service:8082          │
│    Status: UP                              │
│    Uptime: 5 minutes                       │
│                                            │
│ ✅ AI-ADVISOR-SERVICE (1 instance)         │
│    Host: ai-advisor-service:8083           │
│    Status: UP                              │
│    Uptime: 5 minutes                       │
│                                            │
│ ✅ GATEWAY-SERVICE (1 instance)            │
│    Host: gateway-service:8084              │
│    Status: UP (Unhealthy - Redis)          │
│    Uptime: 5 minutes                       │
│                                            │
│ ✅ CONFIG-SERVER (1 instance)              │
│    Host: config-server:8888                │
│    Status: UP                              │
│                                            │
│ ✅ EUREKA-SERVER (1 instance)              │
│    Host: eureka-server:8761                │
│    Status: UP                              │
│                                            │
└────────────────────────────────────────────┘
```

**Narracja:**
> "Eureka to service discovery - rejestr wszystkich mikroserwisów.
> Gateway znает, gdzie znaleźć portfolio-managera i market-data-service.
> Jeśli dodamy nową instancję, Eureka automatycznie ją zarejestruje.
> To umożliwia skalowanie horyztalne bez zmian w konfiguracji."

#### Krok 6.2: Pokaż Docker Architecture
```bash
docker-compose ps
```

**Ekran:** Running Containers
```
fin-insight-keycloak          ✅ UP (8080)
fin-insight-keycloak-db       ✅ UP (5432 internal)
fin-insight-portfolio         ✅ UP (8081)
fin-insight-portfolio-db      ✅ UP (5433 external)
fin-insight-market-data       ✅ UP (8082)
fin-insight-market-data-db    ✅ UP (5434 external)
fin-insight-ai-advisor        ✅ UP (8083)
fin-insight-advisor-db        ✅ UP (5435 external)
fin-insight-gateway           ✅ UP (8084)
fin-insight-config            ✅ UP (8888)
fin-insight-eureka            ✅ UP (8761)
fin-insight-redis             ✅ UP (6379)
```

**Narracja:**
> "Docker Compose orchestruje 12 kontenerów:
> - 3 PostgreSQL bazy (portfolio, market-data, advisor)
> - 5 mikroserwisów (portfolio, market-data, advisor, gateway, config)
> - 2 infrastrukturalne (Eureka, Keycloak)
> - Redis dla cache'a (opcjonalnie)
>
> Każdy serwis ma swoją bazę danych (Database per Service pattern).
> Komunikacja między serwisami przez REST API i service discovery."

#### Krok 6.3: Pokaż Architekturę na Diagramie
```
┌─────────────────────────────────────────────────────┐
│                    FRONTEND (React)                 │
│                   http://localhost:5173             │
└────────────────────┬────────────────────────────────┘
                     │ HTTP/REST
┌────────────────────▼────────────────────────────────┐
│          API GATEWAY (Spring Cloud)                 │
│         http://localhost:8084                       │
│         (Request routing, load balancing)           │
└────────────────────┬────────────────────────────────┘
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼──┐   ┌────▼──┐   ┌───▼────┐
    │ PM    │   │ MD    │   │ AA     │
    │ 8081  │   │ 8082  │   │ 8083   │
    └────┬──┘   └────┬──┘   └───┬────┘
         │           │           │
    ┌────▼──┐   ┌────▼──┐   ┌───▼────┐
    │ PM-DB │   │ MD-DB │   │ AA-DB  │
    │ 5433  │   │ 5434  │   │ 5435   │
    └───────┘   └───────┘   └────────┘

     ┌─────────────────────────┐
     │  EUREKA REGISTRY (8761) │
     │  (Service Discovery)    │
     └─────────────────────────┘

     ┌─────────────────────────┐
     │  CONFIG SERVER (8888)   │
     │  (Centralized config)   │
     └─────────────────────────┘

     ┌─────────────────────────┐
     │  KEYCLOAK (8080)        │
     │  (OAuth2/OpenID Connect)│
     └─────────────────────────┘

Legend:
PM = Portfolio Manager Service
MD = Market Data Service  
AA = AI Advisor Service
```

---

## 📋 Demo Checklist

- [ ] 1. Uruchom Docker Compose
- [ ] 2. Frontend dostępny na :5173
- [ ] 3. Zaloguj się (testuser/test123)
- [ ] 4. Pokaż Dashboard (3 portfele, metryki)
- [ ] 5. Pokaż Market Data (ceny, historia 6 miesięcy)
- [ ] 6. Stwórz nowy portfel
- [ ] 7. Dodaj asset i transakcję
- [ ] 8. Pokaż AI Rekomendacje
- [ ] 9. Pokaż Eureka Dashboard (service discovery)
- [ ] 10. Pokaż Docker Architecture

---

## 🎤 Talking Points

### O Technologii
- **Spring Cloud**: Microservices framework (Eureka, Config Server, Gateway)
- **PostgreSQL**: Database per Service (każdy serwis ma swoją BD)
- **Flyway**: Version control dla baz danych
- **OAuth2/OpenID Connect**: Bezpieczna autentykacja przez Keycloak
- **Docker Compose**: Orchestration dla lokalnego developmentu
- **React + TypeScript**: Frontend z type safety
- **REST API**: Standard komunikacji między serwisami

### O Demo Danych
- **3 Predefiniowane Portfele** z realnymi danymi demo
- **7 Symboli Rynkowych** (5 akcji + 2 kryptowaluty, NO FOREX)
- **6 Miesięcy Historii Cen** dla każdego symbolu
- **Demo User**: testuser/test123 (z Keycloaka)

### O Skalowaniu
- Każdy serwis może być skalowany niezależnie
- Eureka automatycznie odkrywa nowe instancje
- Load balancer (Gateway) dystrybuuje żądania
- Database per Service pozwala na optymalne schematy

---

## 💡 Możliwe Rozszerzenia Demo

1. **Dodaj nową instancję Portfolio Manager**
   ```bash
   docker run -e EUREKA_URL=http://eureka-server:8761 ...
   ```
   Eureka automatycznie ją zarejestruje

2. **Pokaż Error Handling**
   - Stop jednej bazy danych
   - Pokaz graceful degradation

3. **Pokaż Monitoring**
   - Health check endpoints
   - Metrics na Eureka

4. **Pokaż Config Server**
   - Zmieniaj properties bez restartu
   - Broadcasting zmian do serwisów

---

## ⏱️ Timeline

```
0:00 - 0:30  → Przygotowanie + logowanie (Keycloak)
0:30 - 1:30  → Dashboard z metrykami (3 portfele)
1:30 - 2:30  → Market Data (ceny, historia)
2:30 - 4:00  → Create Portfolio + Add Assets
4:00 - 5:00  → AI Recommendations
5:00 - 6:00  → Architektura (Eureka + Docker)
6:00 - 7:00  → Q&A
```

---

## 📞 Kontakt do Demo Usera

```
Email: testuser@fininsight.local
Username: testuser
Password: test123
```

Demo user ma predefiniowane 3 portfele z 10 aktywami i 48 snapshot'ami cen.

---

**Autor**: Demo Preparation  
**Data**: 2026-05-24  
**Czas trwania**: 5-10 minut  
**Wymagane**: Docker, Node.js, Internet connection (opcjonalnie)
