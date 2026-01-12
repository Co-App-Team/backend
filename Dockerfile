# Generated build Claude-4.5 and revised by Bao Ngo

# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Install ca-certificates for HTTPS connections
RUN apk add --no-cache ca-certificates

WORKDIR /app

# Copy gradle wrapper files
COPY gradlew .
COPY gradle gradle

# Copy gradle files first for better caching
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached if build.gradle hasn't changed)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Set JVM options for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]