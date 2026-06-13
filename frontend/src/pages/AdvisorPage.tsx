import { AdvisorRecommendations } from '@/features/advisor/components/AdvisorRecommendations'

export function AdvisorPage() {
  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Doradca AI</h1>
        <p className="text-muted-foreground">
          Spersonalizowane rekomendacje inwestycyjne oparte na Twoim portfelu i aktualnych newsach.
        </p>
      </div>
      <AdvisorRecommendations />
    </div>
  )
}
