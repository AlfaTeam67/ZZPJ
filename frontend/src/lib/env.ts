function readEnv(name: string): string {
  const value = import.meta.env[name as keyof ImportMetaEnv]
  if (!value || typeof value !== 'string') {
    throw new Error(`Missing ${name} environment variable.`)
  }
  return value
}

function readEnvOptional(name: string, fallback: string): string {
  const value = import.meta.env[name as keyof ImportMetaEnv]
  return typeof value === 'string' && value.length > 0 ? value : fallback
}

const isDev = import.meta.env.DEV
const apiUrl = readEnv('VITE_API_URL')

export const env = {
  apiUrl,
  portfolioApiUrl: isDev
    ? ''
    : readEnvOptional('VITE_PORTFOLIO_API_URL', apiUrl || 'http://localhost:8081'),
  marketApiUrl: isDev
    ? ''
    : readEnvOptional('VITE_MARKET_API_URL', apiUrl || 'http://localhost:8082'),
  advisorApiUrl: isDev
    ? ''
    : readEnvOptional('VITE_ADVISOR_API_URL', apiUrl || 'http://localhost:8083'),
  keycloak: {
    url: readEnvOptional('VITE_KEYCLOAK_URL', 'http://localhost:8080'),
    realm: readEnvOptional('VITE_KEYCLOAK_REALM', 'fin-insight'),
    clientId: readEnvOptional('VITE_KEYCLOAK_CLIENT_ID', 'fin-insight-client'),
  },
}
