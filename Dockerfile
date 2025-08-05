# Multi-stage build for Spring Boot application
FROM maven:3.9.11-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8092

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
