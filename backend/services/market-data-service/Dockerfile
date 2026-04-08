# Stage 1: Build and Test
FROM gradle:8.10-jdk21-alpine AS builder
WORKDIR /app

# Install ca-certificates for secure HTTPS connections
RUN apk add --no-cache ca-certificates && update-ca-certificates

ENV GRADLE_OPTS="-Dorg.gradle.vfs.watch=false -Dorg.gradle.native=false -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3"
COPY . .
RUN set -eux; \
    for attempt in 1 2 3; do \
      gradle :services:market-data-service:build -x test --no-daemon --no-watch-fs && break; \
      if [ "$attempt" -eq 3 ]; then exit 1; fi; \
      echo "Gradle build failed (attempt $attempt), retrying in 10s..."; \
      sleep 10; \
    done

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/services/market-data-service/build/libs/*.jar app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]
