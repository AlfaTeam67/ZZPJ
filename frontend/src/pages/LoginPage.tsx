import { HugeiconsIcon } from '@hugeicons/react'
import {
  ChartLineData02Icon,
  Key01Icon,
  LinkSquare02Icon,
  SecurityCheckIcon,
} from '@hugeicons/core-free-icons'
import { useEffect, useMemo, useState } from 'react'
import { Navigate, useLocation, useSearchParams } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { useKeycloak } from '@/features/auth/hooks/useKeycloak'

const APP_VERSION = '0.1.0'

export function LoginPage() {
  const { initialized, initError, isAuthenticated, login } = useAuth()
  const keycloak = useKeycloak()
  const [params] = useSearchParams()
  const location = useLocation()
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const redirectTo = useMemo(() => params.get('from') ?? '/', [params])

  useEffect(() => {
    document.documentElement.classList.add('dark')
  }, [])

  if (initialized && isAuthenticated) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />
  }

  const handleLogin = async () => {
    if (!keycloak.isConfigured) {
      setError('Keycloak nie jest skonfigurowany. Ustaw VITE_KEYCLOAK_* w pliku .env.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await login(redirectTo)
    } catch (err) {
      setSubmitting(false)
      setError(err instanceof Error ? err.message : 'Nie udało się rozpocząć logowania.')
    }
  }

  return (
    <div className="dark relative min-h-screen overflow-hidden bg-brand-neutral-950 text-foreground">
      {/* tło - subtelny gradient + winieta jak na designie */}
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_center,_oklch(0.22_0.012_70/0.55)_0%,_transparent_60%)]"
      />
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 -z-10 [background:radial-gradient(circle_at_50%_50%,_transparent_0,_rgba(0,0,0,0.65)_100%)]"
      />

      <div className="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col px-6 py-8">
        <header className="flex items-center justify-between">
          <span className="text-xs uppercase tracking-[0.3em] text-muted-foreground">
            AlfaTeam · ZZPJ 2025/2026
          </span>
          <span className="text-xs text-muted-foreground">
            v{APP_VERSION} · realm <code className="text-foreground/70">{keycloak.realm}</code>
          </span>
        </header>

        <main className="flex flex-1 items-center justify-center">
          <section
            className="w-full max-w-md rounded-2xl border border-border/40 bg-card/85 p-10 shadow-[0_30px_120px_-40px_rgba(0,0,0,0.85)] backdrop-blur"
            aria-labelledby="login-title"
          >
            <div className="flex flex-col items-center text-center">
              <div className="mb-5 flex size-12 items-center justify-center rounded-xl bg-muted">
                <HugeiconsIcon
                  icon={ChartLineData02Icon}
                  className="size-6 text-primary"
                  aria-hidden
                />
              </div>
              <h1 id="login-title" className="text-3xl font-semibold tracking-tight">
                Fin-Insight
              </h1>
              <p className="mt-2 text-sm text-muted-foreground">
                Twój osobisty asystent inwestycyjny
              </p>
            </div>

            <div className="mt-8 space-y-3">
              <Button
                size="lg"
                className="w-full justify-center text-sm"
                onClick={() => void handleLogin()}
                disabled={submitting}
              >
                <HugeiconsIcon icon={Key01Icon} className="size-4" aria-hidden />
                {submitting
                  ? 'Przekierowywanie…'
                  : !initialized
                    ? 'Łączę z Keycloakiem…'
                    : 'Zaloguj się przez Keycloak'}
              </Button>

              {error ? (
                <p
                  role="alert"
                  className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-xs text-destructive"
                >
                  {error}
                </p>
              ) : null}

              {initialized && initError ? (
                <p className="rounded-md border border-border/40 bg-muted/30 px-3 py-2 text-xs text-muted-foreground">
                  Nie udało się połączyć z Keycloakiem ({keycloak.url}). Uruchom backend
                  poleceniem <code className="text-foreground/80">docker compose up -d keycloak</code>
                  {' '}i odśwież stronę.
                </p>
              ) : null}

              {!keycloak.isConfigured ? (
                <p className="text-xs text-muted-foreground">
                  Brak konfiguracji Keycloaka - sprawdź zmienne środowiskowe.
                </p>
              ) : null}
            </div>

            <div className="mt-8 border-t border-border/40 pt-6 text-center">
              <p className="flex items-center justify-center gap-2 text-[10px] uppercase tracking-[0.25em] text-muted-foreground">
                <HugeiconsIcon icon={SecurityCheckIcon} className="size-3" aria-hidden />
                Szyfrowane połączenie TLS 1.3
              </p>
              <div className="mt-4 flex items-center justify-center gap-6 text-xs">
                <a
                  href="https://fin-insight.dev/privacy"
                  target="_blank"
                  rel="noreferrer"
                  className="text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
                >
                  Polityka prywatności
                </a>
                <a
                  href="https://fin-insight.dev/tos"
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1 text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
                >
                  Warunki użytkowania
                  <HugeiconsIcon icon={LinkSquare02Icon} className="size-3" aria-hidden />
                </a>
              </div>
            </div>
          </section>
        </main>

        <footer className="flex items-center justify-between text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
          <span className="opacity-60">fin-insight</span>
          <span className="opacity-60">ver. {APP_VERSION} · oslona danych</span>
        </footer>
      </div>
    </div>
  )
}
