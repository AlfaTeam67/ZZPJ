import Keycloak from 'keycloak-js'

import { env } from '@/lib/env'

/**
 * Singleton instancja Keycloak. Tworzymy ją raz na poziomie modułu - keycloak-js
 * trzyma w środku timery odświeżania tokenu i nie znosi wielokrotnej inicjalizacji
 * (StrictMode w devie wywołałby init dwa razy bez tej dodatkowej osłony).
 */
let instance: Keycloak | null = null

export function getKeycloak(): Keycloak {
  if (instance) {
    return instance
  }
  instance = new Keycloak({
    url: env.keycloak.url,
    realm: env.keycloak.realm,
    clientId: env.keycloak.clientId,
  })
  return instance
}
