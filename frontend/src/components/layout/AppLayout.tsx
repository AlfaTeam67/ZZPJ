import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'

import { AppHeader } from '@/components/layout/AppHeader'
import { AppSidebar } from '@/components/layout/AppSidebar'
import { useDarkMode } from '@/hooks/useDarkMode'

export function AppLayout() {
  // Inicjalizuj dark mode z localStorage (hook zarządza klasą na <html>)
  useDarkMode()

  return (
    <div className="bg-background text-foreground">
      {/* Mobile: single column; Desktop: sidebar + content */}
      <div className="flex min-h-screen flex-col md:grid md:grid-cols-[240px_1fr]">
        {/* Sidebar — ukryty na mobile, widoczny na md+ */}
        <div className="hidden md:flex md:flex-col">
          <AppSidebar />
        </div>
        <div className="flex min-h-screen flex-col">
          <AppHeader />
          <main className="flex-1 px-4 py-6 md:px-8 md:py-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}
