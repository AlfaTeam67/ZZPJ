import { HugeiconsIcon } from '@hugeicons/react'
import { ArrowRight01Icon } from '@hugeicons/core-free-icons'
import { Link } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { useAdvisorSnapshot } from '@/features/dashboard/hooks/useDashboard'
import { cn } from '@/lib/utils'

interface RiskGaugeProps {
  /** Wartość 0..10 */
  score: number
}

function RiskGauge({ score }: RiskGaugeProps) {
  const clamped = Math.min(Math.max(score, 0), 10)
  const radius = 26
  const circumference = 2 * Math.PI * radius
  const dash = (clamped / 10) * circumference

  return (
    <div className="relative size-16" role="img" aria-label={`Ryzyko ${clamped} na 10`}>
      <svg viewBox="0 0 64 64" className="size-16 -rotate-90">
        <circle cx="32" cy="32" r={radius} stroke="var(--color-brand-neutral-800)" strokeWidth="4" fill="none" />
        <circle
          cx="32"
          cy="32"
          r={radius}
          stroke="var(--color-brand-secondary-400)"
          strokeWidth="4"
          fill="none"
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circumference - dash}`}
        />
      </svg>
      <span className="absolute inset-0 flex items-center justify-center text-xs font-semibold tabular-nums">
        {clamped}/10
      </span>
    </div>
  )
}

export function AdvisorSnapshotCard() {
  const { data, isLoading } = useAdvisorSnapshot()

  return (
    <section
      aria-labelledby="advisor-snapshot-title"
      className="flex h-full flex-col rounded-2xl border border-border/40 bg-card/60 p-6"
    >
      <header className="flex items-center justify-between">
        <h2 id="advisor-snapshot-title" className="text-base font-semibold">
          Ostatnia analiza AI
        </h2>
        <Link
          to="/advisor"
          className="flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
        >
          Historia analiz
          <HugeiconsIcon icon={ArrowRight01Icon} className="size-3" aria-hidden />
        </Link>
      </header>

      {isLoading || !data ? (
        <div className="mt-6 flex flex-1 flex-col gap-3">
          <div className="h-4 w-40 animate-pulse rounded bg-muted" />
          <div className="h-3 w-full animate-pulse rounded bg-muted/70" />
          <div className="h-3 w-5/6 animate-pulse rounded bg-muted/70" />
          <div className="h-3 w-4/6 animate-pulse rounded bg-muted/70" />
        </div>
      ) : (
        <div className="mt-5 flex flex-1 flex-col rounded-xl bg-muted/30 p-5">
          <div className="flex items-center gap-3">
            <span
              className={cn(
                'rounded-md bg-brand-primary-300/90 px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.15em] text-brand-neutral-900'
              )}
            >
              {data.modelTag}
            </span>
            <span className="text-xs text-muted-foreground">{data.generatedAt}</span>
          </div>

          <p className="mt-4 text-sm leading-relaxed text-foreground/90">{data.body}</p>

          <div className="mt-auto flex items-center justify-between gap-4 pt-6">
            <div className="flex items-center gap-3">
              <RiskGauge score={data.riskScore} />
              <div>
                <p className="text-sm font-semibold">{data.riskLabel}</p>
                <p className="text-xs text-muted-foreground">Wskaźnik stabilności portfela</p>
              </div>
            </div>
            <Button asChild size="sm">
              <Link to="/advisor">Pełny raport</Link>
            </Button>
          </div>
        </div>
      )}
    </section>
  )
}
