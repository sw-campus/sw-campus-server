# Multi-stage build for Spring Boot application
FROM gradle:8-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐시를 활용하기 위해 build.gradle과 settings.gradle 먼저 복사
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# 서브프로젝트의 build.gradle 파일들 복사
COPY sw-campus-api/build.gradle ./sw-campus-api/
COPY sw-campus-domain/build.gradle ./sw-campus-domain/
COPY sw-campus-shared/logging/build.gradle ./sw-campus-shared/logging/
COPY sw-campus-infra/db-postgres/build.gradle ./sw-campus-infra/db-postgres/
COPY sw-campus-infra/oauth/build.gradle ./sw-campus-infra/oauth/
COPY sw-campus-infra/s3/build.gradle ./sw-campus-infra/s3/

# 의존성 다운로드 (캐시 활용)
RUN gradle build -x test --no-daemon || return 0

# 소스 코드 복사
COPY . .

# 애플리케이션 빌드
RUN gradle :sw-campus-api:bootJar --no-daemon

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/sw-campus-api/build/libs/*.jar app.jar

# 비root 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

