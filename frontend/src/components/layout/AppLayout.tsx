import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'

import { AppHeader } from '@/components/layout/AppHeader'
import { AppSidebar } from '@/components/layout/AppSidebar'

export function AppLayout() {
  // Cala aplikacja (po zalogowaniu) leci w dark mode zgodnie z designem.
  useEffect(() => {
    document.documentElement.classList.add('dark')
  }, [])

  return (
    <div className="grid min-h-screen grid-cols-[240px_1fr] bg-background text-foreground">
      <AppSidebar />
      <div className="flex min-h-screen flex-col">
        <AppHeader />
        <main className="flex-1 px-8 py-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
