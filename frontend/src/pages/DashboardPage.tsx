import { AdvisorSnapshotCard } from '@/features/dashboard/components/AdvisorSnapshotCard'
import { PerformanceChart } from '@/features/dashboard/components/PerformanceChart'
import { PortfolioMetricHero } from '@/features/dashboard/components/PortfolioMetricHero'
import { WatchlistCard } from '@/features/dashboard/components/WatchlistCard'

export function DashboardPage() {
  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-8">
      <PortfolioMetricHero />
      <PerformanceChart />
      <div className="grid gap-6 lg:grid-cols-2">
        <WatchlistCard />
        <AdvisorSnapshotCard />
      </div>
    </div>
  )
}
