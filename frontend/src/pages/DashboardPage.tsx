import { AdvisorRecommendations } from '@/features/advisor/components/AdvisorRecommendations'
import { MarketTicker } from '@/features/market/components/MarketTicker'
import { PortfolioOverview } from '@/features/portfolio/components/PortfolioOverview'

export function DashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
      <div className="grid gap-6 lg:grid-cols-2">
        <PortfolioOverview />
        <MarketTicker />
      </div>
      <AdvisorRecommendations />
    </div>
  )
}
