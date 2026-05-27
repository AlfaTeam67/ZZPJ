import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { cn } from '@/lib/utils'
import { usePerformanceSeries } from '@/features/dashboard/hooks/useDashboard'
import type { ChartRange, PerformancePoint } from '@/features/dashboard/types'

const RANGE_IDS: ChartRange[] = ['1W', '1M', '3M', '1Y']
const RANGE_KEYS: Record<ChartRange, string> = {
  '1W': 'range-1w',
  '1M': 'range-1m',
  '3M': 'range-3m',
  '1Y': 'range-1y',
}

const VIEW = { width: 1000, height: 280, padX: 24, padY: 32 }

interface ChartGeometry {
  pathLine: string
  pathArea: string
  points: { x: number; y: number; point: PerformancePoint }[]
}

function buildGeometry(series: PerformancePoint[]): ChartGeometry {
  if (series.length === 0) {
    return { pathLine: '', pathArea: '', points: [] }
  }
  const values = series.map((p) => p.value)
  const min = Math.min(...values)
  const max = Math.max(...values)
  const range = Math.max(max - min, 1)
  const innerWidth = VIEW.width - VIEW.padX * 2
  const innerHeight = VIEW.height - VIEW.padY * 2

  const points = series.map((point, idx) => {
    const xRatio = series.length === 1 ? 0.5 : idx / (series.length - 1)
    const yRatio = (point.value - min) / range
    return {
      x: VIEW.padX + innerWidth * xRatio,
      y: VIEW.padY + innerHeight * (1 - yRatio),
      point,
    }
  })

  const lineSegments = points
    .map((p, idx) => `${idx === 0 ? 'M' : 'L'} ${p.x.toFixed(2)} ${p.y.toFixed(2)}`)
    .join(' ')

  const baseY = VIEW.height - VIEW.padY
  const areaSegments =
    `M ${points[0].x.toFixed(2)} ${baseY.toFixed(2)} ` +
    `L ${points[0].x.toFixed(2)} ${points[0].y.toFixed(2)} ` +
    points
      .slice(1)
      .map((p) => `L ${p.x.toFixed(2)} ${p.y.toFixed(2)}`)
      .join(' ') +
    ` L ${points[points.length - 1].x.toFixed(2)} ${baseY.toFixed(2)} Z`

  return { pathLine: lineSegments, pathArea: areaSegments, points }
}

export function PerformanceChart() {
  const [range, setRange] = useState<ChartRange>('1Y')
  const { data, isLoading } = usePerformanceSeries(range)
  const { t } = useTranslation('dashboard')

  const geometry = useMemo(() => buildGeometry(data ?? []), [data])
  const hasData = !isLoading && (data?.length ?? 0) > 1

  return (
    <section
      aria-labelledby="performance-title"
      className="rounded-2xl border border-border/40 bg-card/60 p-6"
    >
      <header className="flex items-start justify-between gap-4">
        <div>
          <h2 id="performance-title" className="text-base font-semibold">
            {t('chart-title')}
          </h2>
          <p className="mt-1 text-sm text-muted-foreground">{t('chart-subtitle')}</p>
        </div>
        <div
          role="tablist"
          aria-label={t('chart-range-label')}
          className="flex items-center gap-1 rounded-full border border-border/40 bg-muted/30 p-1"
        >
          {RANGE_IDS.map((id) => {
            const active = id === range
            return (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={active}
                onClick={() => setRange(id)}
                className={cn(
                  'rounded-full px-3 py-1 text-xs font-medium transition-colors',
                  active
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                )}
              >
                {t(RANGE_KEYS[id] as 'range-1w' | 'range-1m' | 'range-3m' | 'range-1y')}
              </button>
            )
          })}
        </div>
      </header>

      <div className="mt-6">
        {hasData ? (
          <svg
            viewBox={`0 0 ${VIEW.width} ${VIEW.height}`}
            className="h-[260px] w-full"
            role="img"
            aria-label={t('chart-aria')}
            preserveAspectRatio="none"
          >
            <defs>
              <linearGradient id="perf-area" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="var(--color-brand-primary-400)" stopOpacity="0.45" />
                <stop offset="100%" stopColor="var(--color-brand-primary-400)" stopOpacity="0" />
              </linearGradient>
            </defs>
            <path d={geometry.pathArea} fill="url(#perf-area)" />
            <path
              d={geometry.pathLine}
              fill="none"
              stroke="var(--color-brand-primary-300)"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            {geometry.points.length > 0 ? (
              <circle
                cx={geometry.points[geometry.points.length - 1].x}
                cy={geometry.points[geometry.points.length - 1].y}
                r={5}
                fill="var(--color-brand-primary-200)"
                stroke="var(--background)"
                strokeWidth={2}
              />
            ) : null}
          </svg>
        ) : (
          <div className="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
            {t('chart-loading')}
          </div>
        )}
      </div>

      {hasData ? (
        <div className="mt-4 flex justify-between text-[10px] uppercase tracking-[0.25em] text-muted-foreground">
          {data!.map((p) => (
            <span key={p.label}>{p.label}</span>
          ))}
        </div>
      ) : null}
    </section>
  )
}
