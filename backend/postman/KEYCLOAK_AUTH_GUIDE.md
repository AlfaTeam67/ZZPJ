# Keycloak Authentication Guide dla Postman

## Problem: Otrzymujesz 401 Unauthorized

Twoje API używa **Keycloak OAuth2/OpenID Connect** do autentykacji. Nie ma prostego endpointu `/login` - musisz uzyskać token JWT z Keycloaka.

## ⚠️ WAŻNE: localhost vs keycloak hostname

Aktualna konfiguracja akceptuje tokeny uzyskane zarówno z `http://localhost:8080`, jak i `http://keycloak:8080`.  
Dodanie `127.0.0.1 keycloak` do `/etc/hosts` pozwala wygodnie używać obu URL.

```bash
# Dodaj do /etc/hosts (na Mac/Linux):
echo "127.0.0.1 keycloak" | sudo tee -a /etc/hosts
```

## Rozwiązanie: Uzyskaj token z Keycloaka

### Metoda 1: Direct Access Grant (Password Grant) - POLECANA dla Postman

#### Krok 1: Upewnij się że Keycloak działa
```bash
cd backend
docker-compose up keycloak keycloak-db
```

Poczekaj aż Keycloak będzie dostępny na http://localhost:8080

#### Krok 2: Uzyskaj token przez Postman

**WAŻNE: Możesz użyć `http://localhost:8080` albo `http://keycloak:8080` (po dodaniu wpisu do /etc/hosts).**

**Request:**
```
POST http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

Body (x-www-form-urlencoded):
- client_id: fin-insight-client
- username: admin
- password: admin
- grant_type: password
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer"
}
```

#### Krok 3: Użyj tokena w requestach do API

Dodaj header do każdego requesta:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI...
```

### Metoda 2: Service Account (Client Credentials) - dla automatyzacji

**Request:**
```
POST http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

Body (x-www-form-urlencoded):
- client_id: market-data-postman
- client_secret: postman-secret
- grant_type: client_credentials
```

## Konfiguracja w Postman Collections

### Opcja A: Authorization na poziomie Collection

1. Otwórz swoją Collection w Postman
2. Kliknij zakładkę "Authorization"
3. Type: **OAuth 2.0**
4. Add auth data to: **Request Headers**

**Configuration Details:**
- **Token Name:** fin-insight-token
- **Grant Type:** Password Credentials
- **Access Token URL:** `http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token`
- **Client ID:** `fin-insight-client`
- **Username:** `admin`
- **Password:** `admin`
- **Scope:** (zostaw puste lub `openid profile email`)
- **Client Authentication:** Send as Basic Auth header

5. Kliknij **Get New Access Token**
6. Kliknij **Use Token**

Teraz wszystkie requesty w tej collection będą automatycznie używać tego tokena!

### Opcja B: Manual Request w Postman

Jeśli wolisz robić to ręcznie:

1. **Stwórz request do uzyskania tokena:**
   - Method: POST
   - URL: `http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token`
   - Headers: `Content-Type: application/x-www-form-urlencoded`
   - Body (x-www-form-urlencoded):
     ```
     client_id=fin-insight-client
     username=admin
     password=admin
     grant_type=password
     ```

2. **Wyślij request i skopiuj `access_token` z response**

3. **W requestach do API dodaj header:**
   ```
   Authorization: Bearer <WKLEJ_TOKEN_TUTAJ>
   ```

## Przykładowe requesty do API

### Test Health Endpoint (bez autentykacji)
```
GET http://localhost:8081/actuator/health
```

### Test Portfolio API (z autentykacją)
```
GET http://localhost:8081/api/portfolios
Authorization: Bearer <TOKEN>
```

### Test Market Data API (z autentykacją)
```
GET http://localhost:8082/api/symbols
Authorization: Bearer <TOKEN>
```

## Troubleshooting

### 401 Unauthorized
- **Przyczyna:** Brak tokena, token wygasł, lub token ma zły issuer
- **Rozwiązanie:** 
  1. Sprawdź czy token jest w headerze `Authorization: Bearer <token>`
  2. Sprawdź czy używasz poprawnego URL Keycloak (`localhost` lub `keycloak`) i że endpoint tokena odpowiada
  3. Dodaj `127.0.0.1 keycloak` do `/etc/hosts`
  4. Uzyskaj nowy token (ważny tylko 5 minut)

### Token expired
- **Przyczyna:** Token jest ważny tylko 5 minut
- **Rozwiązanie:** Użyj refresh_token lub uzyskaj nowy access_token

### Connection refused na localhost:8080 lub keycloak:8080
- **Przyczyna:** Keycloak nie działa
- **Rozwiązanie:** `docker-compose up keycloak keycloak-db`

### "Unable to resolve Configuration" w logach serwisu
- **Przyczyna:** Serwis działa na starym obrazie bez aktualnej konfiguracji JWT
- **Rozwiązanie:** Przebuduj i uruchom serwisy ponownie: `docker compose up -d --build`

## Użytkownicy testowi

Dostępni użytkownicy w realm `fin-insight`:

| Username | Password | Opis | Role |
|----------|----------|------|------|
| admin    | admin    | Administrator testowy | admin, user |
| testuser | test123  | Użytkownik testowy | user |

## Dostępne clients

| Client ID              | Type   | Secret         | Użycie |
|------------------------|--------|----------------|--------|
| fin-insight-client     | Public | -              | Frontend + Postman (Password Grant) |
| market-data-postman    | Confidential | postman-secret | Service Account (Client Credentials) |

## Więcej informacji

- Keycloak Admin Console: http://localhost:8080/admin lub http://keycloak:8080/admin
  - Username: admin
  - Password: admin (lub wartość z KEYCLOAK_ADMIN_PASSWORD w .env)

- Token introspection endpoint: 
  ```
  POST http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token/introspect
  ```

## Szybki test z curl

```bash
# Dodaj keycloak do /etc/hosts
echo "127.0.0.1 keycloak" | sudo tee -a /etc/hosts

# Uzyskaj token
TOKEN=$(curl -s -X POST 'http://keycloak:8080/realms/fin-insight/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fin-insight-client&username=admin&password=admin&grant_type=password' \
  | jq -r '.access_token')

# Użyj tokena
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/portfolios
```
