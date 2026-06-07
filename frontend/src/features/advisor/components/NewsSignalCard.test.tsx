import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { NewsSignalCard } from './NewsSignalCard'
import type { NewsItem } from '@/features/advisor/api'

const base: NewsItem = {
  id: '1',
  headline: 'Apple reports record Q2 results',
  source: 'Reuters',
  provider: 'finnhub',
  symbol: 'AAPL',
  url: 'https://reuters.com/apple-q2',
  sentiment: 'POSITIVE',
}

describe('NewsSignalCard', () => {
  it('renders ↑ BUY label for POSITIVE sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'POSITIVE' }} />)
    expect(screen.getByText('↑ BUY')).toBeInTheDocument()
  })

  it('renders → HOLD label for NEUTRAL sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'NEUTRAL' }} />)
    expect(screen.getByText('→ HOLD')).toBeInTheDocument()
  })

  it('renders ↓ SELL label for NEGATIVE sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'NEGATIVE' }} />)
    expect(screen.getByText('↓ SELL')).toBeInTheDocument()
  })

  it('falls back to HOLD for unknown sentiment', () => {
    render(<NewsSignalCard item={{ ...base, sentiment: 'UNKNOWN' }} />)
    expect(screen.getByText('→ HOLD')).toBeInTheDocument()
  })

  it('renders symbol badge and headline', () => {
    render(<NewsSignalCard item={base} />)
    expect(screen.getByText('AAPL')).toBeInTheDocument()
    expect(screen.getByText('Apple reports record Q2 results')).toBeInTheDocument()
  })

  it('renders as a link when url is provided', () => {
    render(<NewsSignalCard item={base} />)
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', 'https://reuters.com/apple-q2')
    expect(link).toHaveAttribute('target', '_blank')
  })

  it('renders without href when url is empty', () => {
    render(<NewsSignalCard item={{ ...base, url: '' }} />)
    const link = screen.queryByRole('link')
    expect(link).toBeNull()
  })
})
