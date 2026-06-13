import { HugeiconsIcon } from '@hugeicons/react'
import {
  Moon02Icon,
  Notification03Icon,
  Search01Icon,
  Sun03Icon,
  UserIcon,
} from '@hugeicons/core-free-icons'
import { useLocation, Link } from 'react-router-dom'

import { useDarkMode } from '@/hooks/useDarkMode'

const TITLES: Record<string, string> = {
  '/': 'Kokpit',
  '/portfolio': 'Portfel',
  '/market': 'Rynek',
  '/advisor': 'Doradca AI',
  '/settings': 'Ustawienia',
  '/transactions': 'Transakcje',
  '/profile': 'Profil',
}

export function AppHeader() {
  const location = useLocation()
  const title = TITLES[location.pathname] ?? 'Fin-Insight'
  const { isDark, toggle } = useDarkMode()

  return (
    <header className="flex items-center justify-between border-b border-border/40 px-4 py-4 md:px-8 md:py-5">
      <h1 className="text-base font-semibold tracking-tight">{title}</h1>

      <div className="flex items-center gap-2">
        {/* Search — hidden on mobile */}
        <label className="relative hidden md:block">
          <span className="sr-only">Szukaj</span>
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
            <HugeiconsIcon icon={Search01Icon} className="size-4" aria-hidden />
          </span>
          <input
            type="search"
            placeholder="Szukaj akcji…"
            className="h-9 w-56 rounded-full border border-border/40 bg-muted/30 pl-9 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/30"
          />
        </label>

        {/* Dark mode toggle */}
        <button
          type="button"
          onClick={toggle}
          className="flex size-9 items-center justify-center rounded-full border border-border/40 bg-muted/30 text-muted-foreground transition-colors hover:text-foreground"
          aria-label={isDark ? 'Przełącz na tryb jasny' : 'Przełącz na tryb ciemny'}
        >
          <HugeiconsIcon
            icon={isDark ? Sun03Icon : Moon02Icon}
            className="size-4"
            aria-hidden
          />
        </button>

        {/* Notifications */}
        <button
          type="button"
          className="flex size-9 items-center justify-center rounded-full border border-border/40 bg-muted/30 text-muted-foreground transition-colors hover:text-foreground"
          aria-label="Powiadomienia"
        >
          <HugeiconsIcon icon={Notification03Icon} className="size-4" aria-hidden />
        </button>

        {/* Profile link */}
        <Link
          to="/profile"
          className="flex size-9 items-center justify-center rounded-full border border-border/40 bg-muted/30 text-muted-foreground transition-colors hover:text-foreground"
          aria-label="Profil użytkownika"
        >
          <HugeiconsIcon icon={UserIcon} className="size-4" aria-hidden />
        </Link>
      </div>
    </header>
  )
}
