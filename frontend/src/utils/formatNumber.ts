import Decimal from 'decimal.js'

type Locale = 'pl-PL' | 'en-US'

const DEFAULT_LOCALE: Locale = 'pl-PL'

/**
 * Format pieniądza z grupowaniem tysięcy.
 * Przykład PL: formatCurrency('128450.25', 'PLN') -> "128 450,25 zł"
 * Przykład EN: formatCurrency('128450.25', 'USD', 2, 'en-US') -> "$128,450.25"
 */
export function formatCurrency(
  value: string,
  currency: string,
  fractionDigits = 2,
  locale: Locale = DEFAULT_LOCALE
): string {
  const numeric = new Decimal(value).toNumber()
  if (currency === 'PLN' && locale === 'pl-PL') {
    const number = new Intl.NumberFormat(locale, {
      minimumFractionDigits: fractionDigits,
      maximumFractionDigits: fractionDigits,
    }).format(numeric)
    return `${number} zł`
  }
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(numeric)
}

/**
 * Format procentowy ze znakiem (np. "+0,98%" / "-4,10%").
 */
export function formatPercent(
  value: string,
  fractionDigits = 2,
  locale: Locale = DEFAULT_LOCALE
): string {
  const numeric = new Decimal(value)
  const sign = numeric.greaterThanOrEqualTo(0) ? '+' : ''
  const number = new Intl.NumberFormat(locale, {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(numeric.toNumber())
  return `${sign}${number}%`
}

/**
 * Format absolutnej zmiany ze znakiem.
 */
export function formatSignedCurrency(
  value: string,
  currency: string,
  fractionDigits = 2,
  locale: Locale = DEFAULT_LOCALE
): string {
  const numeric = new Decimal(value)
  const sign = numeric.greaterThanOrEqualTo(0) ? '+' : '−'
  const formatted = formatCurrency(numeric.abs().toString(), currency, fractionDigits, locale)
  return `${sign}${formatted}`
}
