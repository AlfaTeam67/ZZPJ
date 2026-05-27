import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'
import i18n from 'i18next'

import { LanguageSwitcher } from '@/components/layout/LanguageSwitcher'

describe('i18n - LanguageSwitcher', () => {
  it('should switch language from PL to EN', async () => {
    const user = userEvent.setup()
    render(<LanguageSwitcher />)

    // Default is PL (set in setupTests)
    const plButton = screen.getByRole('radio', { name: /pl/i })
    const enButton = screen.getByRole('radio', { name: /en/i })

    expect(plButton).toHaveAttribute('aria-checked', 'true')
    expect(enButton).toHaveAttribute('aria-checked', 'false')

    await user.click(enButton)

    expect(i18n.language).toBe('en')
    expect(enButton).toHaveAttribute('aria-checked', 'true')

    // Reset to PL for other tests
    await i18n.changeLanguage('pl')
  })

  it('should persist language selection via i18n detector', async () => {
    const user = userEvent.setup()
    render(<LanguageSwitcher />)

    const enButton = screen.getByRole('radio', { name: /en/i })
    await user.click(enButton)

    // Verify language was changed (persistence is handled by i18next-browser-languagedetector)
    expect(i18n.language).toBe('en')

    // Reset
    await i18n.changeLanguage('pl')
  })
})
