# Multi-stage build for Eureka Server

# Stage 1: Build the JAR
FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app
COPY . .
# Skip tests to speed up the build for now
RUN gradle :services:eureka-server:bootJar --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/services/eureka-server/build/libs/*.jar app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]
