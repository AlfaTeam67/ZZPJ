import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';

import { PortfolioList } from '@/features/portfolio/components/PortfolioList';
import { CreatePortfolioForm } from '@/features/portfolio/components/CreatePortfolioForm';

vi.unmock('@tanstack/react-query');

const mockCreatePortfolio = vi.fn().mockResolvedValue({ id: '3', name: 'Nowy Fundusz' });

vi.mock('@/features/portfolio/api', () => ({
  fetchPortfolios: vi.fn().mockResolvedValue([
    { id: '1', name: 'Fundusz Emerytalny', description: 'Cele długoterminowe', totals: { USD: 50000 } },
    { id: '2', name: 'Kryptowaluty', description: 'Wysokie ryzyko', totals: { USD: 12000 } },
  ]),
  createPortfolio: () => mockCreatePortfolio(),
}));

const renderPortfolioPage = () => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="space-y-6">
          <CreatePortfolioForm />
          <PortfolioList />
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('Integration - PortfolioPage', () => {
  it('should render portfolio list and support submission of create form', async () => {
    const user = userEvent.setup();
    renderPortfolioPage();

    // Weryfikacja listy portfeli
    await waitFor(() => {
      expect(screen.getByText('Fundusz Emerytalny')).toBeInTheDocument();
    });
    expect(screen.getByText('Kryptowaluty')).toBeInTheDocument();

    // Weryfikacja i interakcja z formularzem tworzenia
    expect(screen.getByLabelText(/Portfolio Name/i)).toBeInTheDocument();
    const nameInput = screen.getByLabelText(/Portfolio Name/i);
    const descInput = screen.getByLabelText(/Description/i);
    const submitButton = screen.getByRole('button', { name: /Create Portfolio/i });

    await user.type(nameInput, 'Nowy Fundusz');
    await user.type(descInput, 'Oszczędności wakacyjne');
    await user.click(submitButton);

    expect(mockCreatePortfolio).toHaveBeenCalled();
  });
});