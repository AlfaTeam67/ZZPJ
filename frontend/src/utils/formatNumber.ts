import Decimal from 'decimal.js'

const PL = 'pl-PL'

/**
 * Format pieniądza z grupowaniem tysięcy spacją i przecinkiem dziesiętnym (PL).
 * Przykład: formatCurrency('128450.25', 'PLN') -> "128 450,25 zł"
 */
export function formatCurrency(value: string, currency: string, fractionDigits = 2): string {
  const numeric = new Decimal(value).toNumber()
  if (currency === 'PLN') {
    const number = new Intl.NumberFormat(PL, {
      minimumFractionDigits: fractionDigits,
      maximumFractionDigits: fractionDigits,
    }).format(numeric)
    return `${number} zł`
  }
  return new Intl.NumberFormat(PL, {
    style: 'currency',
    currency,
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(numeric)
}

/**
 * Format procentowy ze znakiem (np. "+0,98%" / "-4,10%").
 */
export function formatPercent(value: string, fractionDigits = 2): string {
  const numeric = new Decimal(value)
  const sign = numeric.greaterThanOrEqualTo(0) ? '+' : ''
  const number = new Intl.NumberFormat(PL, {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(numeric.toNumber())
  return `${sign}${number}%`
}

/**
 * Format absolutnej zmiany ze znakiem.
 */
export function formatSignedCurrency(value: string, currency: string, fractionDigits = 2): string {
  const numeric = new Decimal(value)
  const sign = numeric.greaterThanOrEqualTo(0) ? '+' : '−'
  const formatted = formatCurrency(numeric.abs().toString(), currency, fractionDigits)
  return `${sign}${formatted}`
}
