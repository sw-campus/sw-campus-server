FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# CI에서 이미 만들어진 jar만 복사
COPY sw-campus-api/build/libs/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
