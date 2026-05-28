import { describe, it, expect } from 'vitest'
import { render } from '@testing-library/react'
import React from 'react'

import { SectionSkeleton } from '@/components/ui/SectionSkeleton'

describe('SectionSkeleton', () => {
  it('renders the specified number of skeleton lines', () => {
    const { container } = render(<SectionSkeleton lines={5} />)
    const lines = container.querySelectorAll('.animate-pulse')
    expect(lines).toHaveLength(5)
  })

  it('renders 3 lines by default', () => {
    const { container } = render(<SectionSkeleton />)
    const lines = container.querySelectorAll('.animate-pulse')
    expect(lines).toHaveLength(3)
  })

  it('applies custom className', () => {
    const { container } = render(<SectionSkeleton className="mt-8" />)
    expect(container.firstChild).toHaveClass('mt-8')
  })
})
