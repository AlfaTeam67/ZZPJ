import { test, expect } from '@playwright/test';

test.describe('E2E Financial Application Flow', () => {

  test('Happy Path: login -> dashboard -> create portfolio -> add asset', async ({ page }) => {
    // 1. Przejdź na stronę główną
    await page.goto('/');
    
    // Jeśli sesja wygasła i widzimy formularz Keycloaka - logujemy się
    if (await page.locator('#kc-login').count() > 0) {
      await page.fill('#username', 'testuser');
      await page.fill('#password', 'testpassword');
      await page.click('#kc-login');
    }

    // 2. OCHRONA PRZED LOADEREM (POPRAWIONA):
    // Szukamy elementów ze spinaczem CSS LUB tekstu "Wczytywanie..." / "Loading..." bezpieczną metodą .or()
    const loader = page.locator('.animate-spin')
      .or(page.getByText('Wczytywanie', { exact: false }))
      .or(page.getByText('Loading', { exact: false }))
      .first();

    if (await loader.count() > 0) {
      await loader.waitFor({ state: 'hidden', timeout: 10000 });
    }

    // Upewniamy się, że body jest załączone
    await page.waitForSelector('body', { state: 'attached' });

    // 3. Inteligentne sprawdzenie widoku (pusty vs pełny dashboard)
    const welcomeScreen = page.getByText('Welcome to FinInsight', { exact: false });
    const isNewUser = await welcomeScreen.count() > 0;

    if (isNewUser) {
      // Ścieżka A: Nowy użytkownik (widok powitalny)
      await page.click('a:has-text("Create Portfolio")');
    } else {
      // Ścieżka B: Użytkownik z istniejącymi danymi
      // Czekamy na dowolny stabilny element dashboardu (karta AI lub sekcja akcji)
      const dashboardElement = page.getByText('Ostatnia analiza AI').or(page.getByText('Twoje akcje')).first();
      await expect(dashboardElement).toBeVisible({ timeout: 10000 });
      
      // Klikamy w przycisk nawigacyjny "Create Portfolio"
      await page.click('text=Create Portfolio');
    }

    // 4. Tworzenie nowego portfela z unikalną nazwą opartą o timestamp
    const uniquePortfolioName = `Emerytura E2E - ${Date.now()}`;
    await page.fill('input[id="name"]', uniquePortfolioName);
    await page.fill('textarea[id="description"]', 'Test automatyczny Playwright');
    await page.click('button[type="submit"]:has-text("Create Portfolio")');

    // Sprawdzamy, czy nowy portfel wskoczył na listę
    await expect(page.getByText(uniquePortfolioName)).toBeVisible();

    // 5. Wejście w szczegóły portfela i dodanie aktywa
    await page.click('text=Details');

    await page.fill('input[id="symbol"]', 'AAPL');
    await page.selectOption('select[id="type"]', 'STOCK');
    await page.fill('input[id="quantity"]', '10');
    await page.fill('input[id="price"]', '180.00');
    await page.fill('input[id="currency"]', 'USD');
    await page.click('button[type="submit"]:has-text("Add to Portfolio")');

    // Weryfikacja końcowa - aktywo na liście
    await expect(page.getByText('AAPL')).toBeVisible();
    await expect(page.getByText('Total Invested')).toBeVisible();
  });

  test('Auth Flow: handle logout redirect', async ({ page }) => {
    await page.goto('/');
    
    if (await page.locator('#kc-login').count() > 0) {
      await page.fill('#username', 'testuser');
      await page.fill('#password', 'testpassword');
      await page.click('#kc-login');
    }

    const logoutButton = page.locator('button:has-text("Logout")').first();
    if (await logoutButton.isVisible()) {
      await logoutButton.click();
      await expect(page).toHaveURL(/.*login.*/);
    }
  });
});