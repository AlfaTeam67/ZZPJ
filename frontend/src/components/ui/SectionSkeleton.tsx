import { cn } from '@/lib/utils'

interface SectionSkeletonProps {
  lines?: number
  className?: string
}

export function SectionSkeleton({ lines = 3, className }: SectionSkeletonProps) {
  return (
    <div className={cn('space-y-3', className)}>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="h-4 animate-pulse rounded bg-muted"
          style={{ width: `${85 - i * 12}%` }}
        />
      ))}
    </div>
  )
}
