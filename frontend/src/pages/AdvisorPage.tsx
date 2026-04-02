import { AdvisorRecommendations } from '@/features/advisor/components/AdvisorRecommendations'

export function AdvisorPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">AI Advisor</h1>
      <AdvisorRecommendations />
    </div>
  )
}
