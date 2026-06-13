import {
  createContext,
  useCallback,
  useContext,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { createPortal } from 'react-dom'
import { cn } from '@/lib/utils'

export type ToastVariant = 'default' | 'success' | 'error' | 'warning'

export interface Toast {
  id: string
  message: string
  variant: ToastVariant
}

interface ToastContextValue {
  toast: (message: string, variant?: ToastVariant) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

let counter = 0

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const timers = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map())

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
    const timer = timers.current.get(id)
    if (timer) {
      clearTimeout(timer)
      timers.current.delete(id)
    }
  }, [])

  const toast = useCallback(
    (message: string, variant: ToastVariant = 'default') => {
      const id = `toast-${++counter}`
      setToasts((prev) => [...prev.slice(-4), { id, message, variant }])
      const timer = setTimeout(() => dismiss(id), 4000)
      timers.current.set(id, timer)
    },
    [dismiss]
  )

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      {createPortal(
        <div
          role="region"
          aria-live="polite"
          aria-label="Powiadomienia"
          className="pointer-events-none fixed bottom-6 right-6 z-50 flex flex-col-reverse gap-2"
        >
          {toasts.map((t) => (
            <div
              key={t.id}
              className={cn(
                'pointer-events-auto flex items-start gap-3 rounded-xl border px-4 py-3 shadow-lg text-sm max-w-sm',
                t.variant === 'success' && 'border-success/30 bg-success/10 text-success',
                t.variant === 'error' && 'border-destructive/30 bg-destructive/10 text-destructive',
                t.variant === 'warning' && 'border-yellow-500/30 bg-yellow-500/10 text-yellow-600',
                t.variant === 'default' && 'border-border/50 bg-card text-foreground'
              )}
            >
              <span className="flex-1 leading-relaxed">{t.message}</span>
              <button
                type="button"
                onClick={() => dismiss(t.id)}
                className="shrink-0 text-current opacity-60 hover:opacity-100 transition-opacity"
                aria-label="Zamknij"
              >
                ✕
              </button>
            </div>
          ))}
        </div>,
        document.body
      )}
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}
