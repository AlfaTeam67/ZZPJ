import { apiClient } from '@/lib/axios'
import { env } from '@/lib/env'
import type {
  AdvisorSnapshot,
  ChartRange,
  PerformancePoint,
  PortfolioMetricSnapshot,
  WatchlistRow,
} from '@/features/dashboard/types'
import type { Portfolio } from '@/types/portfolio/portfolio'
import type { PriceSnapshot } from '@/types/market/price'

// ---------------------------------------------------------------------------
// Portfolio metric — wartość portfela z /api/portfolios + /api/portfolios/:id/valuation
// ---------------------------------------------------------------------------

interface ValuationResponse {
  portfolioId: string
  totalValue: string
  assets: Array<{
    symbol: string
    type: string
    quantity: string
    avgBuyPrice: string
    currentPrice: string | null
    currentValue: string | null
    gainLoss: string | null
    gainLossPct: string | null
  }>
  valuedAt: string
}

export async function fetchPortfolioMetric(): Promise<PortfolioMetricSnapshot> {
  // Backend (po ALF-87) zwraca Page<Portfolio> — obsługujemy oba formaty
  const { data: raw } = await apiClient.get<Portfolio[] | { content: Portfolio[] }>(
    `${env.portfolioApiUrl}/api/portfolios`
  )
  const portfolios = Array.isArray(raw) ? raw : ((raw as { content: Portfolio[] }).content ?? [])

  if (!portfolios || portfolios.length === 0) {
    return {
      totalValue: '0',
      changeAbsolute: '0',
      changePercent: '0',
      changeLabel: 'Brak portfela',
      currency: 'USD',
    }
  }

  // Wycena pierwszego portfela
  const firstId = portfolios[0].id
  try {
    const { data: valuation } = await apiClient.get<ValuationResponse>(
      `${env.portfolioApiUrl}/api/portfolios/${firstId}/valuation`
    )

    // totalValue może być number lub string z backendu
    const total = String(valuation.totalValue ?? '0')

    return {
      totalValue: total,
      changeAbsolute: '0',
      changePercent: '0',
      changeLabel: 'Wartość rynkowa',
      currency: 'USD',
    }
  } catch {
    // Wycena niedostępna — cost-basis z totals portfela
    const totals = portfolios[0].totals ?? {}
    const firstCurrency = Object.keys(totals)[0] ?? 'USD'
    const firstTotal = String(totals[firstCurrency] ?? '0')

    return {
      totalValue: firstTotal,
      changeAbsolute: '0',
      changePercent: '0',
      changeLabel: 'Wartość zakupu',
      currency: firstCurrency,
    }
  }
}

// ---------------------------------------------------------------------------
// Performance series — historia wycen z /api/portfolios/:id/history
// ---------------------------------------------------------------------------

interface ValuationHistoryItem {
  id: string
  portfolioId: string
  valuationDate: string
  totalValue: string
  createdAt: string
}

const RANGE_MONTHS: Record<ChartRange, number> = {
  '1W': 0, // 7 dni
  '1M': 1,
  '3M': 3,
  '1Y': 12,
}

export async function fetchPerformanceSeries(range: ChartRange): Promise<PerformancePoint[]> {
  const { data: raw } = await apiClient.get<Portfolio[] | { content: Portfolio[] }>(
    `${env.portfolioApiUrl}/api/portfolios`
  )
  const portfolios = Array.isArray(raw) ? raw : ((raw as { content: Portfolio[] }).content ?? [])

  if (!portfolios || portfolios.length === 0) return []

  const id = portfolios[0].id
  const to = new Date()
  const from = new Date()

  if (range === '1W') {
    from.setDate(from.getDate() - 7)
  } else {
    from.setMonth(from.getMonth() - RANGE_MONTHS[range])
  }

  const pad = (n: number) => String(n).padStart(2, '0')
  const fmt = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

  try {
    const { data: history } = await apiClient.get<ValuationHistoryItem[]>(
      `${env.portfolioApiUrl}/api/portfolios/${id}/history`,
      { params: { from: fmt(from), to: fmt(to) } }
    )

    if (!history || history.length === 0) {
      // Brak historii — zwróć jeden punkt z aktualną wyceną żeby wykres nie był pusty
      try {
        const { data: valuation } = await apiClient.get<ValuationResponse>(
          `${env.portfolioApiUrl}/api/portfolios/${id}/valuation`
        )
        return [
          {
            label: new Date().toLocaleDateString('pl-PL', { day: 'numeric', month: 'short' }),
            value: parseFloat(String(valuation.totalValue ?? '0')),
          },
        ]
      } catch {
        return []
      }
    }

    return history.map((h) => ({
      label: new Date(h.valuationDate).toLocaleDateString('pl-PL', {
        day: 'numeric',
        month: 'short',
      }),
      value: parseFloat(String(h.totalValue)),
    }))
  } catch {
    return []
  }
}

// ---------------------------------------------------------------------------
// Watchlist — aktywa portfela z aktualną ceną z market service
// ---------------------------------------------------------------------------

export async function fetchWatchlist(): Promise<WatchlistRow[]> {
  const { data: raw } = await apiClient.get<Portfolio[] | { content: Portfolio[] }>(
    `${env.portfolioApiUrl}/api/portfolios`
  )
  const portfolios = Array.isArray(raw) ? raw : ((raw as { content: Portfolio[] }).content ?? [])

  if (!portfolios || portfolios.length === 0) return []

  const firstPortfolio = portfolios[0]
  const assets = firstPortfolio.assets ?? []

  if (assets.length === 0) {
    // assets mogą nie być w odpowiedzi listy — pobierz portfel z detalami
    try {
      const { data: detail } = await apiClient.get<Portfolio>(
        `${env.portfolioApiUrl}/api/portfolios/${firstPortfolio.id}`
      )
      const detailAssets = detail.assets ?? []
      if (detailAssets.length === 0) return []
      return buildWatchlistRows(detailAssets)
    } catch {
      return []
    }
  }

  return buildWatchlistRows(assets)
}

async function buildWatchlistRows(
  assets: NonNullable<Portfolio['assets']>
): Promise<WatchlistRow[]> {
  let snapshots: PriceSnapshot[] = []
  try {
    const { data } = await apiClient.get<PriceSnapshot[]>(
      `${env.marketApiUrl}/api/market-prices/latest`
    )
    snapshots = data ?? []
  } catch {
    // market service niedostępny
  }

  const snapshotMap = new Map(snapshots.map((s) => [s.symbol, s]))

  return assets.slice(0, 8).map((asset) => {
    const snap = snapshotMap.get(asset.symbol)
    const currentPrice = snap?.price != null ? String(snap.price) : asset.avgBuyPrice
    const value = (parseFloat(currentPrice) * parseFloat(asset.quantity)).toFixed(2)
    const changePct = snap?.changePct24h != null ? String(snap.changePct24h) : '0'

    return {
      symbol: asset.symbol,
      name: `${asset.symbol} (${asset.type})`,
      value,
      changePercent: changePct,
      currency: asset.currency,
    }
  })
}

// ---------------------------------------------------------------------------
// Advisor snapshot — ostatnia rekomendacja
// ---------------------------------------------------------------------------

interface RecommendationResponse {
  id: string
  portfolioId: string
  summary: string
  fullText: string
  bulletPoints: string[]
  newsContext: unknown[]
  riskScore: string | null
  modelId: string
  createdAt: string
}

export async function fetchAdvisorSnapshot(): Promise<AdvisorSnapshot> {
  // Pobierz pierwszą rekomendację dla aktualnego użytkownika
  try {
    const { data } = await apiClient.get<{ content: RecommendationResponse[] }>(
      `${env.advisorApiUrl}/api/recommendations/me`,
      { params: { page: 0, size: 1 } }
    )

    const recs = data?.content ?? []
    if (recs.length === 0) {
      return fallbackAdvisorSnapshot()
    }

    const rec = recs[0]
    const riskScore = rec.riskScore ? parseFloat(rec.riskScore) : 0

    return {
      modelLabel: rec.modelId ?? 'AI Model',
      modelTag: (rec.modelId ?? 'AI').toUpperCase().slice(0, 12),
      generatedAt: `Analiza z ${new Date(rec.createdAt).toLocaleString('pl-PL', {
        day: 'numeric',
        month: 'long',
        hour: '2-digit',
        minute: '2-digit',
      })}`,
      body: rec.summary || rec.fullText?.slice(0, 300) || 'Brak analizy.',
      riskScore: Math.min(Math.max(riskScore, 0), 10),
      riskLabel:
        riskScore <= 3 ? 'Niskie ryzyko' : riskScore <= 6 ? 'Umiarkowane ryzyko' : 'Wysokie ryzyko',
    }
  } catch {
    return fallbackAdvisorSnapshot()
  }
}

function fallbackAdvisorSnapshot(): AdvisorSnapshot {
  return {
    modelLabel: 'AI Advisor',
    modelTag: 'AI ADVISOR',
    generatedAt: 'Brak analizy',
    body: 'Wygeneruj pierwszą rekomendację w zakładce Doradca AI.',
    riskScore: 0,
    riskLabel: 'Brak danych',
  }
}
