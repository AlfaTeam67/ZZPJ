import { test, expect } from '@playwright/test';

test.describe('E2E Financial Application Flow', () => {

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