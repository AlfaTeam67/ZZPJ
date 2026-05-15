import { useCallback } from 'react'

import { getKeycloak } from '@/features/auth/keycloak'
import { useAppSelector } from '@/hooks/store'

/**
 * Cienki hook nad Reduxem + Keycloak singletonem.
 * Trzymamy stan w Reduxie żeby był spójny z resztą appki (axios interceptor itd.)
 * a same akcje (login/logout) delegujemy do keycloak-js.
 */
export function useAuth() {
  const { initialized, initError, token, user } = useAppSelector((state) => state.auth)

  const login = useCallback(async (redirectPath?: string) => {
    const keycloak = getKeycloak()
    const redirectUri =
      typeof window !== 'undefined' ? `${window.location.origin}${redirectPath ?? '/'}` : undefined
    await keycloak.login({ redirectUri })
  }, [])

  const logout = useCallback(async () => {
    const keycloak = getKeycloak()
    const redirectUri =
      typeof window !== 'undefined' ? `${window.location.origin}/login` : undefined
    await keycloak.logout({ redirectUri })
  }, [])

  return {
    initialized,
    initError,
    token,
    user,
    isAuthenticated: Boolean(token && user),
    login,
    logout,
  }
}
