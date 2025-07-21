# Multi-stage build for efficient Docker image
FROM gradle:7.6-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy Gradle configuration files first (for better layer caching)
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/

# Download dependencies (this layer will be cached if dependencies don't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src/

# Build the application
RUN gradle build -x test --no-daemon

# Runtime stage with minimal JRE
FROM openjdk:17-jre-slim AS runtime

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r temporal && useradd -r -g temporal temporal

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R temporal:temporal /app

# Switch to non-root user
USER temporal

# Expose the application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Set the entry point with JVM options
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar 