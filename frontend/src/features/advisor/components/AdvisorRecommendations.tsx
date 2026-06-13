import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useMyRecommendations, useGenerateRecommendation } from '@/features/advisor/hooks/useRecommendations'
import type { RecommendationRequest } from '@/features/advisor/api'

type RiskLevel = RecommendationRequest['riskTolerance']
type HorizonLevel = RecommendationRequest['investmentHorizon']

const RISK_LABELS: Record<RiskLevel, string> = {
  LOW: 'Niska',
  MODERATE: 'Umiarkowana',
  HIGH: 'Wysoka',
  AGGRESSIVE: 'Agresywna',
}

const HORIZON_LABELS: Record<HorizonLevel, string> = {
  SHORT_TERM: 'Krótki',
  MID_TERM: 'Średni',
  LONG_TERM: 'Długi',
}

export function AdvisorRecommendations() {
  const [risk, setRisk] = useState<RiskLevel>('MODERATE')
  const [horizon, setHorizon] = useState<HorizonLevel>('MID_TERM')

  const { data: page, isLoading: listLoading } = useMyRecommendations(0)
  const generate = useGenerateRecommendation()

  const recommendations = page?.content ?? []
  const latest = recommendations[0]

  const handleGenerate = () => {
    generate.mutate({ riskTolerance: risk, investmentHorizon: horizon })
  }

  return (
    <div className="space-y-6">
      {/* Generate new recommendation */}
      <Card>
        <CardHeader>
          <CardTitle>Wygeneruj rekomendację AI</CardTitle>
          <CardDescription>
            Spersonalizowana analiza portfela z kontekstem newsów rynkowych.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Risk tolerance */}
          <div className="space-y-2">
            <p className="text-sm font-medium">Tolerancja ryzyka</p>
            <div className="flex flex-wrap gap-2" role="group">
              {(Object.keys(RISK_LABELS) as RiskLevel[]).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRisk(r)}
                  className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                    risk === r
                      ? 'bg-foreground text-background'
                      : 'border border-border/50 text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {RISK_LABELS[r]}
                </button>
              ))}
            </div>
          </div>

          {/* Horizon */}
          <div className="space-y-2">
            <p className="text-sm font-medium">Horyzont inwestycyjny</p>
            <div className="flex flex-wrap gap-2" role="group">
              {(Object.keys(HORIZON_LABELS) as HorizonLevel[]).map((h) => (
                <button
                  key={h}
                  type="button"
                  onClick={() => setHorizon(h)}
                  className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                    horizon === h
                      ? 'bg-foreground text-background'
                      : 'border border-border/50 text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {HORIZON_LABELS[h]}
                </button>
              ))}
            </div>
          </div>

          {generate.isError && (
            <p className="text-sm text-destructive">
              {generate.error instanceof Error
                ? generate.error.message
                : 'Nie udało się wygenerować rekomendacji.'}
            </p>
          )}

          <Button
            className="w-full"
            onClick={handleGenerate}
            disabled={generate.isPending}
          >
            {generate.isPending ? 'Generowanie analizy…' : 'Generuj rekomendację'}
          </Button>
        </CardContent>
      </Card>

      {/* Latest recommendation */}
      {generate.isPending && (
        <Card>
          <CardContent className="py-12 text-center animate-pulse text-muted-foreground">
            Analizuję portfel i dane rynkowe…
          </CardContent>
        </Card>
      )}

      {generate.data && (
        <RecommendationCard rec={generate.data} isNew />
      )}

      {/* History */}
      <div className="space-y-4">
        <h2 className="text-base font-semibold">Historia rekomendacji</h2>
        {listLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-24 animate-pulse rounded-xl bg-muted" />
            ))}
          </div>
        ) : recommendations.length === 0 ? (
          <div className="rounded-xl border border-dashed border-border/50 py-12 text-center text-sm text-muted-foreground">
            Brak historii. Wygeneruj pierwszą rekomendację powyżej.
          </div>
        ) : (
          <div className="space-y-3">
            {recommendations.map((rec) => (
              <RecommendationCard key={rec.id} rec={rec} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------

function RecommendationCard({
  rec,
  isNew = false,
}: {
  rec: {
    id: string
    summary: string
    bulletPoints: string[]
    riskScore: string | null
    modelId: string
    createdAt: string
    newsContext?: Array<{ headline: string; source: string; sentiment: string | null }>
  }
  isNew?: boolean
}) {
  const [expanded, setExpanded] = useState(isNew)
  const riskNum = rec.riskScore ? parseFloat(rec.riskScore) : null

  return (
    <Card className={isNew ? 'border-ring' : ''}>
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-2">
          <div className="flex flex-wrap items-center gap-2">
            {isNew && <Badge className="text-xs">Nowa</Badge>}
            <Badge variant="secondary" className="text-xs font-mono">
              {rec.modelId?.slice(0, 20) ?? 'AI'}
            </Badge>
            {riskNum !== null && (
              <Badge
                variant={riskNum <= 3 ? 'default' : riskNum <= 6 ? 'secondary' : 'destructive'}
                className="text-xs"
              >
                Ryzyko: {riskNum.toFixed(1)}/10
              </Badge>
            )}
          </div>
          <span className="shrink-0 text-xs text-muted-foreground">
            {new Date(rec.createdAt).toLocaleString('pl-PL', {
              day: 'numeric',
              month: 'short',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        </div>
        {rec.summary && (
          <p className="text-sm text-muted-foreground mt-1 leading-relaxed">{rec.summary}</p>
        )}
      </CardHeader>

      {expanded && (
        <CardContent className="space-y-4 pt-0">
          {rec.bulletPoints && rec.bulletPoints.length > 0 && (
            <ul className="space-y-2">
              {rec.bulletPoints.map((bullet, idx) => (
                <li
                  key={idx}
                  className="flex gap-3 items-start rounded-lg border bg-muted/30 p-3 text-sm"
                >
                  <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-foreground/10 text-xs font-bold">
                    {idx + 1}
                  </span>
                  <p className="leading-relaxed">{bullet}</p>
                </li>
              ))}
            </ul>
          )}

          {rec.newsContext && rec.newsContext.length > 0 && (
            <div className="space-y-2">
              <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                Kontekst newsów
              </p>
              {rec.newsContext.slice(0, 4).map((n, i) => (
                <div key={i} className="flex items-start gap-2 rounded-md bg-muted/20 px-3 py-2 text-xs">
                  {n.sentiment && (
                    <span
                      className={`shrink-0 font-medium ${
                        n.sentiment === 'POSITIVE'
                          ? 'text-success'
                          : n.sentiment === 'NEGATIVE'
                            ? 'text-destructive'
                            : 'text-muted-foreground'
                      }`}
                    >
                      {n.sentiment === 'POSITIVE' ? '▲' : n.sentiment === 'NEGATIVE' ? '▼' : '—'}
                    </span>
                  )}
                  <span className="flex-1">{n.headline}</span>
                  <span className="shrink-0 text-muted-foreground">{n.source}</span>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      )}

      <div className="px-6 pb-4">
        <button
          type="button"
          onClick={() => setExpanded((v) => !v)}
          className="text-xs text-muted-foreground hover:text-foreground transition-colors"
        >
          {expanded ? 'Zwiń' : 'Rozwiń szczegóły'}
        </button>
      </div>
    </Card>
  )
}
