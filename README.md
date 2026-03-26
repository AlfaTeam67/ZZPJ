# Fin-Insight (ZZPJ)

Kompletny setup projektu z backendem mikroserwisowym (Java 21 + Spring Boot 3.x) oraz frontendem.

## Cel i zgodność z założeniami

Backend jest przygotowany zgodnie z założeniami:
- Java 21 LTS
- Spring Boot 3.x
- Mikroserwisy + database-per-service
- Eureka (service discovery)
- Spring Cloud Config Server (centralna konfiguracja)
- Keycloak (OAuth2/JWT resource server)
- BigDecimal w polach finansowych
- Build: Gradle Wrapper (`./gradlew`), bez Mavena

## Struktura

```text
ZZPJ/
├── backend/
│   ├── eureka-server/
│   ├── config-server/
│   ├── portfolio-manager/
│   ├── market-data-service/
│   └── ai-advisor-service/
├── frontend/
└── README.md
```

## Ważne: osobne repo konfiguracji

Konfiguracja serwisów jest w osobnym repo:
`https://github.com/AlfaTeam67/ZZPJ-config.git`

Lokalny układ katalogów musi być taki:

```text
study/
├── ZZPJ/
└── ZZPJ-config/
```

`backend/docker-compose.yml` montuje config z:
`CONFIG_REPO_HOST_PATH=../../ZZPJ-config`

## Szybki start (Docker, zalecane)

1. Sklonuj repozytoria:

```bash
cd <twoj-katalog-roboczy>
git clone https://github.com/AlfaTeam67/ZZPJ.git
git clone git@github.com:AlfaTeam67/ZZPJ-config.git
```

2. Przygotuj env backendu:

```bash
cd ZZPJ/backend
cp .env.example .env
```

3. Uruchom backend:

```bash
docker-compose down -v --remove-orphans
docker-compose build
docker-compose up -d
```

4. Szybka walidacja:

```bash
./scripts/test-services.sh
curl http://localhost:8888/portfolio-manager/default
```

## Uruchomienie lokalne (bez Docker dla serwisów Java)

Infrastruktura:

```bash
cd ZZPJ/backend
docker-compose up keycloak-db portfolio-db market-data-db keycloak
```

Serwisy (osobne terminale):

```bash
# 1) Eureka
cd eureka-server && ./gradlew bootRun

# 2) Config Server (wymaga ścieżki do ZZPJ-config)
cd config-server
export CONFIG_REPO_PATH=$(pwd)/../../ZZPJ-config
./gradlew bootRun

# 3) Portfolio
cd portfolio-manager && ./gradlew bootRun

# 4) Market Data
cd market-data-service && ./gradlew bootRun

# 5) AI Advisor
cd ai-advisor-service && ./gradlew bootRun
```

## Testy

W każdym module backendu:

```bash
./gradlew test
```

Weryfikowane moduły:
- `eureka-server`
- `config-server`
- `portfolio-manager`
- `market-data-service`
- `ai-advisor-service`

## Potwierdzenie kluczowych założeń technicznych

- Java 21: ustawione w każdym `backend/*/build.gradle` (`JavaLanguageVersion.of(21)`).
- Spring Boot 3.x: `3.5.3` we wszystkich modułach.
- Gradle Wrapper: obecny w każdym module, brak `pom.xml`/`mvnw`.
- Eureka Server: `spring-cloud-starter-netflix-eureka-server`.
- Config Server: `spring-cloud-config-server`.
- Config Client + Eureka Client: obecne w 3 serwisach biznesowych + ai-advisor.
- OAuth2 resource server: obecny w `portfolio-manager`, `market-data-service`, `ai-advisor-service`.
- BigDecimal: używany w encjach i DTO finansowych (np. `Portfolio.totalValue`, `MarketPrice.price`, `MarketPrice.volume`).

## Definition of Done – status

- Moduły backendu istnieją i uruchamiają się przez `./gradlew bootRun`: **TAK**
- Testy `./gradlew test` (smoke/context): **TAK**
- Eureka i Config Server uruchamiają się poprawnie: **TAK**
- Rejestracja serwisów w Eureka: **TAK**
- Integracja Keycloak (resource server + JWT) przygotowana: **TAK**
- BigDecimal w krytycznych polach finansowych: **TAK**

## Uwaga o Cucumber (BDD)

W projekcie są testy JUnit 5 (i wsparcie Mockito przez stack Spring Test), natomiast **Cucumber nie jest obecnie skonfigurowany**.
Jeśli chcesz, mogę dodać minimalny szkielet Cucumber dla jednego serwisu jako kolejny krok.
