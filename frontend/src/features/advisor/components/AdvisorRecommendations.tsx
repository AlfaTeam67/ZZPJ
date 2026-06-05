import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useRecommendations } from '@/features/advisor/hooks/useRecommendations'
import { Badge } from '@/components/ui/badge'

export function AdvisorRecommendations() {
  const { data, isLoading, refetch, isFetching } = useRecommendations()

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>AI Portfolio Insights</CardTitle>
            <CardDescription>Personalized recommendations based on your holdings.</CardDescription>
          </div>
          {data?.riskScore && (
            <Badge variant="secondary">Risk Score: {data.riskScore}</Badge>
          )}
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
              <Badge variant="outline">{data.llmProvider.name}</Badge>
              <span className="text-xs text-muted-foreground">
                Generated at: {new Date(data.createdAt).toLocaleString()}
              </span>
            </div>
            <div className="p-3 rounded-lg border bg-card">
              <p className="text-sm leading-relaxed whitespace-pre-wrap">{data.llmResponse}</p>
            </div>
            {data.news && data.news.length > 0 && (
              <div className="space-y-2">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  News Context
                </p>
                <ul className="grid gap-2">
                  {data.news.map((item) => (
                    <li
                      key={item.id}
                      className="flex gap-2 items-start p-2 rounded-md border bg-muted/40 text-sm"
                    >
                      <span className="font-medium truncate">{item.headline}</span>
                      <span className="text-muted-foreground shrink-0">— {item.source}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
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
