import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false, 
  forbidOnly: false,
  retries: 0,
  workers: 1, 
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173', 
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  // POPRAWIONE: Bezpieczniejsze przypisanie profilu przeglądarki Chromium
  projects: [
    {
      name: 'chromium',
      use: devices['Desktop Chrome'], // Usunęliśmy destrukturyzację {...}, podajemy obiekt bezpośrednio
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true,
    timeout: 60 * 1000,
  },
});