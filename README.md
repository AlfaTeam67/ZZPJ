# 🚀 Fin-Insight (ZZPJ)

> Inteligentny asystent inwestora: analiza portfela, dane rynkowe i rekomendacje wspierane przez AI.

![Status](https://img.shields.io/badge/status-in%20progress-2563eb)
![Team](https://img.shields.io/badge/team-AlfaTeam-7c3aed)
![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20Vite-0ea5e9)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.x-10b981)
![Course](https://img.shields.io/badge/ZZPJ-2025%2F2026-f59e0b)

## ✨ O projekcie

**Fin-Insight** to projekt semestralny zespołu **AlfaTeam** realizowany w ramach ZZPJ.

Platforma pomaga użytkownikowi:
- śledzić portfel inwestycyjny,
- agregować dane rynkowe (akcje, krypto, FX),
- analizować kontekst rynkowy,
- otrzymywać rekomendacje wspierane przez AI.

## 🧱 Architektura (kierunek)

| Moduł | Odpowiedzialność |
| --- | --- |
| `backend/` | API, logika biznesowa, warstwa danych, integracje |
| `frontend/` | Dashboard, widoki portfela, UX/UI |

## 🛠️ Stack technologiczny

- **Backend:** Java 21 LTS, Spring Boot 3.x, Spring Cloud (Eureka, Config), Keycloak, JPA, Flyway, Gradle Wrapper (`./gradlew`)
- **Frontend:** Node.js (najnowsza), React + TypeScript, Vite, Tailwind, shadcn/ui
- **DevOps:** Docker, Docker Compose, CI (testy backendu)

## 📁 Struktura repozytorium

```text
.
├── backend/
├── frontend/
├── LICENSE
└── README.md
```

## 🔗 Integracja

- Testowe PR tworzymy z ID taska (np. `ALF-17/...`), aby sprawdzić poprawne linkowanie z Linear.

## ⚡ Szybki start

```bash
git clone https://github.com/AlfaTeam67/ZZPJ.git
cd ZZPJ
```

## ☕ Backend (Java) – szybki kierunek

- Generowanie modułów przez **Spring Initializr**: https://start.spring.io/
- Build tool: **Gradle Wrapper** (`./gradlew`)
- Przykładowe moduły: `eureka-server`, `config-server`, `portfolio-manager`, `market-data-service`, `ai-advisor-service`

## 🌿 Zasady branchowania

Każdy branch tworzymy z ID zadania z Linear:

```text
ALF-17/opis-co-robimy
```

Przykłady:
- `ALF-18/backend-spring-boot-bootstrap`
- `ALF-19/frontend-vite-tailwind-setup`

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

## 🗺️ Roadmap (high-level)

1. Standardy repo + workflow zespołu
2. Konfiguracja środowiska backend
3. Konfiguracja środowiska frontend
4. Konteneryzacja lokalna (Docker Compose)
5. Implementacja MVP + jakość kodu + testy

---

Tworzone przez **AlfaTeam** · ZZPJ 2025/2026
