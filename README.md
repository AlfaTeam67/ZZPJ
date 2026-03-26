# Fin-Insight (ZZPJ)

Inteligentny asystent inwestora: analiza portfela, dane rynkowe i rekomendacje wspierane przez AI.

## O projekcie

**Fin-Insight** to projekt semestralny zespołu **AlfaTeam** realizowany w ramach ZZPJ.  
Celem jest stworzenie aplikacji, która pomaga użytkownikowi:

- śledzić portfel inwestycyjny,
- agregować dane rynkowe (akcje, krypto, FX),
- otrzymywać kontekstowe rekomendacje oparte o AI.

## Kierunek architektury

- `backend/` – API, logika biznesowa, warstwa danych, integracje zewnętrzne.
- `frontend/` – aplikacja webowa (dashboard, widoki portfela, interfejs użytkownika).

Repozytorium startuje od czystej struktury i będzie rozwijane iteracyjnie.

## Planowany stack

- Backend: Python, FastAPI, SQLAlchemy, Alembic, Poetry, pytest
- Frontend: React + TypeScript, Vite, Tailwind, shadcn/ui
- DevOps: Docker, Docker Compose, CI/CD-ready setup

## Struktura repozytorium

```text
.
├── backend/
├── frontend/
├── LICENSE
└── README.md
```

## Szybki start

```bash
git clone https://github.com/AlfaTeam67/ZZPJ.git
cd ZZPJ
```

## Roadmap (high-level)

1. Ustalenie standardów repo i workflow
2. Konfiguracja środowiska backend
3. Konfiguracja środowiska frontend
4. Konteneryzacja i lokalne środowisko DevOps
5. Implementacja funkcji MVP + testy

## Zasady pracy zespołu

- Krótkie branche per task
- PR do każdej zmiany
- Planowanie zadań w Linear
- Nacisk na jakość kodu, testy i czytelną dokumentację

---

Tworzone przez **AlfaTeam** · ZZPJ 2025/2026
