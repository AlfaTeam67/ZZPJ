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

export const env = {
  apiUrl: readEnv('VITE_API_URL'),
  keycloak: {
    url: readEnvOptional('VITE_KEYCLOAK_URL', 'http://localhost:8080'),
    realm: readEnvOptional('VITE_KEYCLOAK_REALM', 'fin-insight'),
    clientId: readEnvOptional('VITE_KEYCLOAK_CLIENT_ID', 'fin-insight-client'),
  },
}
