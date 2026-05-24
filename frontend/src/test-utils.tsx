import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { Provider } from 'react-redux';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { vi } from 'vitest';
import { store } from '@/store/store';

/**
 * Custom render function that includes all providers
 * 
 * Use this instead of render() to test components that need:
 * - Redux store
 * - React Query
 * - Other global providers
 */

// Create a fresh QueryClient for each test
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
      mutations: {
        retry: false,
      },
    },
  });

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  queryClient?: QueryClient;
}

export function renderWithProviders(
  ui: ReactElement,
  {
    queryClient = createTestQueryClient(),
    ...renderOptions
  }: CustomRenderOptions = {}
) {
  function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <Provider store={store}>
        <QueryClientProvider client={queryClient}>
          {children}
        </QueryClientProvider>
      </Provider>
    );
  }

  return {
    ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    queryClient,
  };
}

/**
 * Mock user data for testing
 */
export const mockUser = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  username: 'testuser',
  email: 'test@fininsight.local',
  firstName: 'Test',
  lastName: 'User',
};

export const mockAdminUser = {
  id: '550e8400-e29b-41d4-a716-446655440001',
  username: 'admin',
  email: 'admin@fininsight.local',
  firstName: 'Admin',
  lastName: 'User',
};

/**
 * Mock portfolio data for testing
 */
export const mockPortfolio = {
  id: '550e8400-e29b-41d4-a716-446655440002',
  userId: mockUser.id,
  name: 'My Investment Portfolio',
  description: 'Test portfolio',
  totalValue: 100000,
  currency: 'USD',
  createdAt: new Date('2024-01-01').toISOString(),
  updatedAt: new Date('2024-01-15').toISOString(),
  assets: [],
};

/**
 * Mock API responses for common endpoints
 */
export const mockApiResponses = {
  getPortfolios: {
    data: [mockPortfolio],
  },
  createPortfolio: {
    data: mockPortfolio,
  },
  getMarketPrices: {
    data: [
      {
        symbol: 'AAPL',
        price: 150.25,
        currency: 'USD',
        timestamp: new Date().toISOString(),
      },
    ],
  },
  getRecommendations: {
    data: {
      recommendations: [
        {
          id: '1',
          title: 'Diversify your portfolio',
          description: 'Consider adding bonds to reduce risk',
          riskLevel: 'LOW',
          expectedReturn: 0.05,
        },
      ],
    },
  },
};

/**
 * Wait for async operations in tests
 * 
 * Usage: await waitForAsync();
 */
export const waitForAsync = () =>
  new Promise((resolve) => setTimeout(resolve, 0));

/**
 * Create a mock Keycloak instance
 */
export const createMockKeycloak = (overrides = {}) => ({
  init: vi.fn().mockResolvedValue(true),
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  accountManagement: vi.fn(),
  hasRealmRole: vi.fn().mockReturnValue(true),
  hasClientRole: vi.fn().mockReturnValue(true),
  isTokenExpired: vi.fn().mockReturnValue(false),
  updateToken: vi.fn().mockResolvedValue(true),
  clearToken: vi.fn(),
  loadUserProfile: vi.fn().mockResolvedValue(mockUser),
  token: 'mock-jwt-token',
  refreshToken: 'mock-refresh-token',
  idToken: 'mock-id-token',
  authenticated: true,
  realm: 'fin-insight',
  clientId: 'fin-insight-client',
  ...overrides,
});

// Re-export testing library utilities for convenience.
// This file is a test helper, not an HMR-affected component module, so the
// react-refresh rule does not apply meaningfully here.
// eslint-disable-next-line react-refresh/only-export-components
export * from '@testing-library/react';
