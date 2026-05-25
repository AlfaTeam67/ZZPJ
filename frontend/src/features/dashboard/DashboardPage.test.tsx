import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

import { PortfolioMetricHero } from '@/features/dashboard/components/PortfolioMetricHero'
import { PerformanceChart } from '@/features/dashboard/components/PerformanceChart'
import { WatchlistCard } from '@/features/dashboard/components/WatchlistCard'
import { AdvisorSnapshotCard } from '@/features/dashboard/components/AdvisorSnapshotCard'

vi.unmock('@tanstack/react-query')

// Mockowanie warstwy API dla Dashboardu
vi.mock('@/features/dashboard/api', () => ({
  fetchPortfolioMetric: vi.fn().mockResolvedValue({
    totalValue: '124500.50',
    currency: 'PLN',
    changeAbsolute: '1250.00',
    changePercent: '1.02',
    changeLabel: 'od wczoraj',
  }),
  fetchPerformanceSeries: vi.fn().mockResolvedValue([
    { label: 'Sty', value: 10000 },
    { label: 'Lut', value: 12000 },
  ]),
  fetchWatchlist: vi.fn().mockResolvedValue([
    { symbol: 'AAPL', name: 'Apple Inc.', value: '180.50', currency: 'USD', changePercent: '1.4' },
    { symbol: 'BTC', name: 'Bitcoin', value: '64000.00', currency: 'USD', changePercent: '-2.1' },
  ]),
  fetchAdvisorSnapshot: vi.fn().mockResolvedValue({
    modelTag: 'GPT-4',
    generatedAt: '2026-05-23',
    body: 'Twój portfel wykazuje stabilny wzrost. Rekomendowana dywersyfikacja.',
    riskScore: 4,
    riskLabel: 'Umiarkowane ryzyko',
  }),
}))

const createTestQueryClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } })

const renderDashboard = () => {
  const queryClient = createTestQueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="space-y-6 p-6">
          <PortfolioMetricHero />
          <PerformanceChart />
          <WatchlistCard />
          <AdvisorSnapshotCard />
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

describe('Integration - DashboardPage', () => {
  it('should render all dashboard sections and metrics correctly', async () => {
    renderDashboard()

    // POPRAWIONE: Wszystkie kluczowe asercje trafiły do wnętrza waitFor,
    // dając bibliotece React Testing Library czas na odebranie danych z makiety API.
    await waitFor(() => {
      // Weryfikacja metryk Hero
      expect(screen.getByText('Całkowita wartość portfela')).toBeInTheDocument()

      // POPRAWIONE: Uproszczony regex bez błędnej alternatywy logicznej, łapie kwotę niezależnie od spacji
      expect(screen.getByText(/124\s?500/)).toBeInTheDocument()

      // Weryfikacja wykresu (szukamy po roli i obecności ścieżek SVG)
      expect(screen.getByLabelText('Wykres historii wyników portfela')).toBeInTheDocument()

      // Weryfikacja listy obserwowanych (Watchlist)
      expect(screen.getByText('Twoje akcje')).toBeInTheDocument()
      expect(screen.getByText('AAPL')).toBeInTheDocument()
      expect(screen.getByText('BTC')).toBeInTheDocument()

      // Weryfikacja karty analizy AI
      expect(screen.getByText('Ostatnia analiza AI')).toBeInTheDocument()
      expect(screen.getByText('GPT-4')).toBeInTheDocument()
      expect(screen.getByText(/Stabilny wzrost/i)).toBeInTheDocument()
    })
  })
})
