# Multi-stage build for Eureka Server
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

# Expose Eureka Server port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
