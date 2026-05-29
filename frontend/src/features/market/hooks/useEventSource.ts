import { useEffect, useState } from 'react'

interface State<T> {
  url: string
  data?: T[]
  connected: boolean
}

export function useEventSource<T>(url: string): T[] | undefined {
  const [state, setState] = useState<State<T>>({ url, connected: false })

  useEffect(() => {
    const es = new EventSource(url)
    es.onopen = () => setState({ url, connected: true })
    es.onmessage = (event) => {
      try {
        setState({ url, data: JSON.parse(event.data) as T[], connected: true })
      } catch {
        // ignore malformed events
      }
    }
    // don't close on error — EventSource auto-retries per spec; just mark as disconnected
    es.onerror = () => setState((s) => ({ ...s, connected: false }))
    return () => es.close()
  }, [url])

  // Only expose data tied to the current url, preventing stale values after a url change
  return state.url === url && state.connected ? state.data : undefined
}
