# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy the entire project context (including the 'demo' directory)
# This assumes the Dockerfile is at the repository root
COPY . .

# Build the application from the 'demo' directory
WORKDIR /app/demo
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre-alpine

# Use a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Set working directory
WORKDIR /app

# Copy the JAR from the builder stage
# Standard Spring Boot JAR name is <artifactId>-<version>.jar
# Based on pom.xml in 'demo': <artifactId>demo</artifactId>, <version>0.0.1-SNAPSHOT</version>
COPY --from=builder /app/demo/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (Render sets the PORT env var)
# We can use it in the entrypoint
EXPOSE 8080

# Environment variables can be overridden at runtime
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Entry point to run the application
# We use -Dserver.port=${PORT:-8080} to support Render's PORT assignment
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
