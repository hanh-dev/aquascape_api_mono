# Stage 1: Build stage with Maven cache optimization
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache \
    curl \
    bash \
    && addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --chown=appuser:appgroup mvnw .
COPY --chown=appuser:appgroup .mvn .mvn
COPY --chown=appuser:appgroup pom.xml .

RUN chmod +x ./mvnw \
    && ./mvnw dependency:go-offline -B

COPY --chown=appuser:appgroup src ./src

RUN ./mvnw clean package -DskipTests -B \
    && mv target/*.jar app.jar

# Stage 2: Runtime stage - minimal and secure
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk --no-cache upgrade \    
    && apk add --no-cache \
    dumb-init \
    curl \
    && addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup \
    && rm -rf /var/cache/apk/*

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/app.jar app.jar

USER appuser:appgroup

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]

CMD ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+OptimizeStringConcat", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]