import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useRecommendations } from '@/features/advisor/hooks/useRecommendations'
import { Badge } from '@/components/ui/badge'

export function AdvisorRecommendations() {
  const [risk, setRisk] = useState<'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE'>('MODERATE')
  const [horizon, setHorizon] = useState<'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM'>('MID_TERM')
  const { data, isLoading, refetch, isFetching } = useRecommendations(risk, horizon)

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>AI Portfolio Insights</CardTitle>
            <CardDescription>Personalized recommendations based on your holdings.</CardDescription>
          </div>
          <div className="flex flex-col gap-1">
            <div className="flex gap-2 bg-muted p-1 rounded-md">
              {(['LOW', 'MODERATE', 'HIGH', 'AGGRESSIVE'] as const).map((r) => (
                <button
                  key={r}
                  onClick={() => setRisk(r)}
                  className={`px-3 py-1 text-xs rounded-sm transition-colors ${
                    risk === r ? 'bg-background shadow-sm font-medium' : 'hover:bg-background/50'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
            <div className="flex gap-2 bg-muted p-1 rounded-md">
              {(['SHORT_TERM', 'MID_TERM', 'LONG_TERM'] as const).map((h) => (
                <button
                  key={h}
                  onClick={() => setHorizon(h)}
                  className={`px-3 py-1 text-xs rounded-sm transition-colors ${
                    horizon === h ? 'bg-background shadow-sm font-medium' : 'hover:bg-background/50'
                  }`}
                >
                  {h.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {!data && !isLoading && !isFetching ? (
          <div className="py-10 text-center text-muted-foreground border-2 border-dashed rounded-lg">
            Create a portfolio to receive AI-powered financial advice.
          </div>
        ) : isLoading || isFetching ? (
          <div className="py-10 text-center animate-pulse text-muted-foreground">
            Analyzing market data and portfolio context...
          </div>
        ) : data ? (
          <>
            <div className="flex items-center gap-2 mb-2">
              <Badge variant="secondary">
                Risk score: {data.riskScore?.toFixed(1) ?? 'N/A'}
              </Badge>
              <span className="text-xs text-muted-foreground">
                {data.modelId} · {new Date(data.createdAt).toLocaleString()}
              </span>
            </div>
            {data.summary && (
              <p className="text-sm text-muted-foreground italic">{data.summary}</p>
            )}
            <ul className="grid gap-3">
              {data.bulletPoints?.map((text, idx) => (
                <li
                  key={idx}
                  className="flex gap-3 items-start p-3 rounded-lg border bg-card hover:bg-accent/50 transition-colors"
                >
                  <div className="h-6 w-6 rounded-full bg-primary/10 text-primary flex items-center justify-center text-xs font-bold shrink-0">
                    {idx + 1}
                  </div>
                  <p className="text-sm leading-relaxed">{text}</p>
                </li>
              ))}
            </ul>
          </>
        ) : (
          <div className="py-10 text-center border-2 border-dashed rounded-lg">
            <p className="text-muted-foreground">
              No recommendations available. Create a portfolio first!
            </p>
          </div>
        )}

        <Button className="w-full" onClick={() => refetch()} disabled={isLoading || isFetching}>
          {isLoading || isFetching ? 'Generating...' : 'Refresh AI Analysis'}
        </Button>
      </CardContent>
    </Card>
  )
}
