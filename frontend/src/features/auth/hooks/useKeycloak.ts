import { env } from '@/lib/env'

/**
 * Lekki hook informujący czy Keycloak jest skonfigurowany w env.
 * Używany przez UI do wyświetlenia komunikatu, gdy build został odpalony bez konfiguracji.
 */
export function useKeycloak() {
  const configured = Boolean(env.keycloak.url && env.keycloak.realm && env.keycloak.clientId)
  return {
    isConfigured: configured,
    realm: env.keycloak.realm,
    url: env.keycloak.url,
  }
}
