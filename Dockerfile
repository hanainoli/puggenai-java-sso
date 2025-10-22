# ============================================
# Multi-stage Dockerfile for Auth Service
# Keys are included in src/main/resources/keys
# Java 21
# ============================================

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code (includes src/main/resources/keys)
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add wget for healthcheck
RUN apk add --no-cache wget

# Copy the JAR file from build stage
# Keys are already inside the JAR in BOOT-INF/classes/keys/
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]