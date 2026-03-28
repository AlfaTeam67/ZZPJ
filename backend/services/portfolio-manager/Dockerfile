# Stage 1: Build and Test
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
ENV GRADLE_OPTS="-Dorg.gradle.vfs.watch=false -Dorg.gradle.native=false"
COPY . .
RUN gradle :services:portfolio-manager:build --no-daemon --no-watch-fs

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/services/portfolio-manager/build/libs/*.jar app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]
