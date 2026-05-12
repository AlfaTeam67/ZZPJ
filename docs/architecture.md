# 🏗️ Architektura Systemu

## Przegląd ogólny

Fin-Insight to system mikroserwisów oparty na Spring Boot z frontendem React. Każdy serwis ma własną bazę danych i komunikuje się przez REST API.

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (React + Vite)                │
│                    http://localhost:3000                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                    HTTP/REST API
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    API Gateway / Eureka                      │
│                  (Service Discovery)                         │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┬─────────────────┐
        │                │                │                 │
   ┌────▼────┐    ┌─────▼──────┐  ┌─────▼──────┐  ┌────────▼────┐
   │Portfolio │    │Market Data │  │AI Advisor  │  │  Keycloak   │
   │Manager   │    │Service     │  │Service     │  │(Auth)       │
   │:8081     │    │:8082       │  │:8083       │  │:8080        │
   └────┬─────┘    └─────┬──────┘  └─────┬──────┘  └────────┬────┘
        │                │               │                  │
   ┌────▼─────┐    ┌─────▼──────┐  ┌─────▼──────┐    ┌──────▼──────┐
   │Portfolio  │    │Market Data │  │Advisor     │    │Keycloak DB  │
   │DB         │    │DB          │  │DB          │    │(PostgreSQL) │
   │:5433      │    │:5434       │  │:5435       │    │             │
   └───────────┘    └────────────┘  └────────────┘    └─────────────┘
        │                │               │
        └────────────────┼───────────────┘
                         │
                    ┌────▼────┐
                    │  Redis   │
                    │ Cache    │
                    │:6379     │
                    └──────────┘
```

## Komponenty systemu

### Backend Services

| Serwis | Port | Baza | Odpowiedzialność |
|--------|------|------|------------------|
| **Eureka Server** | 8761 | - | Service Discovery, rejestracja serwisów |
| **Config Server** | 8888 | - | Centralna konfiguracja |
| **Portfolio Manager** | 8081 | PostgreSQL 5433 | Portfele, aktywa, transakcje |
| **Market Data Service** | 8082 | PostgreSQL 5434 | Ceny, symbole, dane rynkowe |
| **AI Advisor Service** | 8083 | PostgreSQL 5435 | Rekomendacje AI |
| **Keycloak** | 8080 | PostgreSQL | Autentykacja OAuth2/OIDC |

### Frontend

| Komponent | Port | Technologia |
|-----------|------|-------------|
| **React App** | 3000 | React 18+, TypeScript, Vite |

### Infrastruktura

| Komponent | Port | Opis |
|-----------|------|------|
| **PostgreSQL** | 5433-5435 | Bazy danych serwisów |
| **Redis** | 6379 | Cache dla danych rynkowych |

## Wzorzec komunikacji

### Synchroniczna (REST)

```
Frontend → Portfolio Manager → Market Data Service
```

Serwisy komunikują się bezpośrednio przez REST API.

### Asynchroniczna (przyszłość)

Planowana integracja z message brokerem (RabbitMQ/Kafka) dla zdarzeń domenowych.

## Bezpieczeństwo

- **Keycloak** – centralne zarządzanie tożsamością
- **JWT Tokens** – stateless autentykacja
- **Spring Security** – autoryzacja na poziomie endpointów
- **HTTPS** – szyfrowanie transportu (production)

## Skalowanie

- **Eureka** – automatyczne odkrywanie serwisów
- **Config Server** – dynamiczna konfiguracja
- **Redis** – cache dla wysokiej dostępności
- **PostgreSQL** – replikacja (production)

## Deployment

### Development
- Docker Compose (wszystkie serwisy w kontenerach)
- Localhost networking

### Production (przyszłość)
- Kubernetes
- Load balancer (Nginx/HAProxy)
- Monitoring (Prometheus, Grafana)
- Logging (ELK Stack)
