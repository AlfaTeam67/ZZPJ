import type {
  AdvisorSnapshot,
  ChartRange,
  PerformancePoint,
  PortfolioMetricSnapshot,
  WatchlistRow,
} from '@/features/dashboard/types'

/**
 * Mock danych kokpitu. Backendowe endpointy istnieją (/api/portfolios/{id}/valuation,
 * /api/recommendations, /api/market-prices), ale agregacja "wszystko-na-front"
 * jeszcze nie ma swojego endpointa - na razie zwracamy realistyczne dane mockowe,
 * które dokładnie odpowiadają widokowi w designie.
 */

const TOTAL: PortfolioMetricSnapshot = {
  totalValue: '128450.25',
  changeAbsolute: '1245.10',
  changePercent: '0.98',
  changeLabel: 'Dzisiaj',
  currency: 'PLN',
}

const PERFORMANCE_BY_RANGE: Record<ChartRange, PerformancePoint[]> = {
  '1W': makeSeries(['PON', 'WT', 'ŚR', 'CZW', 'PT', 'SOB', 'NDZ'], [125, 124, 126, 127, 128, 127, 128]),
  '1M': makeSeries(
    ['T1', 'T2', 'T3', 'T4', 'T5', 'T6'],
    [121, 119, 122, 124, 127, 128]
  ),
  '3M': makeSeries(
    ['M1', 'M2', 'M3'],
    [115, 122, 128]
  ),
  '1Y': makeSeries(
    ['STYCZEŃ', 'MARZEC', 'MAJ', 'LIPIEC', 'WRZESIEŃ', 'LISTOPAD'],
    [104, 102, 110, 116, 123, 128]
  ),
}

const WATCHLIST: WatchlistRow[] = [
  { symbol: 'AAPL.US', name: 'Apple Inc.', value: '45230.10', changePercent: '12.40', currency: 'PLN' },
  { symbol: 'TSLA.US', name: 'Tesla, Inc.', value: '18940.55', changePercent: '-4.10', currency: 'PLN' },
  { symbol: 'NVDA.US', name: 'NVIDIA Corp.', value: '32110.00', changePercent: '24.10', currency: 'PLN' },
  { symbol: 'MSFT.US', name: 'Microsoft Corp.', value: '21450.20', changePercent: '2.80', currency: 'PLN' },
  { symbol: 'GOOGL.US', name: 'Alphabet Inc.', value: '10719.40', changePercent: '-0.95', currency: 'PLN' },
]

const ADVISOR: AdvisorSnapshot = {
  modelLabel: 'Gemini Flash',
  modelTag: 'GEMINI FLASH',
  generatedAt: 'Analiza z dzisiaj, 08:15',
  body:
    'Twój portfel wykazuje silną ekspozycję na sektor technologiczny (78%). Rozważ częściową realizację zysków na akcjach NVIDIA i dywersyfikację w stronę surowców lub sektora energetycznego, aby zminimalizować zmienność w nadchodzącym kwartale.',
  riskScore: 3,
  riskLabel: 'Niskie Ryzyko',
}

function makeSeries(labels: string[], values: number[]): PerformancePoint[] {
  return labels.map((label, idx) => ({ label, value: values[idx] ?? 0 }))
}

function delay<T>(payload: T, ms = 250): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(payload), ms))
}

export async function fetchPortfolioMetric(): Promise<PortfolioMetricSnapshot> {
  return delay(TOTAL)
}

export async function fetchPerformanceSeries(range: ChartRange): Promise<PerformancePoint[]> {
  return delay(PERFORMANCE_BY_RANGE[range])
}

export async function fetchWatchlist(): Promise<WatchlistRow[]> {
  return delay(WATCHLIST)
}

export async function fetchAdvisorSnapshot(): Promise<AdvisorSnapshot> {
  return delay(ADVISOR)
}
