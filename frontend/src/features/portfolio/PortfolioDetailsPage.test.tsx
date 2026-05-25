import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import { AssetList } from '@/features/portfolio/components/AssetList';
import { TransactionHistory } from '@/features/portfolio/components/TransactionHistory';

vi.unmock('@tanstack/react-query');

vi.mock('@/features/portfolio/api', () => ({
  fetchFirstPortfolio: vi.fn().mockResolvedValue({
    id: '1',
    name: 'Główny Portfel',
    assets: [
      { id: 'asset-1', symbol: 'AAPL', type: 'STOCK', quantity: '10', avgBuyPrice: '150', currency: 'USD' }
    ]
  }),
  fetchTransactions: vi.fn().mockResolvedValue([
    { id: 'tx-1', executedAt: '2026-05-01T12:00:00Z', type: 'BUY', symbol: 'AAPL', quantity: '10', price: '150', currency: 'USD' }
  ]),
}));

const renderDetailsPage = () => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } });
  
  const mockAssets = [
    { 
      id: 'asset-1', 
      portfolioId: '1',
      symbol: 'AAPL', 
      type: 'STOCK' as const, 
      quantity: '10', 
      avgBuyPrice: '150', 
      currency: 'USD',
      addedAt: '2026-05-01T12:00:00Z'
    }
  ];

  return render(
    <QueryClientProvider client={queryClient}>
      <div className="space-y-6">
        <AssetList portfolioId="1" assets={mockAssets} />
        <TransactionHistory portfolioId="1" />
      </div>
    </QueryClientProvider>
  );
};

describe('Integration - PortfolioDetailsPage', () => {
  it('should display asset details, total invested and transaction rows', async () => {
    renderDetailsPage();

    // Weryfikacja listy aktywów i wyceny
    expect(screen.getByText('Assets')).toBeInTheDocument();
    expect(screen.getByText('AAPL')).toBeInTheDocument();
    expect(screen.getByText('Total Invested')).toBeInTheDocument();

    // POPRAWIONE: Obydwie asercje są teraz wewnątrz waitFor, 
    // dzięki czemu test czeka na załadowanie danych z tabeli transakcji
    await waitFor(() => {
      expect(screen.getByText('Transaction History')).toBeInTheDocument();
      expect(screen.getByRole('cell', { name: 'BUY' })).toBeInTheDocument();
    });
  });
});