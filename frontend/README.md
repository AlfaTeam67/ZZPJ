# Fin-Insight Frontend

Frontend aplikacji **Fin-Insight** oparty o:

- `Vite + React + TypeScript`
- `Tailwind CSS + shadcn/ui`
- `React Router`
- `React Query`
- `Redux Toolkit`

Projekt jest przygotowany w podejściu **feature-first**, żeby logika każdej funkcjonalności była w jednym miejscu.

## Wymagania

- Node.js (zalecane przez `nvm`)
- npm

W repo jest plik `.nvmrc` z wartością `node`, więc najprościej:

```bash
nvm install node
nvm use node
```

## Szybki start (lokalnie)

1. Wejdź do katalogu frontendu:

```bash
cd frontend
```

2. Zainstaluj zależności:

```bash
npm install
```

3. Skopiuj zmienne środowiskowe:

```bash
cp .env.example .env
```

4. Uruchom aplikację:

```bash
npm run dev
```

Aplikacja będzie dostępna pod `http://localhost:5173`.

## Uruchomienie przez Docker Compose (wariant bardziej produkcyjny)

W katalogu `frontend/`:

```bash
docker compose up --build
```

Kontener buduje frontend statycznie i serwuje build na porcie `4173`.

Po uruchomieniu aplikacja jest dostępna pod `http://localhost:4173`.

## Zmienne środowiskowe

Wymagane zmienne (plik `.env`):

- `VITE_API_URL` — bazowy URL backendu (np. gateway/API)

## Najważniejsze skrypty npm

```bash
npm run dev          # tryb developerski (HMR)
npm run build        # build produkcyjny (TypeScript + Vite)
npm run preview      # podgląd buildu lokalnie
npm run lint         # ESLint
npm run format       # formatowanie Prettier
npm run format:check # sprawdzenie formatowania
npm run types:sync   # synchronizacja typów OpenAPI
```

`types:sync` pobiera schemat z `http://localhost:8080/v3/api-docs` i zapisuje go do `src/types/portfolio.ts`.

## Struktura projektu

```text
src/
  features/
    portfolio/     # logika domeny portfela
    market/        # dane rynkowe
    advisor/       # rekomendacje AI
    auth/          # autoryzacja
  components/
    ui/            # komponenty shadcn/ui + atomy
    layout/        # układ aplikacji (header/sidebar/footer)
  hooks/           # hooki współdzielone
  lib/             # konfiguracje bibliotek (axios, queryClient)
  pages/           # cienkie strony routingu
  store/           # Redux store i slice'y
  types/           # współdzielone typy (flat structure)
  utils/           # helpery ogólne
```

## Jakość i CI

Workflow GitHub Actions `frontend-ci.yml` uruchamia dla zmian we frontendzie:

- instalację zależności (`npm ci`)
- lint (`npm run lint`)
- check formatowania (`npm run format:check`)
- build (`npm run build`)

Dodatkowo zapisuje artifact z gotowym `dist/`.
