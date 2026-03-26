# 🚀 Fin-Insight Backend

Backend mikroserwisowy dla platformy Fin-Insight, zbudowany w Java 21 z Spring Boot 3.5.

## 📋 Spis Treści

- [Architektura](#architektura)
- [Wymagania](#wymagania)
- [Szybki Start](#szybki-start)
- [Serwisy](#serwisy)
- [Konfiguracja](#konfiguracja)
- [Testowanie](#testowanie)
- [Troubleshooting](#troubleshooting)

## 🏗️ Architektura

System składa się z następujących mikroserwisów:

| Serwis | Port | Opis |
|--------|------|------|
| **Eureka Server** | 8761 | Service Discovery - rejestr wszystkich mikroserwisów |
| **Config Server** | 8888 | Centralna konfiguracja z Git backend |
| **Keycloak** | 8080 | Autentykacja i autoryzacja (OAuth2/JWT) |
| **Portfolio Manager** | 8081 | Zarządzanie portfelami inwestycyjnymi |
| **Market Data Service** | 8082 | Agregacja i udostępnianie danych rynkowych |
| **AI Advisor Service** | 8083 | Rekomendacje inwestycyjne wspierane AI |

### Technologie

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Cloud**: 2024.0.x
- **Build Tool**: Gradle 8.14.4 (Wrapper)
- **Database**: PostgreSQL 16
- **Authentication**: Keycloak 24.0
- **Migrations**: Flyway
- **API Docs**: SpringDoc OpenAPI 3
- **Container**: Docker + Docker Compose

### Wzorce Architektoniczne

- **Database per Service**: Każdy serwis ma własną bazę danych
- **Service Discovery**: Dynamiczna rejestracja i odkrywanie serwisów
- **Centralized Configuration**: Wspólna konfiguracja w Git repository
- **API Gateway Pattern**: (planowane - przyszły rozwój)

## ⚙️ Wymagania

### Lokalne Uruchomienie

- Java 21 LTS
- Docker + Docker Compose
- (Opcjonalnie) Gradle (wrapper jest dołączony)

### Docker

- Docker Engine 20.10+
- Docker Compose 2.0+
- Min. 4GB RAM dla kontenerów

## 🚀 Szybki Start

### 0. Repo konfiguracji (wymagane)

Konfiguracja mikroserwisów jest przechowywana w osobnym repo:
`https://github.com/AlfaTeam67/ZZPJ-config.git`

Repozytorium powinno być sklonowane obok katalogu głównego projektu (layout rekomendowany):

```bash
study/
├── ZZPJ/
└── ZZPJ-config/
```

Jeśli używasz innej lokalizacji, ustaw odpowiednio `CONFIG_REPO_PATH` (local run) oraz `CONFIG_REPO_HOST_PATH` (docker-compose).

### Migracja istniejącego `backend/config-repo` do nowego repo

Jeśli masz jeszcze lokalny katalog `backend/config-repo`, przenieś konfigurację do nowego repo:

```bash
# Z katalogu backend
git clone https://github.com/AlfaTeam67/ZZPJ-config.git ../../ZZPJ-config
cp -R ./config-repo/. ../../ZZPJ-config/

# commit w repo konfiguracyjnym
cd ../../ZZPJ-config
git add .
git commit -m "Migrate config from monorepo"
git push
```

Po migracji usuń `backend/config-repo` z głównego repo i korzystaj wyłącznie z `ZZPJ-config`.

### 1. Uruchomienie w Docker (Zalecane, krok po kroku)

```bash
# 1) Przejdź do katalogu backend
cd backend

# 1a) (jednorazowo) sklonuj repo konfiguracji obok projektu
git clone https://github.com/AlfaTeam67/ZZPJ-config.git ../../ZZPJ-config

# 2) Wyczyść poprzednie kontenery i wolumeny (czysty start)
docker-compose down -v --remove-orphans

# 3) Zbuduj obrazy
docker-compose build

# 4) Uruchom wszystko w tle
docker-compose up -d

# 5) Sprawdź status kontenerów
docker-compose ps
```

Jeśli wszystkie serwisy mają status `healthy`, przejdź dalej.

### 2. Konfiguracja Keycloak (wymagane 1 raz)

```bash
# Z katalogu backend
./setup-keycloak.sh
```

Skrypt tworzy:
- realm `fin-insight`
- client `fin-insight-client`
- role `USER`, `ADMIN`
- użytkowników `testuser/testpass` i `admin/adminpass`

Na końcu skrypt wypisze `Client Secret` (zachowaj go do testów JWT).

### 3. Weryfikacja działania

```bash
# Z katalogu backend
./test-services.sh
```

Powinieneś zobaczyć `✓ UP` dla wszystkich serwisów.

### 4. Swagger i endpointy

Używaj:
- Portfolio Manager: `http://localhost:8081/swagger-ui/index.html`
- Market Data Service: `http://localhost:8082/swagger-ui/index.html`
- AI Advisor Service: `http://localhost:8083/swagger-ui/index.html`

Uwaga: `swagger-ui.html` robi przekierowanie (`302`) na `swagger-ui/index.html` — to poprawne.

### 5. JWT i test chronionych API

```bash
# A) Pobierz token (wstaw CLIENT_SECRET ze skryptu setup-keycloak.sh)
./get-token.sh testuser testpass <CLIENT_SECRET>

# B) Skopiuj token i wykonaj request do chronionego endpointu
export TOKEN='<TU_WKLEJ_TOKEN>'
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/portfolios
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/api/symbols
```

### 6. Najczęstsze problemy

- Swagger się nie ładuje: sprawdź `http://localhost:8081/api-docs` i `http://localhost:8082/api-docs` (powinno być `200`).
- `401` na endpointach `/api/**`: to normalne bez tokena JWT.
- Serwis nie startuje: sprawdź logi `docker-compose logs -f <nazwa-serwisu>`.

### 7. Zatrzymanie środowiska

```bash
docker-compose down
```

Lub pełny reset:

```bash
docker-compose down -v --remove-orphans
```

### 8. Uruchomienie Lokalne (Development)

**Krok 1: Uruchom Infrastrukturę**

```bash
# Tylko bazy danych i Keycloak
docker-compose up keycloak-db portfolio-db market-data-db keycloak
```

**Krok 2: Uruchom Serwisy Lokalnie**

```bash
# Terminal 1 - Eureka Server
cd eureka-server
./gradlew bootRun

# Terminal 2 - Config Server (wymaga ustawienia CONFIG_REPO_PATH)
cd config-server
export CONFIG_REPO_PATH=$(pwd)/../../ZZPJ-config
./gradlew bootRun

# Terminal 3 - Portfolio Manager
cd portfolio-manager
./gradlew bootRun

# Terminal 4 - Market Data Service
cd market-data-service
./gradlew bootRun

# Terminal 5 - AI Advisor Service
cd ai-advisor-service
./gradlew bootRun
```

### 3. Weryfikacja

Po uruchomieniu sprawdź:

1. **Eureka Dashboard**: http://localhost:8761
   - Wszystkie serwisy powinny być zarejestrowane

2. **Keycloak Admin Console**: http://localhost:8080
   - Login: `admin` / `admin`
   - Sprawdź realm `fin-insight`

3. **Config Server**: http://localhost:8888/actuator/health
   - Status: UP

4. **Swagger UI**:
   - Portfolio Manager: http://localhost:8081/swagger-ui/index.html
   - Market Data: http://localhost:8082/swagger-ui/index.html
   - AI Advisor: http://localhost:8083/swagger-ui/index.html

5. **Health Endpoints**:
```bash
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8888/actuator/health  # Config
curl http://localhost:8081/actuator/health  # Portfolio
curl http://localhost:8082/actuator/health  # Market Data
curl http://localhost:8083/actuator/health  # AI Advisor
```

## 🔧 Serwisy

### Eureka Server

Service Discovery - automatyczna rejestracja i wykrywanie mikroerwisów.

**Konfiguracja**: `ZZPJ-config/eureka-server.yml`

```bash
# Test lokalny
cd eureka-server
./gradlew test
./gradlew bootRun
```

### Config Server

Centralna konfiguracja z Git backend.

**Lokalizacja konfiguracji**: osobne repo `ZZPJ-config/`

```bash
# Test pobrania konfiguracji
curl http://localhost:8888/portfolio-manager/default
```

### Portfolio Manager

Zarządzanie portfelami inwestycyjnymi użytkowników.

**Endpointy**:
- `GET /api/portfolios` - Lista portfeli
- `GET /api/portfolios/{id}` - Szczegóły portfela
- `POST /api/portfolios` - Utworzenie portfela
- `PUT /api/portfolios/{id}` - Aktualizacja portfela
- `DELETE /api/portfolios/{id}` - Usunięcie portfela

**Database**: PostgreSQL `portfolio` (port 5433)

**Migracje**: Flyway w `src/main/resources/db/migration`

### Market Data Service

Dane rynkowe (akcje, krypto, forex).

**Endpointy**:
- `GET /api/symbols` - Lista symboli
- `GET /api/market-prices/latest` - Najnowsze ceny
- `GET /api/market-prices/symbol/{ticker}` - Historia cen symbolu

**Database**: PostgreSQL `marketdata` (port 5434)

### AI Advisor Service

Rekomendacje inwestycyjne (obecnie mock).

**Endpointy**:
- `POST /api/recommendations` - Otrzymaj rekomendacje
- `GET /api/recommendations/health` - Health check

**Database**: Brak (komunikacja z innymi serwisami)

## 🔐 Konfiguracja Keycloak

### Automatyczna Konfiguracja

Skorzystaj ze skryptu:

```bash
./setup-keycloak.sh
```

### Manualna Konfiguracja

1. Otwórz http://localhost:8080
2. Login: `admin` / `admin`
3. Utwórz realm `fin-insight`
4. Utwórz client `fin-insight-client`:
   - Client Protocol: openid-connect
   - Access Type: confidential
   - Valid Redirect URIs: `*`
   - Web Origins: `*`
5. Utwórz role: `USER`, `ADMIN`
6. Utwórz użytkowników testowych i przypisz role

### Test JWT Authentication

```bash
# 1. Pobierz token z Keycloak
export TOKEN=$(curl -X POST http://localhost:8080/realms/fin-insight/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=fin-insight-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password" | jq -r '.access_token')

# 2. Wywołaj chroniony endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/portfolios
```

## 🧪 Testowanie

### Unit Tests

```bash
# Wszystkie serwisy
for service in eureka-server config-server portfolio-manager market-data-service ai-advisor-service; do
  echo "Testing $service..."
  cd $service
  ./gradlew test
  cd ..
done

# Pojedynczy serwis
cd portfolio-manager
./gradlew test
```

### Integration Tests

```bash
# TODO: Implementacja testów integracyjnych
```

### Docker Build Test

```bash
# Test budowania obrazów
docker-compose build

# Test uruchomienia
docker-compose up -d

# Sprawdź status
docker-compose ps

# Sprawdź logi
docker-compose logs

# Cleanup
docker-compose down
```

## 💰 BigDecimal dla Obliczeń Finansowych

**KRYTYCZNE**: Wszystkie wartości finansowe używają `BigDecimal`, NIGDY `double` lub `float`.

**Przykład (Portfolio Entity)**:

```java
@Column(name = "total_value", precision = 19, scale = 4)
private BigDecimal totalValue;  // Ceny: precision=19, scale=4

@Column(precision = 19, scale = 8)
private BigDecimal quantity;    // Ilości: precision=19, scale=8
```

**W SQL (Flyway)**:

```sql
CREATE TABLE portfolios (
    total_value NUMERIC(19, 4)  -- Ceny
);

CREATE TABLE positions (
    quantity NUMERIC(19, 8)     -- Ilości
);
```

## 🐛 Troubleshooting

### Serwis nie może połączyć się z Eureka

**Problem**: `Connection refused` do Eureka Server

**Rozwiązanie**:
1. Sprawdź czy Eureka działa: `curl http://localhost:8761/actuator/health`
2. Sprawdź logs Eureka: `docker-compose logs eureka-server`
3. Upewnij się, że zmienna `EUREKA_URL` jest poprawna

### Błąd Flyway Migration

**Problem**: `Flyway migration failed`

**Rozwiązanie**:
1. Sprawdź logi PostgreSQL: `docker-compose logs portfolio-db`
2. Zresetuj bazę:
   ```bash
   docker-compose down -v  # Usuwa volumes
   docker-compose up
   ```

### Keycloak JWT Validation Failed

**Problem**: `401 Unauthorized` mimo poprawnego tokena

**Rozwiązanie**:
1. Sprawdź issuer-uri w konfiguracji serwisu
2. Sprawdź czy realm i client istnieją w Keycloak
3. Sprawdź logi: `docker-compose logs keycloak`
4. Zweryfikuj token: https://jwt.io

### Port Already in Use

**Problem**: `Port 8080 is already allocated`

**Rozwiązanie**:
```bash
# Znajdź proces używający portu
lsof -i :8080

# Zabij proces
kill -9 <PID>

# Lub zmień port w docker-compose.yml
```

### Out of Memory

**Problem**: `OutOfMemoryError` podczas budowania

**Rozwiązanie**:
```bash
# Zwiększ pamięć Docker Desktop (min. 4GB)
# Lub buduj z mniejszą liczbą wątków:
./gradlew build -Dorg.gradle.jvmargs=-Xmx1024m
```

## 📚 Przydatne Komendy

```bash
# Docker Compose
docker-compose up -d                    # Uruchom w tle
docker-compose ps                       # Status serwisów
docker-compose logs -f <service>        # Logi (follow)
docker-compose restart <service>        # Restart serwisu
docker-compose down -v                  # Zatrzymaj i usuń volumes
docker-compose build --no-cache         # Przebuduj bez cache

# Gradle
./gradlew clean                         # Wyczyść build
./gradlew build                         # Build
./gradlew bootRun                       # Uruchom
./gradlew test                          # Testy
./gradlew bootJar                       # Zbuduj JAR

# PostgreSQL
docker exec -it fin-insight-portfolio-db psql -U user -d portfolio

# Keycloak
docker exec -it fin-insight-keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password admin
```

## 📖 Dokumentacja API

Każdy serwis biznesowy ma automatycznie generowaną dokumentację OpenAPI:

- **Portfolio Manager**: http://localhost:8081/swagger-ui/index.html
- **Market Data Service**: http://localhost:8082/swagger-ui/index.html
- **AI Advisor Service**: http://localhost:8083/swagger-ui/index.html

## 🤝 Contributing

1. Utwórz branch: `git checkout -b feature/ALF-XXX/opis`
2. Commit zmiany: `git commit -m "feat: opis zmiany"`
3. Push: `git push origin feature/ALF-XXX/opis`
4. Utwórz Pull Request

## 📝 License

Zobacz [LICENSE](../LICENSE) w głównym katalogu projektu.

---

**Tworzone przez AlfaTeam** · ZZPJ 2025/2026
