#!/bin/bash
# Frontend Testing Infrastructure - Setup Guide

## 📋 Overview

This document describes the testing infrastructure for the Fin-Insight frontend. The setup includes:

- **Vitest**: Fast unit test framework powered by Vite
- **React Testing Library**: Testing utilities for React components
- **jsdom**: DOM environment for tests
- **Setup mocks**: Pre-configured mocks for Keycloak, axios, and React Query

## 🛠️ Installation

All dependencies are already installed. To reinstall:

```bash
cd frontend
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom @vitest/ui
```

## 📂 Files Overview

### `vitest.config.ts`
- Main Vitest configuration
- Configures jsdom environment
- Sets up path aliases (`@/` → `./src/`)
- Defines test file patterns
- Coverage configuration

### `src/setupTests.ts`
- Global test setup file
- **Keycloak mock**: Full mock of Keycloak client
  - Auto-initialized and authenticated
  - Returns mock user profile
  - Includes mock tokens and realm info
  
- **Axios mock**: HTTP client mock
  - All HTTP methods mocked (get, post, put, delete, etc.)
  - Includes interceptors support
  
- **React Query mock**: Data fetching library mock
  - useQuery, useMutation, useQueryClient mocked
  - Safe defaults for loading/error states
  
- **DOM API mocks**: window.matchMedia, IntersectionObserver

### Example Test Files
- `src/example.test.ts` - Basic unit test examples
- `src/example.component.test.tsx` - React component test example

## ▶️ Running Tests

### Run all tests (watch mode)
```bash
npm run test
```

### Run tests once (CI mode)
```bash
npm run test -- --run
```

### Run tests with UI
```bash
npm run test:ui
```

### Generate coverage report
```bash
npm run test:coverage
```

Coverage report is generated in:
- HTML: `coverage/index.html`
- LCOV: `coverage/lcov.info` (for CI tools)

## 📝 Writing Tests

### Basic Test Structure
```typescript
import { describe, it, expect } from 'vitest';

describe('MyFeature', () => {
  it('should do something', () => {
    expect(true).toBe(true);
  });
});
```

### Testing React Components
```typescript
import { render, screen } from '@testing-library/react';
import { MyComponent } from '@/components/MyComponent';

describe('MyComponent', () => {
  it('should render text', () => {
    render(<MyComponent />);
    expect(screen.getByText('Expected text')).toBeInTheDocument();
  });
});
```

### Using Mocked Dependencies
```typescript
import { vi } from 'vitest';
import axios from 'axios';

// Mocks are auto-applied via setupTests.ts
it('should call API', async () => {
  vi.mocked(axios.get).mockResolvedValue({ data: [] });
  // ... test code
});
```

## 🔄 CI Integration

Tests run automatically in GitHub Actions on:
- Push to `main` (frontend changes only)
- Pull requests to `main` (frontend changes only)

The workflow:
1. Installs dependencies with `npm ci`
2. Runs linting with `npm run lint`
3. Checks formatting with `npm run format:check`
4. **Runs tests with coverage** with `npm run test:coverage`
5. Builds with `npm run build`

### GitHub Actions Job
See `.github/workflows/frontend-ci.yml` for the full workflow definition.

## 📊 Code Coverage Goals

Target coverage metrics:
- **Statements**: 70%+
- **Branches**: 65%+
- **Functions**: 70%+
- **Lines**: 70%+

Current coverage: Check `coverage/index.html` after running `npm run test:coverage`

## 🧪 Testing Best Practices

### Do's ✅
- Test behavior, not implementation
- Use semantic queries (`getByRole`, `getByLabelText`)
- Mock external dependencies (API calls, auth)
- Keep tests focused and independent
- Use descriptive test names

### Don'ts ❌
- Don't test React internals
- Avoid testing CSS styles
- Don't use `querySelector` for assertions
- Avoid test interdependencies
- Don't mock everything (only external deps)

## 🐛 Debugging Tests

### Run a single test file
```bash
npm run test -- src/components/MyComponent.test.tsx
```

### Run tests matching a pattern
```bash
npm run test -- --grep "button"
```

### Debug in browser
```bash
npm run test:ui
```
Opens interactive UI at http://localhost:51204

### Console logging in tests
```typescript
import { describe, it, expect } from 'vitest';

it('should log', () => {
  console.log('Debug info');
  expect(true).toBe(true);
});
```

## 🔗 Resources

- [Vitest Documentation](https://vitest.dev)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- [Testing Library Queries](https://testing-library.com/docs/queries/about)
- [Jest DOM Matchers](https://github.com/testing-library/jest-dom)

## 📞 Common Issues

### Issue: "Cannot find module 'keycloak-js'"
**Solution**: The mock in `setupTests.ts` should handle this. Ensure the file is included in `vitest.config.ts` setupFiles.

### Issue: Tests timeout
**Solution**: Check for unresolved promises. Use `vi.clearAllMocks()` in `beforeEach`.

### Issue: DOM not rendering
**Solution**: Ensure `jsdom` is installed and configured in `vitest.config.ts`.

### Issue: Mocks not working
**Solution**: Import mocked modules AFTER they're set up in tests. Don't import in describe block.

## 🚀 Next Steps

1. **Add more tests**: Start with critical components and utilities
2. **Increase coverage**: Aim for 80%+ coverage in core modules
3. **Set up pre-commit hooks**: Use husky + lint-staged to run tests before commits
4. **Monitor coverage trends**: Track coverage over time in CI
5. **Document test patterns**: Create shared test utilities for common scenarios
