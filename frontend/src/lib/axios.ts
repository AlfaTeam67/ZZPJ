import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

import { getKeycloak } from '@/features/auth/keycloak'
import { env } from '@/lib/env'

export const apiClient = axios.create({
  baseURL: env.apiUrl,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

interface RetryableConfig extends InternalAxiosRequestConfig {
  __retry?: boolean
}

export function setupAxiosInterceptors(getToken: () => string | null) {
  apiClient.interceptors.request.use(async (config) => {
    let token = getToken()

    // Proaktywnie odświeżamy, jeśli token wygasa w ciągu 30s. Bezpieczniejsze niż
    // czekanie na 401 - portfolio-manager i tak odrzuci wygasły token.
    const keycloak = getKeycloak()
    if (keycloak.token) {
      try {
        const refreshed = await keycloak.updateToken(30)
        if (refreshed && keycloak.token) {
          token = keycloak.token
        }
      } catch {
        // Refresh failed - i tak wyślijmy stary token; reactor 401 dopilnuje reszty.
      }
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const original = error.config as RetryableConfig | undefined
      if (!original || error.response?.status !== 401 || original.__retry) {
        return Promise.reject(error)
      }
      original.__retry = true
      try {
        const keycloak = getKeycloak()
        await keycloak.updateToken(0)
        if (keycloak.token) {
          original.headers.set('Authorization', `Bearer ${keycloak.token}`)
          return apiClient.request(original)
        }
      } catch {
        // przejdź do logout flow
      }
      const keycloak = getKeycloak()
      await keycloak.login()
      return Promise.reject(error)
    }
  )
}
