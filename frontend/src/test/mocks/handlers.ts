import { http, HttpResponse } from 'msw';
import { env } from '@/lib/env';

// Korzystamy z dokładnie tych samych zmiennych środowiskowych, co Twój kod produkcyjny
const MARKET_API = env.marketApiUrl;
const PORTFOLIO_API = env.portfolioApiUrl;

export const handlers = [
  // =========================================================================
  // 1. HANDLERY DLA PORTFOLIO-MANAGER ENDPOINTS (`env.portfolioApiUrl`)
  // =========================================================================

  // Pobieranie wszystkich portfeli (fetchPortfolios)
  http.get(`${PORTFOLIO_API}/api/portfolios`, ({ request }) => {
    // SCENARIUSZ 401: Brak nagłówka lub jawne żądanie błędu autoryzacji
    if (request.headers.get('Authorization') === 'Bearer trigger-401') {
      return new HttpResponse(JSON.stringify({ error: 'Unauthorized' }), { status: 401 });
    }
    // SCENARIUSZ 500: Wymuszenie błędu serwera przez nagłówek testowy
    if (request.headers.get('x-trigger-error') === '500') {
      return new HttpResponse(JSON.stringify({ error: 'Internal Server Error' }), { status: 500 });
    }

    return HttpResponse.json([
      {
        id: '1',
        userId: 'user-1',
        name: 'Główny Portfel',
        description: 'Moje oszczędności',
        currency: 'USD',
        totals: { USD: '10000.00', PLN: '40000.00' },
        assets: [
          {
            id: 'asset-1',
            portfolioId: '1',
            symbol: 'AAPL',
            type: 'STOCK',
            quantity: '10',
            avgBuyPrice: '150.00',
            currency: 'USD',
            addedAt: '2026-01-01T12:00:00Z',
          },
        ],
      },
    ]);
  }),

  // Pobieranie konkretnego portfela po ID (fetchPortfolio)
  http.get(`${PORTFOLIO_API}/api/portfolios/:id`, ({ params }) => {
    // SCENARIUSZ 404: Nie znaleziono portfela
    if (params.id === '999') {
      return HttpResponse.json({ error: 'Portfolio not found' }, { status: 404 });
    }

    return HttpResponse.json({
      id: params.id,
      userId: 'user-1',
      name: `Portfel ${params.id}`,
      description: 'Opis portfela',
      currency: 'USD',
      totals: { USD: '15000.00' },
      assets: [],
    });
  }),

  // Tworzenie portfela (createPortfolio)
  http.post(`${PORTFOLIO_API}/api/portfolios`, async ({ request }) => {
    const body = (await request.json()) as { name: string; description?: string };
    return HttpResponse.json(
      {
        id: 'mocked-portfolio-id',
        userId: 'user-1',
        name: body.name,
        description: body.description || '',
        currency: 'USD',
        totals: { USD: '0.00' },
        assets: [],
      },
      { status: 201 }
    );
  }),

  // Usuwanie portfela (deletePortfolio)
  http.delete(`${PORTFOLIO_API}/api/portfolios/:id`, ({ params }) => {
    if (params.id === '999') {
      return HttpResponse.json({ error: 'Portfolio not found' }, { status: 404 });
    }
    return new HttpResponse(null, { status: 204 });
  }),

  // Wycena portfela (fetchPortfolioValuation)
  http.get(`${PORTFOLIO_API}/api/portfolios/:id/valuation`, ({ params }) => {
    if (params.id === '999') {
      return HttpResponse.json({ error: 'Portfolio not found' }, { status: 404 });
    }

    return HttpResponse.json({
      portfolioId: params.id as string,
      totalValue: { USD: '1800.00' },
      assetValuations: [
        {
          assetId: 'asset-1',
          symbol: 'AAPL',
          currentPrice: '180.00',
          currentValue: '1800.00',
          profitPercentage: '20.00',
        },
      ],
    });
  }),

  // Pobieranie transakcji (fetchTransactions)
  http.get(`${PORTFOLIO_API}/api/portfolios/:portfolioId/transactions`, ({ params }) => {
    if (params.portfolioId === '999') {
      return HttpResponse.json([], { status: 404 });
    }

    return HttpResponse.json([
      {
        id: 'tx-1',
        portfolioId: params.portfolioId,
        assetId: 'asset-1',
        type: 'BUY',
        symbol: 'AAPL',
        quantity: '10',
        price: '150.00',
        currency: 'USD',
        executedAt: '2026-05-01T12:00:00Z',
      },
    ]);
  }),

  // Dodawanie nowej transakcji (createTransaction)
  http.post(`${PORTFOLIO_API}/api/portfolios/:portfolioId/transactions`, async ({ request, params }) => {
    const body = (await request.json()) as any;
    return HttpResponse.json(
      {
        id: 'mocked-tx-id',
        portfolioId: params.portfolioId,
        ...body,
        executedAt: body.executedAt || new Date().toISOString(),
      },
      { status: 201 }
    );
  }),

  // =========================================================================
  // 2. HANDLERY DLA MARKET-DATA ENDPOINTS (`env.marketApiUrl`)
  // =========================================================================

  // Pobieranie najnowszych cen (fetchMarketSnapshots)
  http.get(`${MARKET_API}/api/market-prices/latest`, () => {
    return HttpResponse.json([
      {
        symbol: 'AAPL',
        price: 180.5,
        change: 2.5,
        changePercent: '1.4',
        timestamp: new Date().toISOString(),
        high52w: 200,
        low52w: 50,
        marketCap: 3000000000000,
      },
      {
        symbol: 'BTC',
        price: 64000.0,
        change: -1250.0,
        changePercent: '-2.1',
        timestamp: new Date().toISOString(),
        high52w: 69000,
        low52w: 30000,
        marketCap: 1200000000000,
      },
    ]);
  }),

  // Historia cenowa symbolu (fetchPriceHistory)
  http.get(`${MARKET_API}/api/market-prices/symbol/:ticker`, ({ params }) => {
    // SCENARIUSZ 404: Nieznany ticker giełdowy
    if (params.ticker === 'UNKNOWN') {
      return HttpResponse.json({ error: 'Symbol not found' }, { status: 404 });
    }

    return HttpResponse.json([
      { symbol: params.ticker, price: 175.0, timestamp: '2026-05-20T12:00:00Z' },
      { symbol: params.ticker, price: 178.0, timestamp: '2026-05-21T12:00:00Z' },
      { symbol: params.ticker, price: 180.5, timestamp: '2026-05-22T12:00:00Z' },
    ]);
  }),

  // Pobieranie listy dostępnych symboli (fetchSymbols)
  http.get(`${MARKET_API}/api/symbols`, () => {
    return HttpResponse.json([
      { symbol: 'AAPL', type: 'STOCK', active: true },
      { symbol: 'BTC', type: 'CRYPTO', active: true },
    ]);
  }),

  // =========================================================================
  // 3. HANDLERY DLA AI-ADVISOR ENDPOINTS
  // =========================================================================

  // Pobieranie rekomendacji AI i podsumowania (wykorzystywane w testach)
  http.get('http://localhost:8080/api/recommendations', () => {
    return HttpResponse.json({
      userId: 'user-1',
      portfolioId: '1',
      modelTag: 'GPT-4',
      generatedAt: '2026-05-23',
      body: 'Twój portfel wykazuje stabilny wzrost. Rekomendowana dywersyfikacja o obligacje.',
      riskScore: 4,
      riskLabel: 'Umiarkowane ryzyko',
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
    });
  }),
];