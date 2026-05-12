# 📋 Konwencje kodowania i workflow

## 🌿 Branchowanie

### Format nazwy brancha

```
<typ>/<ID-linear>/<opis>
```

### Typy branchy

| Typ | Opis | Przykład |
|-----|------|---------|
| `feature/` | Nowa funkcjonalność | `feature/ALF-17/dodaj-portfolio-api` |
| `fix/` | Poprawka błędu | `fix/ALF-23/napraw-walidacje-transakcji` |
| `refactor/` | Refaktoryzacja kodu | `refactor/ALF-31/reorganizuj-strukturę-serwisu` |
| `docs/` | Dokumentacja | `docs/ALF-42/dodaj-api-docs` |
| `chore/` | Zadania techniczne | `chore/ALF-15/aktualizuj-dependencies` |

### Zasady

- **Zawsze** tworzymy branch z `main`
- **Nigdy** nie pushujemy bezpośrednio na `main`
- Nazwa brancha powinna być **lowercase** z myślnikami
- ID Linear jest **obowiązkowe**

### Przykłady poprawne

```
feature/ALF-18/backend-spring-boot-bootstrap
feature/ALF-19/frontend-vite-tailwind-setup
fix/ALF-27/naprawa-integracji-keycloak
docs/ALF-45/dokumentacja-endpointow
refactor/ALF-33/czyszczenie-kodu-portfolio-service
```

---

## 💬 Commity

### Format (Conventional Commits)

```
<typ>(<zakres>): <opis>

<opcjonalny opis rozszerzony>

<opcjonalne footer>
```

### Typy commitów

| Typ | Opis | Emoji |
|-----|------|-------|
| `feat` | Nowa funkcjonalność | ✨ |
| `fix` | Poprawka błędu | 🐛 |
| `docs` | Dokumentacja | 📚 |
| `style` | Formatowanie kodu (bez zmian logiki) | 🎨 |
| `refactor` | Refaktoryzacja kodu | ♻️ |
| `perf` | Optymalizacja wydajności | ⚡ |
| `test` | Dodanie/zmiana testów | ✅ |
| `chore` | Zadania techniczne, dependencies | 🔧 |
| `ci` | Zmiany CI/CD | 🚀 |

### Zakres (opcjonalny)

Określa część systemu, którą dotyczy commit:

```
feat(portfolio-manager): dodaj endpoint do usuwania aktywów
fix(market-data): napraw cache'owanie cen
docs(api): aktualizuj dokumentację endpointów
test(auth): dodaj testy dla JWT validation
```

### Przykłady poprawne

```
feat: dodaj endpoint do tworzenia portfela
fix: napraw walidację symbolu aktywa
docs: aktualizuj README
refactor(portfolio-service): wydziel logikę walidacji
test(transaction): dodaj testy dla transakcji
chore: zaktualizuj Spring Boot do 3.2.0
ci: dodaj GitHub Actions workflow
```

### Zasady

- Opis w **imperatywie** (dodaj, napraw, zmień – nie dodałem, naprawiłem)
- Pierwsza litera **lowercase**
- **Bez kropki** na końcu
- Maksymalnie **50 znaków** dla pierwszej linii
- Jeśli potrzebny dłuższy opis – oddziel pustą linią

---

## 🔗 Integracja z Linear

### Linkowanie commitu z taskiem

Dodaj ID Linear w commit message:

```
feat: dodaj endpoint do portfeli ALF-17
```

Lub w opisie:

```
feat: dodaj endpoint do portfeli

Implementuje GET /api/portfolios dla zalogowanego użytkownika.

Fixes ALF-17
```

### Linkowanie PR z taskiem

Tytuł PR powinien zawierać ID:

```
[ALF-17] Dodaj endpoint do portfeli
```

Lub w opisie:

```
Fixes #ALF-17
Closes #ALF-17
```

### Status w Linear

- **Draft PR** → task w `In Progress`
- **PR Review** → task w `In Review`
- **Merged PR** → task w `Done`

---

## 🔄 Pull Request (PR)

### Proces

1. Stwórz branch z `main`
2. Commituj zmiany z konwencją
3. Pushuj branch
4. Otwórz PR na GitHub
5. Dodaj opis i linkowanie do Linear
6. Czekaj na **code review** (Gemini + zespół)
7. Merge po zatwierdzeniu

### Szablon PR

```markdown
## Opis
Krótko opisz co robisz.

## Typ zmian
- [ ] Nowa funkcjonalność
- [ ] Poprawka błędu
- [ ] Refaktoryzacja
- [ ] Dokumentacja

## Linkowanie
Fixes #ALF-17

## Checklist
- [ ] Kod przechodzi testy
- [ ] Dodałem testy
- [ ] Zaktualizowałem dokumentację
- [ ] Nie ma breaking changes
```

### Zasady

- **Tytuł PR** zawiera ID Linear
- **Opis** wyjaśnia co i dlaczego
- **Minimum 1 approval** przed mergem
- **Zielone CI** (testy muszą przejść)
- **Squash merge** dla czystej historii

---

## ✅ Code Review

### Kto reviewuje?

- **Automatycznie**: Google Gemini (code quality)
- **Ręcznie**: Cały zespół (minimum 1 osoba)

### Kryteria review

- ✅ Kod jest czytelny i zrozumiały
- ✅ Konwencje są przestrzegane
- ✅ Testy są wystarczające
- ✅ Brak breaking changes
- ✅ Dokumentacja jest aktualna
- ✅ Performance jest OK

---

## 🚀 Deployment

### Development

```bash
cd backend
docker-compose up -d

cd frontend
npm run dev
```

### Staging/Production

- Automatyczne deployment na merge do `main`
- GitHub Actions workflow
- Monitoring i alerting

---

## 📝 Dokumentacja kodu

### JavaDoc (Backend)

```java
/**
 * Tworzy nowy portfel dla użytkownika.
 *
 * @param request dane do utworzenia portfela
 * @param userId ID zalogowanego użytkownika
 * @return utworzony portfel
 * @throws PortfolioException jeśli portfel już istnieje
 */
public PortfolioResponse createPortfolio(CreatePortfolioRequest request, UUID userId) {
    // implementacja
}
```

### TSDoc (Frontend)

```typescript
/**
 * Pobiera portfele użytkownika z API.
 * @param userId - ID użytkownika
 * @returns Promise z listą portfeli
 * @throws ApiError jeśli request się nie powiedzie
 */
async function getPortfolios(userId: string): Promise<Portfolio[]> {
    // implementacja
}
```

---

## 🔍 Szukanie w kodzie

### Grep (Backend)

```bash
# Szukaj wszystkich TODO
grep -r "TODO" backend/services/

# Szukaj endpointów
grep -r "@PostMapping\|@GetMapping" backend/services/

# Szukaj serwisów
grep -r "@Service" backend/services/
```

### Szukanie w IDE

- **VSCode**: Ctrl+Shift+F (Find in Files)
- **IntelliJ**: Ctrl+Shift+F (Find in Path)

---

## 📊 Metryki

### Coverage testów

Minimum **80%** pokrycia kodu testami.

```bash
./gradlew jacocoTestReport
```

### Linting

```bash
# Backend
./gradlew checkstyleMain

# Frontend
npm run lint
```

---

## 🎯 Checklist przed PR

- [ ] Branch ma poprawną nazwę (`feature/ALF-XX/...`)
- [ ] Commity mają konwencję (`feat:`, `fix:`, itd.)
- [ ] Kod przechodzi testy (`./gradlew test`)
- [ ] Dodałem testy dla nowych funkcji
- [ ] Dokumentacja jest aktualna
- [ ] Nie ma `console.log()` / `System.out.println()`
- [ ] Nie ma hardcodowanych wartości
- [ ] PR ma opis i linkowanie do Linear
