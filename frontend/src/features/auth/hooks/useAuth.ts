import { useCallback } from 'react'

import { loginDemo } from '@/features/auth/api'
import { useAppDispatch, useAppSelector } from '@/hooks/store'
import { setAuth, logout as logoutAction } from '@/store/slices/authSlice'

export function useAuth() {
  const token = useAppSelector((state) => state.auth.token)
  const dispatch = useAppDispatch()

  const login = useCallback(async (): Promise<void> => {
    try {
      const result = await loginDemo()
      dispatch(setAuth({ token: result.accessToken, refreshToken: result.refreshToken }))
    } catch {
      dispatch(logoutAction())
      throw new Error('Login failed. Please try again.')
    }
  }, [dispatch])

  const logout = useCallback(() => {
    dispatch(logoutAction())
  }, [dispatch])

  return { token, login, logout }
}
