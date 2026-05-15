import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '@/features/auth/hooks/useAuth'

/**
 * Guard w stylu react-router. Przed inicjalizacją Keycloaka pokazuje pełnoekranowy
 * loader, żeby uniknąć flasha "Login" przy pierwszym renderze strony chronionej.
 */
export function RequireAuth() {
  const { initialized, isAuthenticated } = useAuth()
  const location = useLocation()

  if (!initialized) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-muted-foreground">
        <div className="size-10 animate-spin rounded-full border-2 border-muted border-t-primary" />
      </div>
    )
  }

  if (!isAuthenticated) {
    const search = new URLSearchParams({ from: location.pathname }).toString()
    return <Navigate to={`/login?${search}`} replace />
  }

  return <Outlet />
}
