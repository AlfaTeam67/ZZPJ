import { useEffect, useState } from 'react'

const STORAGE_KEY = 'fin-insight-theme'

function getInitialDark(): boolean {
  if (typeof window === 'undefined') return true
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored !== null) return stored === 'dark'
  return true
}

// Shared theme state so every component using the hook stays in sync.
let globalIsDark = getInitialDark()
const listeners = new Set<(val: boolean) => void>()

function setGlobalDark(val: boolean) {
  globalIsDark = val
  const root = document.documentElement
  if (val) {
    root.classList.add('dark')
  } else {
    root.classList.remove('dark')
  }
  localStorage.setItem(STORAGE_KEY, val ? 'dark' : 'light')
  listeners.forEach((l) => l(val))
}

export function useDarkMode() {
  const [isDark, setIsDark] = useState<boolean>(globalIsDark)

  useEffect(() => {
    // Ensure the DOM reflects the current state on first mount.
    const root = document.documentElement
    if (globalIsDark) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }

    const listener = (val: boolean) => setIsDark(val)
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  }, [])

  const toggle = () => setGlobalDark(!globalIsDark)

  return { isDark, toggle }
}
