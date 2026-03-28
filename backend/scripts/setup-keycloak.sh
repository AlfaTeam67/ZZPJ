#!/bin/bash

# Fin-Insight - Keycloak Configuration Script
# This script automates Keycloak realm, client, roles, and users setup

set -e

KEYCLOAK_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin"
REALM_NAME="fin-insight"
CLIENT_ID="fin-insight-client"

echo "=================================="
echo "Keycloak Configuration for Fin-Insight"
echo "=================================="
echo ""

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready..."
until curl -sf "${KEYCLOAK_URL}/health/ready" > /dev/null; do
    echo -n "."
    sleep 2
done
echo ""
echo "✓ Keycloak is ready!"
echo ""

# Get admin token
echo "Authenticating as admin..."
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASSWORD}" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "✗ Failed to authenticate"
    exit 1
fi
echo "✓ Authenticated"
echo ""

# Create Realm
echo "Creating realm '${REALM_NAME}'..."
REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}")

if [ "$REALM_EXISTS" = "200" ]; then
    echo "  Realm already exists, skipping..."
else
    curl -s -X POST "${KEYCLOAK_URL}/admin/realms" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" \
      -d '{
        "realm": "'"${REALM_NAME}"'",
        "enabled": true,
        "displayName": "Fin-Insight",
        "accessTokenLifespan": 1800,
        "sslRequired": "none"
      }' > /dev/null
    echo "✓ Realm created"
fi
echo ""

# Create Client
echo "Creating client '${CLIENT_ID}'..."
CLIENT_UUID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r ".[] | select(.clientId==\"${CLIENT_ID}\") | .id")

if [ -n "$CLIENT_UUID" ] && [ "$CLIENT_UUID" != "null" ]; then
    echo "  Client already exists (UUID: ${CLIENT_UUID})"
else
    curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "Content-Type: application/json" \
      -d '{
        "clientId": "'"${CLIENT_ID}"'",
        "enabled": true,
        "protocol": "openid-connect",
        "publicClient": false,
        "directAccessGrantsEnabled": true,
        "serviceAccountsEnabled": false,
        "standardFlowEnabled": true,
        "implicitFlowEnabled": false,
        "redirectUris": ["*"],
        "webOrigins": ["*"],
        "attributes": {
          "access.token.lifespan": "1800"
        }
      }' > /dev/null
    
    # Get the newly created client UUID
    CLIENT_UUID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r ".[] | select(.clientId==\"${CLIENT_ID}\") | .id")
    
    echo "✓ Client created (UUID: ${CLIENT_UUID})"
fi
echo ""

# Get client secret
echo "Client Secret:"
CLIENT_SECRET=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients/${CLIENT_UUID}/client-secret" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r '.value')
echo "  ${CLIENT_SECRET}"
echo ""

# Create Roles
echo "Creating roles..."
for role in "USER" "ADMIN"; do
    ROLE_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles/${role}")
    
    if [ "$ROLE_EXISTS" = "200" ]; then
        echo "  Role ${role} already exists, skipping..."
    else
        curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}" \
          -H "Content-Type: application/json" \
          -d '{
            "name": "'"${role}"'",
            "description": "'"${role}"' role for Fin-Insight"
          }' > /dev/null
        echo "✓ Role ${role} created"
    fi
done
echo ""

# Create Test Users
echo "Creating test users..."

create_user() {
    local username=$1
    local password=$2
    local role=$3
    
    # Check if user exists
    USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${username}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r '.[0].id')
    
    if [ -n "$USER_ID" ] && [ "$USER_ID" != "null" ]; then
        echo "  User ${username} already exists (ID: ${USER_ID})"
    else
        # Create user
        curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}" \
          -H "Content-Type: application/json" \
          -d '{
            "username": "'"${username}"'",
            "enabled": true,
            "emailVerified": true,
            "email": "'"${username}@fininsight.local"'"
          }' > /dev/null
        
        # Get user ID
        USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=${username}" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r '.[0].id')
        
        # Set password
        curl -s -X PUT "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users/${USER_ID}/reset-password" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}" \
          -H "Content-Type: application/json" \
          -d '{
            "type": "password",
            "value": "'"${password}"'",
            "temporary": false
          }' > /dev/null
        
        # Assign role
        ROLE_DATA=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles/${role}" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}")
        
        curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users/${USER_ID}/role-mappings/realm" \
          -H "Authorization: Bearer ${ADMIN_TOKEN}" \
          -H "Content-Type: application/json" \
          -d "[${ROLE_DATA}]" > /dev/null
        
        echo "✓ User ${username} created with role ${role}"
    fi
}

create_user "testuser" "testpass" "USER"
create_user "admin" "adminpass" "ADMIN"

echo ""
echo "=================================="
echo "✓ Keycloak Configuration Complete!"
echo "=================================="
echo ""
echo "Summary:"
echo "  Realm:         ${REALM_NAME}"
echo "  Client ID:     ${CLIENT_ID}"
echo "  Client Secret: ${CLIENT_SECRET}"
echo ""
echo "Test Users:"
echo "  - testuser / testpass (USER role)"
echo "  - admin / adminpass (ADMIN role)"
echo ""
echo "Issuer URI:"
echo "  http://localhost:8080/realms/${REALM_NAME}"
echo ""
echo "To get a token:"
echo "  ./get-token.sh testuser testpass ${CLIENT_SECRET}"
echo ""
