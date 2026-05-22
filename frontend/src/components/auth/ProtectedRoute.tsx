import { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAppSelector } from '@/hooks/store'

interface ProtectedRouteProps {
  children: ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = useAppSelector((state) => state.auth.token)
  const location = useLocation()

  if (!token) {
    // Redirect to auth page, but save the current location they were trying to go to
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <>{children}</>
}
