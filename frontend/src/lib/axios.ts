import axios, { type AxiosError } from 'axios'
import { env } from '@/lib/env'

const baseURL = import.meta.env.DEV ? '' : env.apiUrl

export const apiClient = axios.create({
  baseURL,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

// Separate instance for refreshing tokens to avoid interceptor recursion
const refreshClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
})

export function setupAxiosInterceptors(
  getToken: () => string | null,
  getRefreshToken: () => string | null,
  onTokenRefreshed: (tokens: { accessToken: string; refreshToken: string }) => void,
  onLogout: () => void
) {
  // Request Interceptor
  apiClient.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  // Response Interceptor for handling 401s
  let isRefreshing = false
  let failedQueue: { resolve: (value: unknown) => void; reject: (reason: unknown) => void }[] = []

  const processQueue = (error: unknown, token: string | null = null) => {
    failedQueue.forEach((prom) => {
      if (error) {
        prom.reject(error)
      } else {
        prom.resolve(token)
      }
    })
    failedQueue = []
  }

  apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as AxiosError['config'] & { _retry?: boolean }

      // If error is 401 and we haven't retried yet
      if (error.response?.status === 401 && !originalRequest._retry) {
        if (isRefreshing) {
          return new Promise(function (resolve, reject) {
            failedQueue.push({ resolve, reject })
          })
            .then((token) => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              return apiClient(originalRequest)
            })
            .catch((err) => {
              return Promise.reject(err)
            })
        }

        originalRequest._retry = true
        isRefreshing = true

        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          onLogout()
          return Promise.reject(error)
        }

        try {
          const params = new URLSearchParams()
          params.append('grant_type', 'refresh_token')
          params.append('client_id', 'fin-insight-client')
          params.append('refresh_token', refreshToken)

          const response = await refreshClient.post(
            '/realms/fin-insight/protocol/openid-connect/token',
            params
          )

          const { access_token, refresh_token } = response.data
          onTokenRefreshed({ accessToken: access_token, refreshToken: refresh_token })

          processQueue(null, access_token)
          originalRequest.headers.Authorization = `Bearer ${access_token}`
          return apiClient(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          onLogout()
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      return Promise.reject(error)
    }
  )
}
