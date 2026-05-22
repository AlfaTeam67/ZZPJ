import { useEffect, useRef } from 'react'

import { getKeycloak } from '@/features/auth/keycloak'
import { useAppDispatch } from '@/hooks/store'
import {
  authReady,
  authInitFailed,
  signedOut,
  tokenRefreshed,
  type AuthUser,
} from '@/store/slices/authSlice'

/**
 * Inicjalizuje Keycloak w trybie check-sso (bez wymuszania od razu logowania)
 * i pilnuje, by token w storze nie wygasł. Na zewnątrz nie renderuje nic widocznego.
 *
 * Strategia:
 *   1. init({ onLoad: 'check-sso' }) - jeśli mamy aktywną sesję na Keycloaku, podnosi token cicho
 *   2. ustawia onTokenExpired -> updateToken
 *   3. setInterval co 30s odświeża token jeżeli wygasa w ciągu 60s
 *   4. czyści wszystko przy unmount
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const dispatch = useAppDispatch()
  const initStartedRef = useRef(false)

  useEffect(() => {
    if (initStartedRef.current) {
      return
    }
    initStartedRef.current = true

    const keycloak = getKeycloak()
    let refreshTimer: number | undefined

    const buildUser = (): AuthUser | null => {
      const parsed = keycloak.tokenParsed as
        | {
            sub?: string
            preferred_username?: string
            email?: string
            given_name?: string
            family_name?: string
            realm_access?: { roles?: string[] }
          }
        | undefined
      if (!parsed?.sub) {
        return null
      }
      return {
        id: parsed.sub,
        username: parsed.preferred_username,
        email: parsed.email,
        firstName: parsed.given_name,
        lastName: parsed.family_name,
        roles: parsed.realm_access?.roles ?? [],
      }
    }

    const publishSession = () => {
      const token = keycloak.token
      const user = buildUser()
      if (token && user) {
        dispatch(authReady({ token, user }))
      } else {
        dispatch(authReady(null))
      }
    }

    keycloak.onTokenExpired = () => {
      keycloak
        .updateToken(60)
        .then(() => {
          if (keycloak.token) {
            dispatch(tokenRefreshed(keycloak.token))
          }
        })
        .catch(() => {
          dispatch(signedOut())
          void keycloak.login()
        })
    }

    // Init bez silent-SSO - korzystamy z prostego flow:
    // 1) niezalogowany user widzi LoginPage,
    // 2) klik wywoluje keycloak.login() => pelny redirect na Keycloak,
    // 3) po powrocie keycloak-js sam parsuje fragment URL i podnosi sesje.
    // Silent-SSO przez iframe wymagaloby dodatkowych redirectUris, ktore latwo
    // sie rozjezdzaja z konfiguracja realmu, wiec swiadomie z niego rezygnujemy.
    const initTimeoutMs = 15_000
    let timeoutHandle: number | undefined
    const timeoutPromise = new Promise<never>((_, reject) => {
      timeoutHandle = window.setTimeout(
        () => reject(new Error('Keycloak init timed out')),
        initTimeoutMs
      )
    })

    Promise.race([
      keycloak.init({
        pkceMethod: 'S256',
        checkLoginIframe: false,
      }),
      timeoutPromise,
    ])
      .then((authenticated) => {
        if (timeoutHandle) {
          window.clearTimeout(timeoutHandle)
        }
        publishSession()
        if (authenticated) {
          // Refresh proactively so axios interceptors always send a valid token.
          refreshTimer = window.setInterval(() => {
            keycloak
              .updateToken(60)
              .then((refreshed) => {
                if (refreshed && keycloak.token) {
                  dispatch(tokenRefreshed(keycloak.token))
                }
              })
              .catch(() => {
                dispatch(signedOut())
              })
          }, 30_000)
        }
      })
      .catch((error) => {
        if (timeoutHandle) {
          window.clearTimeout(timeoutHandle)
        }
        // Keycloak unreachable - traktujemy jak brak sesji, nie blokujemy aplikacji
        const message = error instanceof Error ? error.message : 'Keycloak init failed'
        console.warn('Keycloak init failed', error)
        dispatch(authInitFailed(message))
      })

    return () => {
      if (refreshTimer) {
        window.clearInterval(refreshTimer)
      }
    }
  }, [dispatch])

  return <>{children}</>
}
