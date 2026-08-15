# Stage 1: Build Java artifact
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Production Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -g 10001 -S sporekart && \
    adduser -u 10001 -S sporekart -G sporekart

USER sporekart:sporekart
COPY --from=build --chown=sporekart:sporekart /app/target/sporekart-backend-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
