import { HugeiconsIcon, type IconSvgElement } from '@hugeicons/react'
import {
  AiBrain02Icon,
  Briefcase01Icon,
  ChartLineData02Icon,
  Crown02Icon,
  DashboardSquare02Icon,
  Logout03Icon,
  Settings01Icon,
} from '@hugeicons/core-free-icons'
import { NavLink } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { cn } from '@/lib/utils'

interface NavItem {
  to: string
  label: string
  icon: IconSvgElement
}

const navigation: NavItem[] = [
  { to: '/', label: 'Kokpit', icon: DashboardSquare02Icon },
  { to: '/portfolio', label: 'Portfel', icon: Briefcase01Icon },
  { to: '/market', label: 'Rynek', icon: ChartLineData02Icon },
  { to: '/advisor', label: 'Doradca AI', icon: AiBrain02Icon },
]

export function AppSidebar() {
  const { user, logout } = useAuth()
  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') ||
      user.username ||
      user.email ||
      'Uzytkownik'
    : 'Goscinnie'

  return (
    <aside className="flex h-full flex-col gap-6 border-r border-border/40 bg-card/40 px-4 py-6">
      <div className="px-2">
        <span className="text-lg font-semibold tracking-tight">Fin-Insight</span>
      </div>

      <nav className="flex flex-1 flex-col gap-1">
        {navigation.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-muted text-foreground'
                  : 'text-muted-foreground hover:bg-muted/50 hover:text-foreground'
              )
            }
          >
            {({ isActive }) => (
              <>
                <HugeiconsIcon
                  icon={item.icon}
                  className={cn('size-4', isActive ? 'text-foreground' : 'text-muted-foreground')}
                  aria-hidden
                />
                {item.label}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="space-y-3 border-t border-border/30 pt-4">
        <NavLink
          to="/settings"
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
              isActive
                ? 'bg-muted text-foreground'
                : 'text-muted-foreground hover:bg-muted/50 hover:text-foreground'
            )
          }
        >
          <HugeiconsIcon icon={Settings01Icon} className="size-4" aria-hidden />
          Ustawienia
        </NavLink>

        <div className="flex items-center gap-3 rounded-lg bg-muted/40 px-3 py-3">
          <span className="flex size-8 items-center justify-center rounded-md bg-brand-primary-300/90 text-brand-neutral-900">
            <HugeiconsIcon icon={Crown02Icon} className="size-4" aria-hidden />
          </span>
          <div className="min-w-0 leading-tight">
            <p className="truncate text-sm font-semibold">Portfel Premium</p>
            <p className="truncate text-xs text-muted-foreground">{displayName}</p>
          </div>
        </div>

        <Button
          variant="ghost"
          size="sm"
          className="w-full justify-start text-muted-foreground"
          onClick={() => void logout()}
        >
          <HugeiconsIcon icon={Logout03Icon} className="size-4" aria-hidden />
          Wyloguj
        </Button>
      </div>
    </aside>
  )
}
