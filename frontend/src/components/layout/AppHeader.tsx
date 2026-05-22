import { HugeiconsIcon } from '@hugeicons/react'
import { Notification03Icon, Search01Icon, UserIcon } from '@hugeicons/core-free-icons'
import { useLocation } from 'react-router-dom'

const TITLES: Record<string, string> = {
  '/': 'Kokpit',
  '/portfolio': 'Portfel',
  '/market': 'Rynek',
  '/advisor': 'Doradca AI',
  '/settings': 'Ustawienia',
}

export function AppHeader() {
  const location = useLocation()
  const title = TITLES[location.pathname] ?? 'Fin-Insight'

  return (
    <header className="flex items-center justify-between border-b border-border/40 px-8 py-5">
      <h1 className="text-base font-semibold tracking-tight">{title}</h1>

      <div className="flex items-center gap-3">
        <label className="relative hidden md:block">
          <span className="sr-only">Szukaj</span>
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
            <HugeiconsIcon icon={Search01Icon} className="size-4" aria-hidden />
          </span>
          <input
            type="search"
            placeholder="Szukaj akcji…"
            className="h-9 w-64 rounded-full border border-border/40 bg-muted/30 pl-9 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/30"
          />
        </label>
        <button
          type="button"
          className="flex size-9 items-center justify-center rounded-full border border-border/40 bg-muted/30 text-muted-foreground transition-colors hover:text-foreground"
          aria-label="Powiadomienia"
        >
          <HugeiconsIcon icon={Notification03Icon} className="size-4" aria-hidden />
        </button>
        <button
          type="button"
          className="flex size-9 items-center justify-center rounded-full border border-border/40 bg-muted/30 text-muted-foreground transition-colors hover:text-foreground"
          aria-label="Konto"
        >
          <HugeiconsIcon icon={UserIcon} className="size-4" aria-hidden />
        </button>
      </div>
    </header>
  )
}
