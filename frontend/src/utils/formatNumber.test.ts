import { describe, it, expect } from 'vitest'
import { formatCurrency, formatPercent, formatSignedCurrency } from '@/utils/formatNumber'

describe('formatNumber', () => {
  describe('formatCurrency', () => {
    it('formats PLN in pl-PL locale', () => {
      const result = formatCurrency('1234.56', 'PLN', 2, 'pl-PL')
      expect(result).toContain('1')
      expect(result).toContain('234')
      expect(result).toContain('zł')
    })

    it('formats USD in en-US locale', () => {
      const result = formatCurrency('1234.56', 'USD', 2, 'en-US')
      expect(result).toContain('$')
      expect(result).toContain('1,234.56')
    })

    it('formats USD in pl-PL locale', () => {
      const result = formatCurrency('1234.56', 'USD', 2, 'pl-PL')
      expect(result).toContain('USD')
    })

    it('defaults to pl-PL locale', () => {
      const result = formatCurrency('100', 'PLN')
      expect(result).toContain('zł')
    })
  })

  describe('formatPercent', () => {
    it('formats positive percent with + sign', () => {
      const result = formatPercent('5.25', 2, 'pl-PL')
      expect(result).toBe('+5,25%')
    })

    it('formats negative percent', () => {
      const result = formatPercent('-3.10', 2, 'pl-PL')
      expect(result).toBe('-3,10%')
    })

    it('formats in en-US locale', () => {
      const result = formatPercent('5.25', 2, 'en-US')
      expect(result).toBe('+5.25%')
    })
  })

  describe('formatSignedCurrency', () => {
    it('formats positive value with + sign', () => {
      const result = formatSignedCurrency('500', 'PLN', 2, 'pl-PL')
      expect(result).toContain('+')
      expect(result).toContain('zł')
    })

    it('formats negative value with − sign', () => {
      const result = formatSignedCurrency('-200', 'USD', 2, 'en-US')
      expect(result).toContain('−')
      expect(result).toContain('$')
    })
  })
})
