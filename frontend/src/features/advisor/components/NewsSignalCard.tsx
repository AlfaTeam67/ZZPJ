import { cn } from '@/lib/utils'
import type { NewsItem } from '@/features/advisor/api'

interface SignalConfig {
  label: string
  cardClass: string
  labelClass: string
}

function sentimentToSignal(sentiment: string): SignalConfig {
  switch (sentiment.toUpperCase()) {
    case 'POSITIVE':
      return {
        label: '↑ BUY',
        cardClass: 'bg-green-500/10 border-green-500/35',
        labelClass: 'text-green-400',
      }
    case 'NEGATIVE':
      return {
        label: '↓ SELL',
        cardClass: 'bg-red-500/10 border-red-500/35',
        labelClass: 'text-red-400',
      }
    default:
      return {
        label: '→ HOLD',
        cardClass: 'bg-yellow-500/10 border-yellow-500/35',
        labelClass: 'text-yellow-400',
      }
  }
}

interface NewsSignalCardProps {
  item: NewsItem
}

export function NewsSignalCard({ item }: NewsSignalCardProps) {
  const signal = sentimentToSignal(item.sentiment ?? '')

  const content = (
    <div className={cn('rounded-xl border p-3 transition-colors hover:brightness-110', signal.cardClass)}>
      <div className="mb-1.5 flex items-center justify-between gap-2">
        <span className={cn('text-xs font-bold tracking-wide', signal.labelClass)}>
          {signal.label}
        </span>
        <span className="rounded px-1.5 py-0.5 text-[10px] font-semibold text-blue-300 bg-blue-900/40">
          {item.symbol}
        </span>
      </div>
      <p className="text-xs leading-relaxed text-foreground">{item.headline}</p>
      <p className="mt-1 text-[10px] text-muted-foreground">{item.source}</p>
    </div>
  )

  if (!item.url) return content

  return (
    <a href={item.url} target="_blank" rel="noopener noreferrer" className="block">
      {content}
    </a>
  )
}
