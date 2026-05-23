# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: portfolio.spec.ts >> E2E Financial Application Flow >> Happy Path: login -> dashboard -> create portfolio -> add asset
- Location: e2e\portfolio.spec.ts:5:3

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: page.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('text=Create Portfolio')

```

# Page snapshot

```yaml
- generic [ref=e4]:
  - banner [ref=e5]:
    - generic [ref=e6]: AlfaTeam · ZZPJ 2025/2026
    - generic [ref=e7]:
      - text: v0.1.0 · realm
      - code [ref=e8]: fin-insight
  - main [ref=e9]:
    - region "Fin-Insight" [ref=e10]:
      - generic [ref=e11]:
        - img [ref=e13]
        - heading "Fin-Insight" [level=1] [ref=e19]
        - paragraph [ref=e20]: Twój osobisty asystent inwestycyjny
      - button "Zaloguj się przez Keycloak" [ref=e22]:
        - img
        - text: Zaloguj się przez Keycloak
      - generic [ref=e23]:
        - paragraph [ref=e24]:
          - img [ref=e25]
          - text: Szyfrowane połączenie TLS 1.3
        - generic [ref=e28]:
          - link "Polityka prywatności" [ref=e29] [cursor=pointer]:
            - /url: https://fin-insight.dev/privacy
          - link "Warunki użytkowania" [ref=e30] [cursor=pointer]:
            - /url: https://fin-insight.dev/tos
            - text: Warunki użytkowania
            - img [ref=e31]
  - contentinfo [ref=e34]:
    - generic [ref=e35]: fin-insight
    - generic [ref=e36]: ver. 0.1.0 · oslona danych
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('E2E Financial Application Flow', () => {
  4  | 
  5  |   test('Happy Path: login -> dashboard -> create portfolio -> add asset', async ({ page }) => {
  6  |     // 1. Przejdź na stronę główną
  7  |     await page.goto('/');
  8  |     
  9  |     // Jeśli sesja wygasła i widzimy formularz Keycloaka - logujemy się
  10 |     if (await page.locator('#kc-login').count() > 0) {
  11 |       await page.fill('#username', 'testuser');
  12 |       await page.fill('#password', 'testpassword');
  13 |       await page.click('#kc-login');
  14 |     }
  15 | 
  16 |     // 2. OCHRONA PRZED LOADEREM (POPRAWIONA):
  17 |     // Szukamy elementów ze spinaczem CSS LUB tekstu "Wczytywanie..." / "Loading..." bezpieczną metodą .or()
  18 |     const loader = page.locator('.animate-spin')
  19 |       .or(page.getByText('Wczytywanie', { exact: false }))
  20 |       .or(page.getByText('Loading', { exact: false }))
  21 |       .first();
  22 | 
  23 |     if (await loader.count() > 0) {
  24 |       await loader.waitFor({ state: 'hidden', timeout: 10000 });
  25 |     }
  26 | 
  27 |     // Upewniamy się, że body jest załączone
  28 |     await page.waitForSelector('body', { state: 'attached' });
  29 | 
  30 |     // 3. Inteligentne sprawdzenie widoku (pusty vs pełny dashboard)
  31 |     const welcomeScreen = page.getByText('Welcome to FinInsight', { exact: false });
  32 |     const isNewUser = await welcomeScreen.count() > 0;
  33 | 
  34 |     if (isNewUser) {
  35 |       // Ścieżka A: Nowy użytkownik (widok powitalny)
  36 |       await page.click('a:has-text("Create Portfolio")');
  37 |     } else {
  38 |       // Ścieżka B: Użytkownik z istniejącymi danymi
  39 |       // Czekamy na dowolny stabilny element dashboardu (karta AI lub sekcja akcji)
  40 |     //   const dashboardElement = page.getByText('Ostatnia analiza AI').or(page.getByText('Twoje akcje')).first();
  41 |     //   await expect(dashboardElement).toBeVisible({ timeout: 10000 });
  42 |       
  43 |       // Klikamy w przycisk nawigacyjny "Create Portfolio"
> 44 |       await page.click('text=Create Portfolio');
     |                  ^ Error: page.click: Test timeout of 30000ms exceeded.
  45 |     }
  46 | 
  47 |     // 4. Tworzenie nowego portfela z unikalną nazwą opartą o timestamp
  48 |     const uniquePortfolioName = `Emerytura E2E - ${Date.now()}`;
  49 |     await page.fill('input[id="name"]', uniquePortfolioName);
  50 |     await page.fill('textarea[id="description"]', 'Test automatyczny Playwright');
  51 |     await page.click('button[type="submit"]:has-text("Create Portfolio")');
  52 | 
  53 |     // Sprawdzamy, czy nowy portfel wskoczył na listę
  54 |     await expect(page.getByText(uniquePortfolioName)).toBeVisible();
  55 | 
  56 |     // 5. Wejście w szczegóły portfela i dodanie aktywa
  57 |     await page.click('text=Details');
  58 | 
  59 |     await page.fill('input[id="symbol"]', 'AAPL');
  60 |     await page.selectOption('select[id="type"]', 'STOCK');
  61 |     await page.fill('input[id="quantity"]', '10');
  62 |     await page.fill('input[id="price"]', '180.00');
  63 |     await page.fill('input[id="currency"]', 'USD');
  64 |     await page.click('button[type="submit"]:has-text("Add to Portfolio")');
  65 | 
  66 |     // Weryfikacja końcowa - aktywo na liście
  67 |     await expect(page.getByText('AAPL')).toBeVisible();
  68 |     await expect(page.getByText('Total Invested')).toBeVisible();
  69 |   });
  70 | 
  71 |   test('Auth Flow: handle logout redirect', async ({ page }) => {
  72 |     await page.goto('/');
  73 |     
  74 |     if (await page.locator('#kc-login').count() > 0) {
  75 |       await page.fill('#username', 'testuser');
  76 |       await page.fill('#password', 'testpassword');
  77 |       await page.click('#kc-login');
  78 |     }
  79 | 
  80 |     const logoutButton = page.locator('button:has-text("Logout")').first();
  81 |     if (await logoutButton.isVisible()) {
  82 |       await logoutButton.click();
  83 |       await expect(page).toHaveURL(/.*login.*/);
  84 |     }
  85 |   });
  86 | });
```