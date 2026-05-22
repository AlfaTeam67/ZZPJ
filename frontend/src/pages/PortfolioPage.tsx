import { PortfolioList } from '@/features/portfolio/components/PortfolioList'
import { CreatePortfolioForm } from '@/features/portfolio/components/CreatePortfolioForm'

export function PortfolioPage() {
  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Portfolios</h1>
        <p className="text-muted-foreground">
          Manage your investment portfolios and track their performance.
        </p>
      </div>

      <div className="grid gap-8 lg:grid-cols-[1fr,350px]">
        <div className="space-y-6">
          <PortfolioList />
        </div>
        <aside className="space-y-6">
          <CreatePortfolioForm />
        </aside>
      </div>
    </div>
  )
}
