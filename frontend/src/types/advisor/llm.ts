export interface LlmProvider {
  id: number
  name: string      // np. "Google Gemini Flash", "Claude Opus"
  modelId: string   // np. "gemini-1.5-flash", "claude-3-opus"
  active: boolean
  priority?: number // wyższa liczba = wyższy priorytet
}

export function isLlmProvider(obj: unknown): obj is LlmProvider {
  if (obj === null || typeof obj !== 'object') return false
  const candidate = obj as Record<string, unknown>
  return (
    typeof candidate.id === 'number' &&
    typeof candidate.name === 'string' &&
    typeof candidate.modelId === 'string' &&
    typeof candidate.active === 'boolean'
  )
}
