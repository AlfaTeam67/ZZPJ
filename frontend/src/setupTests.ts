import '@testing-library/jest-dom'
import { vi, beforeAll, afterEach, afterAll } from 'vitest'
import { server } from '@/test/mocks/server'

// 1. Mockowanie Keycloak (Zabezpieczenia)
vi.mock('keycloak-js', () => ({
  default: vi.fn(() => ({
    init: vi.fn().mockResolvedValue(true),
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    accountManagement: vi.fn(),
    hasRealmRole: vi.fn().mockReturnValue(true),
    hasClientRole: vi.fn().mockReturnValue(true),
    isTokenExpired: vi.fn().mockReturnValue(false),
    updateToken: vi.fn().mockResolvedValue(true),
    clearToken: vi.fn(),
    loadUserProfile: vi.fn().mockResolvedValue({
      id: 'test-user-id',
      username: 'testuser',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    }),
    token: 'mock-jwt-token',
    refreshToken: 'mock-refresh-token',
    idToken: 'mock-id-token',
    authenticated: true,
    realm: 'fin-insight',
    clientId: 'fin-insight-client',
  })),
}))

// 2. Mockowanie Axios (Klient HTTP)
vi.mock('axios', () => {
  const axiosMock = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
    create: vi.fn(),
    interceptors: {
      request: {
        use: vi.fn(),
        eject: vi.fn(),
      },
      response: {
        use: vi.fn(),
        eject: vi.fn(),
      },
    },
  }

  axiosMock.create = vi.fn(() => axiosMock)
  return {
    default: axiosMock,
  }
})

// 3. Mockowanie API przeglądarkowych (MatchMedia i IntersectionObserver)
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// POPRAWIONE: window zamiast global, aby zapobiec błędowi ts(2304)
Object.defineProperty(window, 'IntersectionObserver', {
  writable: true,
  value: vi.fn().mockImplementation(() => ({
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  })),
})

// 4. Ograniczanie zbędnych warningów w logach konsoli
const originalError = console.error
beforeAll(() => {
  console.error = (...args: unknown[]) => {
    if (typeof args[0] === 'string' && args[0].includes('Warning: ReactDOM.render')) {
      return
    }
    originalError.call(console, ...args)
  }
})

afterAll(() => {
  console.error = originalError
})

// 5. Globalne uruchomienie serwera atrap sieciowych MSW
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'bypass' })
})

afterEach(() => {
  server.resetHandlers()
})

afterAll(() => {
  server.close()
})
