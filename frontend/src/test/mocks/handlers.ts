import { http, HttpResponse } from 'msw';

const API_URL = 'http://localhost:8080';

export const handlers = [
  // Portfolio endpoints
  http.get(`${API_URL}/api/portfolios`, () => {
    return HttpResponse.json([
      {
        id: '1',
        userId: 'user-1',
        name: 'My Portfolio',
        totalValue: 10000,
        currency: 'USD',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-15T00:00:00Z',
        assets: [
          {
            id: 'asset-1',
            name: 'Apple Inc.',
            symbol: 'AAPL',
            quantity: 10,
            purchasePrice: 150,
            currentPrice: 180,
            totalValue: 1800,
          },
        ],
      },
      {
        id: '2',
        userId: 'user-1',
        name: 'Retirement Portfolio',
        totalValue: 50000,
        currency: 'USD',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-15T00:00:00Z',
        assets: [],
      },
    ]);
  }),

  http.get(`${API_URL}/api/portfolios/first`, () => {
    return HttpResponse.json({
      id: '1',
      userId: 'user-1',
      name: 'My Portfolio',
      totalValue: 10000,
      currency: 'USD',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-15T00:00:00Z',
      assets: [],
    });
  }),

  http.get(`${API_URL}/api/portfolios/:id`, ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      userId: 'user-1',
      name: `Portfolio ${params.id}`,
      totalValue: 10000 + Math.random() * 50000,
      currency: 'USD',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-15T00:00:00Z',
      assets: [],
    });
  }),

  // Market data endpoints
  http.get(`${API_URL}/api/market-data`, () => {
    return HttpResponse.json({
      data: [
        {
          symbol: 'AAPL',
          price: 180.5,
          change: 2.5,
          changePercent: 1.4,
          timestamp: new Date().toISOString(),
        },
        {
          symbol: 'GOOGL',
          price: 140.2,
          change: -1.2,
          changePercent: -0.85,
          timestamp: new Date().toISOString(),
        },
        {
          symbol: 'MSFT',
          price: 380.1,
          change: 5.2,
          changePercent: 1.39,
          timestamp: new Date().toISOString(),
        },
      ],
    });
  }),

  http.get(`${API_URL}/api/market-data/:symbol`, ({ params }) => {
    return HttpResponse.json({
      symbol: params.symbol,
      price: 100 + Math.random() * 400,
      change: Math.random() * 20 - 10,
      changePercent: Math.random() * 5 - 2.5,
      timestamp: new Date().toISOString(),
      high52w: 200,
      low52w: 50,
      marketCap: 3000000000000,
    });
  }),

  // Recommendations endpoints
  http.get(`${API_URL}/api/recommendations`, () => {
    return HttpResponse.json({
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
        {
          id: 'rec-2',
          title: 'Increase tech exposure',
          description: 'Tech sector showing strong growth',
          action: 'ADD_ASSET',
          asset: { symbol: 'QQQ', name: 'Nasdaq ETF' },
          riskLevel: 'MEDIUM',
          expectedReturn: 0.12,
          confidence: 0.7,
        },
        {
          id: 'rec-3',
          title: 'Reduce concentrated position',
          description: 'AAPL position is 18% of portfolio',
          action: 'REDUCE_ASSET',
          asset: { symbol: 'AAPL', name: 'Apple Inc.' },
          riskLevel: 'HIGH',
          expectedReturn: 0.08,
          confidence: 0.9,
        },
      ],
    });
  }),

  http.post(`${API_URL}/api/recommendations`, () => {
    return HttpResponse.json({
      userId: 'user-1',
      portfolioId: '1',
      riskTolerance: 'MEDIUM',
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
