# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
# We use -DskipTests for faster image building in this context,
# but in a formal CI/CD pipeline, you'd want tests to run.
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre-alpine

# Use a non-root user for security
# (alpine uses addgroup/adduser)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Set working directory
WORKDIR /app

# Copy the JAR from the builder stage
# Standard Spring Boot JAR name is <artifactId>-<version>.jar
# Based on pom.xml: <artifactId>demo</artifactId>, <version>0.0.1-SNAPSHOT</version>
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Environment variables can be overridden at runtime
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Entry point to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
