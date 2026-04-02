import axios from 'axios'
import { env } from '@/lib/env'
import { store } from '@/store/store'

export const apiClient = axios.create({
  baseURL: env.apiUrl,
})

apiClient.interceptors.request.use((config) => {
  const token = store.getState().auth.token

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})
