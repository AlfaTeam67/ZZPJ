#!/bin/bash

# Fin-Insight Backend - Test Script
# This script tests all services health endpoints and Eureka registration

set -e

echo "=================================="
echo "Fin-Insight Backend Health Check"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service health
check_health() {
    local service_name=$1
    local url=$2
    
    echo -n "Checking $service_name... "
    
    if response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null); then
        if [ "$response" = "200" ]; then
            echo -e "${GREEN}✓ UP${NC} (HTTP $response)"
            return 0
        else
            echo -e "${RED}✗ DOWN${NC} (HTTP $response)"
            return 1
        fi
    else
        echo -e "${RED}✗ UNREACHABLE${NC}"
        return 1
    fi
}

# Function to check Eureka registration
check_eureka_registration() {
    echo ""
    echo "Checking Eureka Service Registration..."
    echo "---------------------------------------"
    
    if response=$(curl -s "http://localhost:8761/eureka/apps" 2>/dev/null); then
        echo "$response" | grep -o "<name>[^<]*</name>" | sed 's/<name>//;s/<\/name>//' | while read -r service; do
            echo -e "${GREEN}✓${NC} $service registered"
        done
    else
        echo -e "${RED}✗ Failed to fetch Eureka registry${NC}"
    fi
}

# Main health checks
echo "Health Endpoints:"
echo "-----------------"

check_health "Eureka Server      " "http://localhost:8761/actuator/health"
check_health "Config Server      " "http://localhost:8888/actuator/health"
check_health "Keycloak          " "http://localhost:8080/health/ready"
check_health "Portfolio Manager  " "http://localhost:8081/actuator/health"
check_health "Market Data Service" "http://localhost:8082/actuator/health"
check_health "AI Advisor Service " "http://localhost:8083/actuator/health"

# Check Eureka registration
check_eureka_registration

# Check Config Server configuration retrieval
echo ""
echo "Config Server Tests:"
echo "--------------------"
echo -n "Fetching portfolio-manager config... "
if curl -s "http://localhost:8888/portfolio-manager/default" | grep -q "spring"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
fi

# Summary
echo ""
echo "=================================="
echo "Summary"
echo "=================================="
echo "All services checked!"
echo ""
echo "Useful URLs:"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Keycloak Admin:   http://localhost:8080 (admin/admin)"
echo "  - Portfolio API:    http://localhost:8081/swagger-ui.html"
echo "  - Market Data API:  http://localhost:8082/swagger-ui.html"
echo "  - AI Advisor API:   http://localhost:8083/swagger-ui.html"
echo ""
