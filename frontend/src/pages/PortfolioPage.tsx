import { PortfolioOverview } from '@/features/portfolio/components/PortfolioOverview'

export function PortfolioPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Portfolio</h1>
      <PortfolioOverview />
    </div>
  )
}
