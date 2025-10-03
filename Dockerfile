# Multi-stage build for better dependency management
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies with retry logic and better network handling
RUN ./mvnw dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true || \
    (echo "First attempt failed, retrying with different settings..." && \
     ./mvnw dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.http.retryHandler.retryInterval=2000)

# Copy source code
COPY src src

# Build the application with network optimizations
RUN ./mvnw clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

# Debug: List what was created
RUN ls -la target/
RUN find target/ -name "*.jar" -type f

# Verify JAR file exists
RUN if [ ! -f target/app.jar ]; then echo "ERROR: JAR file not created!"; exit 1; else echo "SUCCESS: JAR file created successfully"; fi

# Runtime stage
FROM amazoncorretto:17-alpine

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from build stage
COPY --from=build /app/target/app.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appuser && adduser -D -u 1001 -G appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
