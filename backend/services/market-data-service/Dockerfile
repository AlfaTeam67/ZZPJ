# Stage 1: Build and Test
FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle :services:market-data-service:build --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/services/market-data-service/build/libs/*.jar app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]
