/**
 * Lekkie typy widoku kokpitu. Trzymane osobno od domain types z portfolio/advisor,
 * bo strona główna agreguje dane z trzech serwisów - łatwiej mieć projekcję pod UI.
 */
export interface PortfolioMetricSnapshot {
  /** Wartość portfela jako string z dokładnością BigDecimal. */
  totalValue: string
  /** Bezwzględna zmiana wartości od początku okresu (DZIŚ / 1M / itd.). */
  changeAbsolute: string
  /** Zmiana procentowa, np. 0.98 oznacza +0.98%. */
  changePercent: string
  /** Etykieta okresu, którego dotyczy zmiana. */
  changeLabel: string
  currency: string
}

export type ChartRange = '1W' | '1M' | '3M' | '1Y'

export interface PerformancePoint {
  /** Etykieta osi X (np. "STYCZEŃ"). */
  label: string
  /** Wartość punktu. */
  value: number
}

export interface WatchlistRow {
  symbol: string
  name: string
  /** Łączna wartość pozycji w walucie portfela. */
  value: string
  /** Procentowa zmiana dzienna; ujemna = spadek. */
  changePercent: string
  currency: string
}

export interface AdvisorSnapshot {
  modelLabel: string
  modelTag: string
  generatedAt: string
  body: string
  riskScore: number
  riskLabel: string
}
