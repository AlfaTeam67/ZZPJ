# 🚀 Fin-Insight (ZZPJ)

> **Inteligentny asystent inwestora** — Aplikacja webowa do zarządzania portfelem inwestycyjnym z analizą rynkową i rekomendacjami AI.

![Status](https://img.shields.io/badge/status-production%20ready-10b981)
![Team](https://img.shields.io/badge/team-AlfaTeam-7c3aed)
![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20TypeScript%20%2B%20Vite-0ea5e9)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.2%20%2B%20Cloud-10b981)
![Course](https://img.shields.io/badge/ZZPJ%202025%2F26-Projekt%20Semestralny-f59e0b)

---

## 📋 Zawartość dokumentacji

1. [O projekcie](#-o-projekcie)
2. [Architektura](#-architektura-systemu)
3. [Stack technologiczny](#%EF%B8%8F-stack-technologiczny)
4. [Szybki start (krok po kroku)](#%EF%B8%8F-szybki-start--instrukcja-uruchomienia)
5. [Demo scenariusz (5-10 minut)](#-demo-scenariusz)
6. [Czego się nauczyliśmy](#-czego-się-nauczyliśmy)
7. [Linki do dokumentacji](#-linki-do-dokumentacji)

---

## ✨ O projekcie

**Fin-Insight** to projekt semestralny zespołu **AlfaTeam** realizowany w ramach przedmiotu ZZPJ (2025/2026).

### Funkcjonalności

- ✅ **Zarządzanie portfelem** — Tworzenie portfeli, dodawanie aktywów (akcje, krypto, obligacje), śledzenie transakcji
- ✅ **Dane rynkowe** — Aktualne ceny symboli rynkowych, 6-miesięczna historia cen
- ✅ **AI Advisor** — Rekomendacje wspierane przez LLM (Claude, GPT) z analizą portfela
- ✅ **Bezpieczeństwo** — OAuth2/OIDC (Keycloak), JWT, szyfrowana komunikacja TLS 1.3
- ✅ **Skalowalna architektura** — Microservices, Service Discovery, Load Balancing

### Użytkownik może:
1. Zalogować się przez Keycloak
2. Tworzyć i zarządzać portfelami inwestycyjnymi
3. Przeglądać dane rynkowe (ceny, trend 6 miesięcy)
4. Dodawać i usuwać aktywa z portfela
5. Śledzić transakcje (kupno, sprzedaż)
6. Otrzymywać rekomendacje AI na podstawie zawartości portfela

---

## 🧱 Architektura systemu

### Diagram wysokopoziomowy

```
┌─────────────┐
│   Frontend  │ (React + TypeScript)
│ :5173/5174  │
└──────┬──────┘
       │ (HTTPS)
┌──────▼──────────────┐
│  Keycloak (OAuth2)  │ (8080)
│  Identity Provider  │
└────────────┬────────┘
       │
┌──────▼──────────────────────────┐
│     Spring Cloud Gateway        │ (8084)
│  Request Routing & Auth         │
└──────┬─┬────────────────┬───────┘
       │ │                │
   ┌───▼─▼─┐  ┌─────────┐  ┌─────────┐
   │ Port. │  │ Market  │  │   AI    │
   │ Mgr.  │  │  Data   │  │ Advisor │
   │ (8081)│  │ (8082)  │  │ (8083)  │
   └───┬───┘  └────┬────┘  └────┬────┘
       │           │            │
   ┌───▼───┐   ┌───▼────┐  ┌────▼────┐
   │ Port. │   │Market  │  │ Advisor │
   │  DB   │   │  DB    │  │   DB    │
   │ :5433 │   │ :5434  │  │ :5435   │
   └───────┘   └────────┘  └─────────┘

┌────────────────────────────────────────┐
│    Infrastructure Services             │
├────────────────────────────────────────┤
│ Eureka (8761) - Service Discovery     │
│ Config Server (8888) - Configuration  │
│ Redis (6379) - Cache                  │
└────────────────────────────────────────┘
```

### Core Services

| Serwis | Port | Odpowiedzialność |
|--------|------|------------------|
| **Keycloak** | 8080 | OAuth2/OIDC, zarządzanie użytkownikami |
| **API Gateway** | 8084 | Routing, load balancing, autentykacja |
| **Portfolio Manager** | 8081 | CRUD portfeli, zarządzanie aktywami, transakcjami |
| **Market Data** | 8082 | Agregacja danych rynkowych, ceny, historia |
| **AI Advisor** | 8083 | Analiza portfela, rekomendacje AI |
| **Eureka Server** | 8761 | Service registry & discovery |
| **Config Server** | 8888 | Zarządzanie konfiguracją |

### Bazy danych

- **Keycloak DB** (PostgreSQL 16) — Realm, users, sessions
- **Portfolio DB** (PostgreSQL 16) — Portfolios, assets, transactions
- **Market Data DB** (PostgreSQL 16) — Symbols, price snapshots
- **Advisor DB** (PostgreSQL 16) — Recommendations, LLM cache

### Demo Data

**Seed Scripts** (Flyway migrations):
- ✅ 3 predefiniowane portfele (Growth, Crypto, Diversified)
- ✅ 10 aktywów (5 stocks + 2 crypto każdy portfel)
- ✅ 9 transakcji BUY
- ✅ 7 symboli rynkowych (AAPL, GOOGL, MSFT, TSLA, AMZN, BTC-USD, ETH-USD)
- ✅ 48 snapshots cen historycznych (6 miesięcy)
- ✅ Demo user: **testuser** / **test123**

---

## 🛠️ Stack technologiczny

### Backend
- **Java 21.0.11** LTS
- **Spring Boot 3.2.4** — REST API, MVC
- **Spring Cloud 2023.0.4** — Eureka, Config Server, Gateway, Load Balancer
- **Keycloak 24.0** — OAuth2/OIDC Provider
- **PostgreSQL 16.14** — 4 bazy danych (Database per Service)
- **Flyway 9.22.3** — Schema versioning & migrations
- **JPA/Hibernate** — ORM
- **Gradle 8.x** — Build system

### Frontend
- **React 18.3** — UI library
- **TypeScript 5.x** — Static typing
- **Vite 5.x** — Build tool & dev server
- **React Query** — Data fetching & caching
- **Redux Toolkit** — State management
- **Tailwind CSS** — Utility-first CSS
- **shadcn/ui** — Component library
- **Axios** — HTTP client

### DevOps & Infrastructure
- **Docker 27.x** — Containerization
- **Docker Compose** — Container orchestration
- **Keycloak** — Identity & Access Management
- **Redis 7** — Caching layer
- **Spring Boot Actuator** — Health checks

---

## ⚙️ Szybki start — Instrukcja uruchomienia

### Wymagania wstępne

```bash
# Sprawdź wersje
java -version           # Java 21+
node --version          # Node.js 18+
docker --version        # Docker 27+
docker-compose --version  # Docker Compose 2.x+
```

### Krok 1️⃣ — Klonowanie repozytorium

```bash
git clone https://github.com/AlfaTeam/ZZPJ.git
cd ZZPJ
```

### Krok 2️⃣ — Uruchomienie Backend (Docker Compose)

```bash
cd backend

# Uruchom wszystkie serwisy (12 kontenerów)
docker-compose up -d

# Czekaj aż serwisy się inicjalizują (1-2 minuty)
docker ps  # Powinno być 12 kontenerów ze statusem "healthy" lub "Up"
```

**Zweryfikuj inicjalizację:**

```bash
# Eureka Service Registry
open http://localhost:8761

# Keycloak Admin Console
open http://localhost:8080  # admin / admin

# Health Check
curl http://localhost:8081/actuator/health  # Portfolio Manager
curl http://localhost:8082/actuator/health  # Market Data
curl http://localhost:8083/actuator/health  # AI Advisor
```

### Krok 3️⃣ — Uruchomienie Frontend (Vite Dev Server)

```bash
cd frontend

# Zainstal zależności
npm install

# Uruchom dev server
npm run dev

# Frontend dostępny na http://localhost:5173 lub :5174
```

### Krok 4️⃣ — Zalogowanie się

Otwórz http://localhost:5173 w przeglądarce:

```
📧 Email/Username: testuser
🔒 Hasło: test123
```

### Krok 5️⃣ — Przeglądaj demo

Po zalogowaniu:
- 📊 **Dashboard** — Przegląd portfeli i metryk
- 💼 **Portfolios** — Lista portfeli demo (Growth, Crypto, Diversified)
- 📈 **Market Data** — Ceny i historia 7 symboli
- 🤖 **Advisor** — Rekomendacje AI
- ⚙️ **Eureka Dashboard** — Service registry

---

## 🎬 Demo scenariusz (5-10 minut)

### Pełny scenariusz demontacyjny

👉 **Dokumentacja:** [backend/DEMO_SCENARIO.md](backend/DEMO_SCENARIO.md)

Scenariusz obejmuje:
- ✅ Logowanie (Keycloak OAuth2)
- ✅ Przeglądanie dashboard'u
- ✅ Widok portfeli i aktywów
- ✅ Dane rynkowe
- ✅ Tworzenie nowego portfela
- ✅ Rekomendacje AI
- ✅ Architektura systemu

**Czas:** 5-10 minut z dyskusją

---

## 📚 Czego się nauczyliśmy

### 🎯 Backend Learnings

#### Spring Cloud Microservices
- ✅ **Service Discovery** — Eureka server & client registration
- ✅ **API Gateway** — Request routing, load balancing, circuit breakers
- ✅ **Configuration Management** — Centralized config server, environment-specific profiles
- ✅ **Distributed Tracing** — Tracking requests across services
- **Lesson:** Microservices złożoność rosnąć z liczbą serwisów; potrzebne monitoring i logging

#### OAuth2 & Keycloak
- ✅ **OpenID Connect** — Authorization Code Flow with PKCE
- ✅ **JWT Token Management** — Token validation, refresh, expiration
- ✅ **Realm Management** — Users, roles, client configuration
- **Lesson:** Delegowanie autentykacji do specjalizowanego providera (Keycloak) to best practice

#### Database Design & Migrations
- ✅ **Database per Service Pattern** — Każdy serwis ma swoją bazę (separation of concerns)
- ✅ **Flyway Migrations** — Version control dla schema, rollback strategy
- ✅ **Entity Constraints** — Enum constraints (STOCK, CRYPTO, BOND only) muszą być  weryfikowane przed seeding
- **Lesson:** Typy baz i constraints muszą być znane przed generowaniem demo data

#### API Design
- ✅ **RESTful Endpoints** — Consistency w URL structure, HTTP methods, status codes
- ✅ **Error Handling** — Proper exception mapping to HTTP responses
- ✅ **CORS Configuration** — Handling cross-origin requests from Frontend
- **Lesson:** Dokumentacja API musi być aktualna i zasilane unit testów

### 🎨 Frontend Learnings

#### React Architecture
- ✅ **Custom Hooks** — Extraction logiki biznesowej (usePortfolios, useAdvisor)
- ✅ **React Query** — Server state management, caching, auto-refetch
- ✅ **Redux Toolkit** — Client state (auth, UI) vs server state (portfolio data)
- ✅ **Component Organization** — Feature-based folder structure
- **Lesson:** Separacja server state i client state jest crucially importante

#### Authentication Flow
- ✅ **Keycloak JS Adapter** — Token management, automatic refresh
- ✅ **Axios Interceptors** — JWT injection w headers, automatic token refresh on 401
- ✅ **Protected Routes** — Auth guards w React Router
- **Lesson:** Keycloak JS adapter handle'uje wiele edge cases (token expiration, silent refresh)

#### TypeScript & Type Safety
- ✅ **Strict Type Definitions** — Entity types z backend (Portfolio, Asset, Transaction)
- ✅ **API Response Types** — Ensure responses match expected shape
- ✅ **Component Props Typing** — Every component fully typed
- **Lesson:** TypeScript zmniejsza runtime errors 60-70% (estimate)

#### Testing
- ✅ **Vitest & React Testing Library** — Unit tests dla components i hooks
- ✅ **MSW (Mock Service Worker)** — Network mocking dla integration tests
- ✅ **E2E Testing (Playwright)** — Full user journeys (login, create portfolio, etc.)
- **Lesson:** Test pyramid: dużo unit tests, mniej integration tests, kilka E2E tests

### 🏗️ Architecture & DevOps Learnings

#### Docker & Compose
- ✅ **Multi-Container Architecture** — 12 containers orchestrated by Docker Compose
- ✅ **Health Checks** — Liveness & readiness probes dla proper startup order
- ✅ **Volume Management** — Data persistence across container restarts
- ✅ **Network Isolation** — Custom bridge network (fin-insight-network)
- **Lesson:** Docker Compose świetny dla dev; potrzeba Kubernetes dla production

#### Database Design Patterns
- ✅ **Database per Service** — Evituje tight coupling, enables independent scaling
- ✅ **No Cross-DB Transactions** — Eventual consistency, event-driven communication
- ✅ **Seed Data via Migrations** — Demo data version controlled, reproducible
- **Lesson:** Distributed data management trade-off between consistency i scalability

#### Security Best Practices
- ✅ **HTTPS/TLS** — All communication encrypted (TLS 1.3)
- ✅ **JWT Token Scopes** — Fine-grained permissions (openid scope)
- ✅ **PKCE** — Authorization Code Flow with Proof Key (protects SPA)
- ✅ **CORS Whitelist** — Only trusted origins can access API
- **Lesson:** Security nie single sprint; requires ongoing vigilance

### 📊 Domain Knowledge (Finance/Investing)

- ✅ **Asset Types** — STOCK, CRYPTO, BOND (FOREX removed due to schema constraints)
- ✅ **Portfolio Valuation** — Computing total value, daily changes, profit/loss
- ✅ **Price Snapshots** — Historical data dla charting i trend analysis
- ✅ **Transaction Types** — BUY, SELL, TRANSFER
- **Lesson:** Finite asset types i constraints muszą być znane upfront

### 🔄 Development Process Learnings

#### CI/CD & Version Control
- ✅ **Feature Branches** — Branch per feature/task (ALF-xx/feature-name)
- ✅ **Pull Requests** — Code review, linting, test execution before merge
- ✅ **Semantic Versioning** — Consistent versioning (major.minor.patch)
- **Lesson:** Automated checks (linting, tests) save significant review time

#### Documentation
- ✅ **Architecture Diagrams** — Visual representation of system
- ✅ **API Documentation** — Endpoint paths, params, responses
- ✅ **Setup Instructions** — Onboarding new developers
- ✅ **Demo Scenarios** — Presenting to stakeholders
- **Lesson:** Good documentation multiplies team velocity

#### Debugging & Troubleshooting
- ✅ **Container Logs** — `docker logs service-name`
- ✅ **Database Inspection** — Direct `psql` queries
- ✅ **Network Issues** — Docker network troubleshooting
- ✅ **Seed Data Validation** — Checking migrations executed correctly
- **Lesson:** Systematic debugging (check container, then logs, then DB) saves hours

### 💡 Key Lessons & Best Practices

1. **Constraints must be known early** — FOREX asset type constraint lesson
2. **Seed data must match schema** — On Conflict Do Nothing for idempotency
3. **Demo data should be realistic** — 6-month price history looks more authentic
4. **Separate concerns** — Database per Service, server state vs client state
5. **Test at multiple levels** — Unit, integration, E2E
6. **Security first** — OAuth2, JWT, HTTPS from the start
7. **Documentation pays dividends** — Demo scenario, architecture guide, troubleshooting
8. **Docker Compose for dev, Kubernetes for prod** — Composition vs orchestration tradeoff

---

## 🔗 Linki do dokumentacji

### Backend
- 📄 [Architecture Guide](backend/ARCHITECTURE.md) — Detailed system design, data flows, scaling
- 📋 [DEMO.md](backend/DEMO.md) — Complete setup & verification instructions
- 🚀 [QUICK_START.md](backend/QUICK_START_DEMO.md) — One-page quick reference
- 📊 [DEMO_SCENARIO.md](backend/DEMO_SCENARIO.md) — 5-10 minute demo walkthrough with talking points
- 📚 [API Endpoints](docs/api/) — Portfolio, Market Data, Recommendations endpoints

### Frontend
- 📚 [Testing Guide](frontend/TESTING.md) — Vitest, React Testing Library, Playwright setup
- 🧪 [Test Results](frontend/test-results/) — Latest E2E test results
- 📊 [Coverage Report](frontend/coverage/) — Code coverage statistics

### Infrastructure & Configuration
- 🐳 [docker-compose.yml](backend/docker-compose.yml) — 12-container orchestration
- ⚙️ [ZZPJ-config/](ZZPJ-config/) — Centralized application configuration
- 🔐 [Keycloak Realm](backend/docker/keycloak/fin-insight-realm.json) — Users, roles, clients

### Documentation Hub
- 📚 [docs/architecture.md](docs/architecture.md) — System architecture & design decisions
- 🎯 [docs/conventions.md](docs/conventions.md) — Code style, naming, patterns
- 🤖 [docs/llms.txt](docs/llms.txt) — LLM context for code understanding

### Postman Collections
- 📮 [Portfolio Manager Endpoints](backend/postman/portfolio-manager-all-endpoints.postman_collection.json)
- 📮 [Market Data Service](backend/postman/market-data-service.postman_collection.json)  
- 🔑 [Keycloak Auth Guide](backend/postman/KEYCLOAK_AUTH_GUIDE.md)

---

## 📝 Notatka dla twórców

```bash
# Build frontend production
npm run build

# Run backend tests
cd backend && ./gradlew test

# Check linting
npm run lint

# Format code
npm run format

# Clean Docker
docker-compose down -v
```

---

##  Autorzy

**AlfaTeam** — ZZPJ Projekt Semestralny 2025/2026

```bash
git clone https://github.com/AlfaTeam67/ZZPJ.git
cd ZZPJ
```

## ☕ Backend (Java) – szybki kierunek

- Generowanie modułów przez **Spring Initializr**: https://start.spring.io/
- Build tool: **Gradle Wrapper** (`./gradlew`)
- Przykładowe moduły: `eureka-server`, `config-server`, `portfolio-manager`, `market-data-service`, `ai-advisor-service`

## 🌿 Zasady branchowania

Każdy branch tworzymy z prefiksem typu pracy + ID zadania z Linear:

```text
feature/ALF-17/opis-co-robimy
```

Przykłady:
- `feature/ALF-18/backend-spring-boot-bootstrap`
- `feature/ALF-19/frontend-vite-tailwind-setup`
- `fix/ALF-27/naprawa-integracji-keycloak`

## ✅ Zasady commitów

Rekomendowany format (Conventional Commits):

```text
typ: krótki opis
```

Dozwolone typy:
- `feat:` nowa funkcjonalność (zamiast `feature:`)
- `fix:` poprawka błędu
- `chore:` porządki / techniczne
- `docs:` dokumentacja
- `refactor:` refaktoryzacja bez zmiany działania
- `test:` testy
- `ci:` pipeline / workflow

Przykłady:
- `feat: dodać endpoint healthcheck`
- `fix: poprawić walidację symbolu aktywa`
- `ci: uruchamiać testy backendu na PR`

## 🔍 Zasady Pull Requestów i review

- **Nie pushujemy bezpośrednio na `main`.**
- Każda zmiana idzie przez **PR**.
- Do PR automatycznie uruchamiany jest code review przez **Google Gemini**.
- Do review przypisujemy cały zespół.
- Co najmniej **jedna osoba z zespołu** musi ręcznie zatwierdzić PR.
- Merge dopiero po zielonym CI i zatwierdzeniu.

## 🤖 CI (backend)

W PR uruchamiany jest pipeline backendu:
- testy (`./gradlew test`).

Cel: wychwycić problemy przed mergem i utrzymać stabilny `main`.

## 🛠️ Uruchomienie lokalne

### 📦 Backend (Docker)

Wszystkie serwisy backendowe (Eureka, Config, Portfolio, itd.) oraz bazy danych (Postgres, Redis) i Keycloak:

```bash
cd backend
docker-compose up -d
```

Zatrzymaj wszystko:

```bash
docker-compose down
```

### 💻 Frontend (Node.js + Docker)

Zalecane użycie `nvm`:

```bash
cd frontend
nvm install node
nvm use node
npm install
npm run dev
```

Uruchomienie przez Docker Compose:

```bash
cd frontend
docker-compose up -d
```

## 🗺️ Roadmap (high-level)

1. Standardy repo + workflow zespołu
2. Konfiguracja środowiska backend
3. Konfiguracja środowiska frontend
4. Konteneryzacja lokalna (Docker Compose)
5. Implementacja MVP + jakość kodu + testy

---

Tworzone przez **AlfaTeam** · ZZPJ 2025/2026
