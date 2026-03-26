#!/bin/bash

# Fin-Insight - Get JWT Token from Keycloak
# Usage: ./get-token.sh <username> <password>

set -e

# Configuration
KEYCLOAK_URL="http://localhost:8080"
REALM="fin-insight"
CLIENT_ID="fin-insight-client"
CLIENT_SECRET="${CLIENT_SECRET:-}"  # Set via environment or pass as 3rd arg

# Arguments
USERNAME="${1:-testuser}"
PASSWORD="${2:-testpass}"

if [ -n "$3" ]; then
    CLIENT_SECRET="$3"
fi

echo "Fetching JWT token from Keycloak..."
echo "Username: $USERNAME"
echo ""

# Get token
RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" \
  -d "grant_type=password" \
  ${CLIENT_SECRET:+-d "client_secret=${CLIENT_SECRET}"})

# Check if response contains access_token
if echo "$RESPONSE" | jq -e '.access_token' > /dev/null 2>&1; then
    TOKEN=$(echo "$RESPONSE" | jq -r '.access_token')
    
    echo "✓ Token obtained successfully!"
    echo ""
    echo "Access Token:"
    echo "$TOKEN"
    echo ""
    echo "To use in curl:"
    echo "export TOKEN='$TOKEN'"
    echo "curl -H \"Authorization: Bearer \$TOKEN\" http://localhost:8081/api/portfolios"
    echo ""
    
    # Decode token (optional, requires jq)
    echo "Token Claims (decoded):"
    echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq . || echo "(could not decode)"
    
else
    echo "✗ Failed to obtain token"
    echo "Response:"
    echo "$RESPONSE" | jq . || echo "$RESPONSE"
    exit 1
fi
