import { HugeiconsIcon } from '@hugeicons/react'
import {
  Crown02Icon,
  Logout03Icon,
  Mail01Icon,
  UserCircleIcon,
} from '@hugeicons/core-free-icons'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/hooks/useAuth'

export function ProfilePage() {
  const { user, logout } = useAuth()

  const displayName =
    user
      ? [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || 'Użytkownik'
      : 'Użytkownik'

  const initials = displayName
    .split(' ')
    .map((w) => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-3xl font-bold tracking-tight">Profil</h1>
        <p className="text-muted-foreground">Twoje dane konta i preferencje.</p>
      </div>

      {/* Avatar + basic info */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center gap-5">
            <div className="flex size-16 items-center justify-center rounded-2xl bg-brand-primary-300/90 text-2xl font-bold text-brand-neutral-900">
              {initials || <HugeiconsIcon icon={UserCircleIcon} className="size-8" aria-hidden />}
            </div>
            <div className="flex flex-col gap-1">
              <p className="text-xl font-semibold">{displayName}</p>
              {user?.email && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <HugeiconsIcon icon={Mail01Icon} className="size-4" aria-hidden />
                  {user.email}
                </div>
              )}
              {user?.roles && user.roles.length > 0 && (
                <div className="flex flex-wrap gap-1 mt-1">
                  {user.roles.map((role) => (
                    <Badge key={role} variant="secondary" className="text-xs">
                      {role}
                    </Badge>
                  ))}
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Account details */}
      <Card>
        <CardHeader>
          <CardTitle>Dane konta</CardTitle>
          <CardDescription>Informacje z serwisu Keycloak.</CardDescription>
        </CardHeader>
        <CardContent>
          <dl className="divide-y divide-border/30">
            {[
              { label: 'ID użytkownika', value: user?.id },
              { label: 'Nazwa użytkownika', value: user?.username },
              { label: 'Imię', value: user?.firstName },
              { label: 'Nazwisko', value: user?.lastName },
              { label: 'E-mail', value: user?.email },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center justify-between py-3">
                <dt className="text-sm text-muted-foreground">{label}</dt>
                <dd className="text-sm font-medium">{value ?? '—'}</dd>
              </div>
            ))}
          </dl>
        </CardContent>
      </Card>

      {/* Subscription */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <HugeiconsIcon icon={Crown02Icon} className="size-5 text-brand-primary-300" aria-hidden />
            Plan konta
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-semibold">Fin-Insight Premium</p>
              <p className="text-sm text-muted-foreground">
                Pełny dostęp do wszystkich funkcji.
              </p>
            </div>
            <Badge className="bg-brand-primary-300/20 text-brand-primary-300 border-brand-primary-300/30">
              Aktywny
            </Badge>
          </div>
        </CardContent>
      </Card>

      {/* Logout */}
      <div className="flex justify-end">
        <Button
          variant="destructive"
          onClick={() => void logout()}
          className="flex items-center gap-2"
        >
          <HugeiconsIcon icon={Logout03Icon} className="size-4" aria-hidden />
          Wyloguj się
        </Button>
      </div>
    </div>
  )
}
