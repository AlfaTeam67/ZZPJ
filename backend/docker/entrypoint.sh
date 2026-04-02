#!/bin/sh

set -e

SERVICE_PROFILES="${SPRING_PROFILES_ACTIVE:-docker}"
RUN_DB_MIGRATION="${RUN_DB_MIGRATION:-true}"

# DEBUG: Print current service name
echo "ENTRYPOINT: Starting service: ${SERVICE_NAME:-unknown}"

CONFIG_SERVER_URL="http://config-server:8888/actuator/health"
EUREKA_SERVER_URL="http://eureka-server:8761/actuator/health"

wait_for_service() {
  local service_url=$1
  local service_name=$2
  echo "DEBUG: Waiting for $service_name at $service_url..."
  until wget -qO- "$service_url" 2>/dev/null | grep -q '"status":"UP"'; do
    echo "DEBUG: $service_name is not ready yet. Retrying in 5s..."
    sleep 5
  done
  echo "DEBUG: $service_name is UP!"
}

run_db_migrations() {
  if [ "$RUN_DB_MIGRATION" != "true" ]; then
    echo "ENTRYPOINT: RUN_DB_MIGRATION is set to false. Skipping Flyway migrations."
    return
  fi

  echo "ENTRYPOINT: Running Flyway migrations for $SERVICE_NAME (profiles: ${SERVICE_PROFILES},migration)"
  if SPRING_PROFILES_ACTIVE="${SERVICE_PROFILES},migration" java -jar app.jar; then
    echo "ENTRYPOINT: Flyway migrations completed."
  else
    echo "ENTRYPOINT: Flyway migrations failed. Exiting."
    exit 1
  fi
}

case "$SERVICE_NAME" in
  "portfolio-manager" | "market-data-service" | "ai-advisor-service")
    echo "ENTRYPOINT: Business service detected. Waiting for infrastructure..."
    wait_for_service "$EUREKA_SERVER_URL" "Eureka Server"
    wait_for_service "$CONFIG_SERVER_URL" "Config Server"
    run_db_migrations
    ;;
  *)
    echo "ENTRYPOINT: Infrastructure service ($SERVICE_NAME) - starting immediately."
    ;;
esac

echo "ENTRYPOINT: Executing java -jar app.jar"
exec java -jar app.jar
