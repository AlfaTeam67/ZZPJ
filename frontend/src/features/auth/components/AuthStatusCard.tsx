import { useState } from 'react'
import { NavLink, useLocation, useNavigate } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { useKeycloak } from '@/features/auth/hooks/useKeycloak'
import { cn } from '@/lib/utils'

const navigation = [
  { to: '/', label: 'Dashboard' },
  { to: '/portfolio', label: 'Portfolio' },
  { to: '/advisor', label: 'AI Advisor' },
]

export function AuthStatusCard() {
  const { token, login } = useAuth()
  const keycloak = useKeycloak()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const navigate = useNavigate()
  const location = useLocation()

  const handleLoginClick = async () => {
    try {
      await login()
      setErrorMessage(null)
      // Redirect back to the page user was trying to access
      const from = (location.state as any)?.from?.pathname || '/'
      navigate(from, { replace: true })
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Login failed.')
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Auth status</CardTitle>
        <CardDescription>{keycloak.reason}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-sm">Status: {token ? 'authenticated' : 'not authenticated'}</p>
        
        {token && (
          <div className="space-y-2 pt-2">
            <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Go to section:
            </p>
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
              {navigation.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className="inline-flex h-9 items-center justify-center rounded-md border border-input bg-background px-4 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground"
                >
                  {item.label}
                </NavLink>
              ))}
            </div>
          </div>
        )}

        {errorMessage ? <p className="text-sm text-destructive">{errorMessage}</p> : null}
        
        <Button type="button" onClick={() => void handleLoginClick()} className="w-full">
          Demo login (Refresh Token)
        </Button>
      </CardContent>
    </Card>
  )
}
