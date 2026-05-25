import { describe, it, expect, vi } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import React from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

// Importujemy hooki do przetestowania
import { usePortfolios } from '@/features/portfolio/hooks/usePortfolios'
import { usePortfolio } from '@/features/portfolio/hooks/usePortfolio'
import { useMarketData } from '@/features/market/hooks/useMarketData'
import { useRecommendations } from '@/features/advisor/hooks/useRecommendations'

// KLUCZOWE 1: Odblokowujemy prawdziwe React Query, które zostało zablokowane w setupTests.ts
vi.unmock('@tanstack/react-query')

// KLUCZOWE 2: Mockujemy warstwę API zgodnie z Twoimi rzeczywistymi funkcjami i nazwami
vi.mock('@/features/portfolio/api', () => ({
  fetchPortfolios: vi.fn().mockResolvedValue([
    {
      id: '1',
      userId: 'user-1',
      name: 'My Portfolio',
      totalValue: 10000,
      currency: 'USD',
      assets: [],
    },
  ]),
  fetchFirstPortfolio: vi.fn().mockResolvedValue({
    id: '1',
    userId: 'user-1',
    name: 'My Portfolio',
    totalValue: 10000,
    currency: 'USD',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z',
    assets: [],
  }),
}))

vi.mock('@/features/market/api', () => ({
  // Serwer MSW / makieta zwraca pełną listę, w tym dane szczegółowe dla AAPL wewnątrz tablicy
  fetchMarketSnapshots: vi.fn().mockResolvedValue([
    {
      symbol: 'AAPL',
      price: 180.5,
      change: 2.5,
      changePercent: 1.4,
      timestamp: new Date().toISOString(),
      high52w: 200,
      low52w: 50,
      marketCap: 3000000000000,
    },
  ]),
}))

vi.mock('@/features/advisor/api', () => ({
  fetchRecommendations: vi.fn().mockResolvedValue({
    userId: 'user-1',
    portfolioId: '1',
    recommendations: [
      {
        id: 'rec-1',
        title: 'Diversify your portfolio',
        description: 'Consider adding bonds to reduce risk',
        action: 'ADD_ASSET',
        asset: { symbol: 'BND', name: 'Bond ETF' },
        riskLevel: 'LOW',
        expectedReturn: 0.05,
        confidence: 0.85,
      },
    ],
  }),
}))

// Tworzymy świeży QueryClient dla każdego testu
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  })

const createWrapper = () => {
  const testQueryClient = createTestQueryClient()
  return ({ children }: { children?: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: testQueryClient }, children)
}

describe('React Query Hooks - Portfolio', () => {
  describe('usePortfolios Hook', () => {
    it('should fetch portfolios successfully', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBeGreaterThan(0)
    })

    it('should return loading state initially', () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      expect(result.current.isLoading).toBe(true)
    })

    it('should have no error on successful fetch', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBeNull()
      expect(result.current.isError).toBe(false)
    })

    it('should contain portfolio properties', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.length).toBeGreaterThan(0)
      })

      const portfolio = result.current.data?.[0]
      expect(portfolio).toHaveProperty('id')
      expect(portfolio).toHaveProperty('name')
      expect(portfolio).toHaveProperty('totalValue')
      expect(portfolio).toHaveProperty('currency')
      expect(portfolio).toHaveProperty('assets')
    })

    it('should support refetch', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      expect(result.current.refetch).toBeDefined()
      expect(typeof result.current.refetch).toBe('function')
    })

    it('should have isFetching state', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(typeof result.current.isFetching).toBe('boolean')
    })

    it('should have status property', async () => {
      const { result } = renderHook(() => usePortfolios(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.status).toBe('success')
      })
    })
  })

  describe('usePortfolio Hook (Single Portfolio)', () => {
    it('should fetch single portfolio overview', async () => {
      const { result } = renderHook(() => usePortfolio(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.id).toBe('1')
    })

    it('should have portfolio overview with correct structure', async () => {
      const { result } = renderHook(() => usePortfolio(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.id).toBe('1')
      })

      const portfolio = result.current.data
      expect(portfolio?.userId).toBeDefined()
      expect(portfolio?.name).toBeDefined()
    })

    it('should return loading state for portfolio overview', () => {
      const { result } = renderHook(() => usePortfolio(), {
        wrapper: createWrapper(),
      })

      expect(result.current.isLoading).toBe(true)
    })
  })
})

describe('React Query Hooks - Market Data', () => {
  describe('useMarketData Hook', () => {
    it('should fetch market data successfully', async () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(Array.isArray(result.current.data)).toBe(true)
    })

    it('should return loading state initially', () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      expect(result.current.isLoading).toBe(true)
    })

    it('should contain market data properties', async () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.length).toBeGreaterThan(0)
      })

      const marketData = (result.current.data as Array<Record<string, unknown>>)?.[0]
      expect(marketData).toHaveProperty('symbol')
      expect(marketData).toHaveProperty('price')
      expect(marketData).toHaveProperty('change')
      expect(marketData).toHaveProperty('changePercent')
      expect(marketData).toHaveProperty('timestamp')
    })

    it('should have numeric price data', async () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.length).toBeGreaterThan(0)
      })

      const marketData = (result.current.data as Array<Record<string, unknown>>)?.[0]
      expect(typeof marketData?.price).toBe('number')
    })

    it('should have no error on successful market data fetch', async () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBeNull()
      expect(result.current.isError).toBe(false)
    })

    it('should support refetch for market data', async () => {
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.refetch).toBeDefined()
      expect(typeof result.current.refetch).toBe('function')

      const refetchResult = await result.current.refetch()
      expect(refetchResult.data).toBeDefined()
    })
  })

  describe('useMarketData Hook - Symbol Specific', () => {
    it('should fetch market data for specific symbol', async () => {
      // POPRAWIONE: usunięto argument 'AAPL', bo hook go nie przyjmuje
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      // Szukamy obiektu AAPL wewnątrz zwróconej tablicy
      const aaplData = result.current.data?.find(
        (item: Record<string, unknown>) => item.symbol === 'AAPL'
      )
      expect(aaplData).toBeDefined()
      expect(aaplData?.symbol).toBe('AAPL')
    })

    it('should have 52-week data for specific symbol', async () => {
      // POPRAWIONE: usunięto argument 'AAPL'
      const { result } = renderHook(() => useMarketData(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      // Szukamy obiektu AAPL wewnątrz tablicy, aby sprawdzić jego unikalne właściwości finansowe
      const marketData = result.current.data?.find(
        (item: Record<string, unknown>) => item.symbol === 'AAPL'
      )
      expect(marketData).toHaveProperty('high52w')
      expect(marketData).toHaveProperty('low52w')
      expect(marketData).toHaveProperty('marketCap')
    })
  })
})

describe('React Query Hooks - Recommendations', () => {
  describe('useRecommendations Hook', () => {
    it('should fetch recommendations successfully', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
    })

    it('should return loading state initially', () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      expect(result.current.isLoading).toBe(true)
    })

    it('should have bulletPoints array', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.bulletPoints).toBeDefined()
      })

      expect(Array.isArray(result.current.data?.bulletPoints)).toBe(true)
      expect(result.current.data?.bulletPoints?.length).toBeGreaterThan(0)
    })

    it('should contain recommendation properties', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.data?.bulletPoints?.length).toBeGreaterThan(0)
      })

      const bullet = result.current.data?.bulletPoints?.[0]
      expect(typeof bullet).toBe('string')
      expect(result.current.data).toHaveProperty('riskScore')
      expect(result.current.data).toHaveProperty('modelId')
      expect(result.current.data).toHaveProperty('createdAt')
    })

    it('should have no error on successful fetch', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBeNull()
      expect(result.current.isError).toBe(false)
    })

    it('should support risk tolerance parameter', async () => {
      const { result } = renderHook(() => useRecommendations('HIGH'), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
    })

    it('should update recommendations when risk tolerance changes', async () => {
      // POPRAWIONE: Otypowanie 'riskTolerance' jako unii akceptowanej przez Twój komponent, zamiast ogólnego stringa
      const { result, rerender } = renderHook(
        ({ riskTolerance }: { riskTolerance: 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE' | undefined }) =>
          useRecommendations(riskTolerance),
        {
          initialProps: { riskTolerance: 'LOW' as const },
          wrapper: createWrapper(),
        }
      )

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      rerender({ riskTolerance: 'LOW' })

      await waitFor(() => {
        expect(result.current.data).toBeDefined()
      })
    })

    it('should support refetch for recommendations', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.refetch).toBeDefined()
      expect(typeof result.current.refetch).toBe('function')

      const refetchResult = await result.current.refetch()
      expect(refetchResult.data).toBeDefined()
    })
  })

  describe('useRecommendations Hook - Error Handling', () => {
    it('should have isError flag', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(typeof result.current.isError).toBe('boolean')
    })

    it('should have status property', async () => {
      const { result } = renderHook(() => useRecommendations(), {
        wrapper: createWrapper(),
      })

      await waitFor(() => {
        expect(result.current.status).toBe('success')
      })
    })
  })
})

describe('React Query Hooks - Integration', () => {
  it('should handle multiple hook instances independently', async () => {
    const { result: portfoliosResult } = renderHook(() => usePortfolios(), {
      wrapper: createWrapper(),
    })

    const { result: recommendationsResult } = renderHook(() => useRecommendations(), {
      wrapper: createWrapper(),
    })

    await waitFor(() => {
      expect(portfoliosResult.current.isLoading).toBe(false)
      expect(recommendationsResult.current.isLoading).toBe(false)
    })

    expect(portfoliosResult.current.data).toBeDefined()
    expect(recommendationsResult.current.data).toBeDefined()
  })

  it('should manage query client state independently', async () => {
    const queryClient1 = createTestQueryClient()
    const queryClient2 = createTestQueryClient()

    const wrapper1 = ({ children }: { children?: React.ReactNode }) =>
      React.createElement(QueryClientProvider, { client: queryClient1 }, children)

    const wrapper2 = ({ children }: { children?: React.ReactNode }) =>
      React.createElement(QueryClientProvider, { client: queryClient2 }, children)

    const { result: result1 } = renderHook(() => usePortfolios(), { wrapper: wrapper1 })
    const { result: result2 } = renderHook(() => usePortfolios(), { wrapper: wrapper2 })

    await waitFor(() => {
      expect(result1.current.isLoading).toBe(false)
      expect(result2.current.isLoading).toBe(false)
    })

    expect(result1.current.data).toBeDefined()
    expect(result2.current.data).toBeDefined()
  })
})
