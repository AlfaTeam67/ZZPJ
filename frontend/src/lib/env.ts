const isDev = import.meta.env.DEV

const apiUrl = import.meta.env.VITE_API_URL || ''
const portfolioApiUrl = isDev ? '' : (import.meta.env.VITE_PORTFOLIO_API_URL || apiUrl || 'http://localhost:8081')
const marketApiUrl = isDev ? '' : (import.meta.env.VITE_MARKET_API_URL || apiUrl || 'http://localhost:8082')
const advisorApiUrl = isDev ? '' : (import.meta.env.VITE_ADVISOR_API_URL || apiUrl || 'http://localhost:8083')

export const env = {
  apiUrl,
  portfolioApiUrl,
  marketApiUrl,
  advisorApiUrl,
}
