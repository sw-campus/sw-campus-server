# Build stage
FROM gradle:8-jdk17-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

COPY sw-campus-api/build.gradle ./sw-campus-api/
COPY sw-campus-domain/build.gradle ./sw-campus-domain/
COPY sw-campus-shared/logging/build.gradle ./sw-campus-shared/logging/
COPY sw-campus-infra/db-postgres/build.gradle ./sw-campus-infra/db-postgres/
COPY sw-campus-infra/oauth/build.gradle ./sw-campus-infra/oauth/
COPY sw-campus-infra/s3/build.gradle ./sw-campus-infra/s3/

# 캐시(의존성)만 먼저 당겨놓기
RUN gradle :sw-campus-api:dependencies --no-daemon || true

COPY . .
RUN gradle :sw-campus-api:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# bootJar만 하나 복사
COPY --from=builder /app/sw-campus-api/build/libs/*boot.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
