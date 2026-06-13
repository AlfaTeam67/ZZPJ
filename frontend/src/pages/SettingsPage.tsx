import { useState } from 'react'
import { HugeiconsIcon } from '@hugeicons/react'
import { Moon02Icon, Sun03Icon } from '@hugeicons/core-free-icons'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { useDarkMode } from '@/hooks/useDarkMode'
import { useToast } from '@/components/ui/toast'

type Currency = 'USD' | 'EUR' | 'PLN' | 'GBP'
type RiskTolerance = 'LOW' | 'MODERATE' | 'HIGH' | 'AGGRESSIVE'
type InvestmentHorizon = 'SHORT_TERM' | 'MID_TERM' | 'LONG_TERM'

const CURRENCIES: { value: Currency; label: string }[] = [
  { value: 'USD', label: 'USD — Dolar amerykański' },
  { value: 'EUR', label: 'EUR — Euro' },
  { value: 'PLN', label: 'PLN — Złoty polski' },
  { value: 'GBP', label: 'GBP — Funt brytyjski' },
]

const RISK_OPTIONS: { value: RiskTolerance; label: string; desc: string }[] = [
  { value: 'LOW', label: 'Niska', desc: 'Konserwatywna strategia, minimalizacja ryzyka.' },
  { value: 'MODERATE', label: 'Umiarkowana', desc: 'Balans między zyskiem a ryzykiem.' },
  { value: 'HIGH', label: 'Wysoka', desc: 'Akceptacja dużych wahań dla wyższych zysków.' },
  { value: 'AGGRESSIVE', label: 'Agresywna', desc: 'Maksymalizacja potencjalnych zysków.' },
]

const HORIZON_OPTIONS: { value: InvestmentHorizon; label: string; desc: string }[] = [
  { value: 'SHORT_TERM', label: 'Krótkoterminowy', desc: 'Do 1 roku' },
  { value: 'MID_TERM', label: 'Średnioterminowy', desc: '1–5 lat' },
  { value: 'LONG_TERM', label: 'Długoterminowy', desc: 'Powyżej 5 lat' },
]

const PREFS_KEY = 'fin-insight-prefs'

interface Prefs {
  currency: Currency
  riskTolerance: RiskTolerance
  investmentHorizon: InvestmentHorizon
}

function loadPrefs(): Prefs {
  try {
    const raw = localStorage.getItem(PREFS_KEY)
    if (raw) return JSON.parse(raw) as Prefs
  } catch {
    // ignore
  }
  return { currency: 'USD', riskTolerance: 'MODERATE', investmentHorizon: 'MID_TERM' }
}

export function SettingsPage() {
  const { isDark, toggle } = useDarkMode()
  const { toast } = useToast()
  const [prefs, setPrefs] = useState<Prefs>(loadPrefs)

  const handleSavePrefs = () => {
    localStorage.setItem(PREFS_KEY, JSON.stringify(prefs))
    toast('Ustawienia zapisane.', 'success')
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Ustawienia</h1>
        <p className="text-muted-foreground">Zarządzaj preferencjami aplikacji i konta.</p>
      </div>

      {/* Appearance */}
      <Card>
        <CardHeader>
          <CardTitle>Wygląd</CardTitle>
          <CardDescription>Motyw kolorystyczny aplikacji.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <HugeiconsIcon
                icon={isDark ? Moon02Icon : Sun03Icon}
                className="size-5 text-muted-foreground"
                aria-hidden
              />
              <div>
                <p className="text-sm font-medium">{isDark ? 'Tryb ciemny' : 'Tryb jasny'}</p>
                <p className="text-xs text-muted-foreground">
                  {isDark
                    ? 'Zoptymalizowany dla słabego oświetlenia.'
                    : 'Klasyczny jasny motyw.'}
                </p>
              </div>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={isDark}
              onClick={toggle}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                isDark ? 'bg-foreground' : 'bg-muted'
              }`}
            >
              <span
                className={`inline-block size-4 rounded-full bg-background shadow transition-transform ${
                  isDark ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          </div>
        </CardContent>
      </Card>

      {/* Default currency */}
      <Card>
        <CardHeader>
          <CardTitle>Domyślna waluta</CardTitle>
          <CardDescription>Waluta używana do wyświetlania sum i wycen.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {CURRENCIES.map((c) => (
              <button
                key={c.value}
                type="button"
                onClick={() => setPrefs((p) => ({ ...p, currency: c.value }))}
                className={`rounded-lg border px-3 py-2.5 text-sm font-medium transition-colors ${
                  prefs.currency === c.value
                    ? 'border-ring bg-muted'
                    : 'border-border/50 text-muted-foreground hover:text-foreground'
                }`}
              >
                {c.value}
              </button>
            ))}
          </div>
          <p className="text-xs text-muted-foreground">
            {CURRENCIES.find((c) => c.value === prefs.currency)?.label}
          </p>
        </CardContent>
      </Card>

      {/* Risk tolerance */}
      <Card>
        <CardHeader>
          <CardTitle>Tolerancja ryzyka</CardTitle>
          <CardDescription>Określa strategię rekomendacji AI Doradcy.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2">
          {RISK_OPTIONS.map((r) => (
            <button
              key={r.value}
              type="button"
              onClick={() => setPrefs((p) => ({ ...p, riskTolerance: r.value }))}
              className={`w-full flex items-center justify-between rounded-lg border px-4 py-3 text-left transition-colors ${
                prefs.riskTolerance === r.value
                  ? 'border-ring bg-muted'
                  : 'border-border/40 hover:bg-muted/40'
              }`}
            >
              <div>
                <p className="text-sm font-medium">{r.label}</p>
                <p className="text-xs text-muted-foreground">{r.desc}</p>
              </div>
              {prefs.riskTolerance === r.value && (
                <span className="size-2 rounded-full bg-foreground" />
              )}
            </button>
          ))}
        </CardContent>
      </Card>

      {/* Investment horizon */}
      <Card>
        <CardHeader>
          <CardTitle>Horyzont inwestycyjny</CardTitle>
          <CardDescription>Planowany czas trzymania inwestycji.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2">
          {HORIZON_OPTIONS.map((h) => (
            <button
              key={h.value}
              type="button"
              onClick={() => setPrefs((p) => ({ ...p, investmentHorizon: h.value }))}
              className={`w-full flex items-center justify-between rounded-lg border px-4 py-3 text-left transition-colors ${
                prefs.investmentHorizon === h.value
                  ? 'border-ring bg-muted'
                  : 'border-border/40 hover:bg-muted/40'
              }`}
            >
              <div>
                <p className="text-sm font-medium">{h.label}</p>
                <p className="text-xs text-muted-foreground">{h.desc}</p>
              </div>
              {prefs.investmentHorizon === h.value && (
                <span className="size-2 rounded-full bg-foreground" />
              )}
            </button>
          ))}
        </CardContent>
      </Card>

      {/* Save */}
      <div className="flex justify-end">
        <Button onClick={handleSavePrefs}>Zapisz ustawienia</Button>
      </div>
    </div>
  )
}
