import { useEffect, useState } from 'react'

export function useEventSource<T>(url: string): T[] | undefined {
  const [data, setData] = useState<T[] | undefined>(undefined)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const es = new EventSource(url)
    es.onopen = () => setConnected(true)
    es.onmessage = (event) => {
      try {
        setData(JSON.parse(event.data) as T[])
      } catch {
        // ignore malformed events
      }
    }
    // don't close — EventSource auto-retries per spec; just mark as disconnected
    es.onerror = () => setConnected(false)
    return () => {
      es.close()
      setConnected(false)
    }
  }, [url])

  return connected ? data : undefined
}
