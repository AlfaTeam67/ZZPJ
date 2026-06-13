import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

import App from './App'
import './index.css'
import { AuthProvider } from '@/features/auth/AuthProvider'
import { setupAxiosInterceptors } from '@/lib/axios'
import { queryClient } from '@/lib/queryClient'
import { store } from '@/store/store'
import { ToastProvider } from '@/components/ui/toast'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error("Root element '#root' was not found.")
}

setupAxiosInterceptors(() => store.getState().auth.token)

// Expose store to window in dev for easy token injection during local testing
if (import.meta.env.DEV) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ;(window as any).store = store
}

createRoot(rootElement).render(
  <StrictMode>
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthProvider>
              <ToastProvider>
                <App />
              </ToastProvider>
            </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </Provider>
  </StrictMode>
)
