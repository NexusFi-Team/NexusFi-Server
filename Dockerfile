# Build stage - Alpine 대신 Debian 기반 이미지 사용 (ARM64 호환성)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 해결을 위해 필요한 파일만 먼저 복사하여 캐싱 활용
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# gradlew 개행 문자 수정 및 실행 권한 부여
RUN tr -d '\r' < gradlew > gradlew_unix && \
    mv gradlew_unix gradlew && \
    chmod +x gradlew && \
    chmod -R +x gradle/

# 의존성 다운로드 (캐싱 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Run stage - 런타임은 Alpine 사용 가능 (네이티브 빌드 없음)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 및 타임존 설정
ENV SPRING_PROFILES_ACTIVE=local
ENV TZ=Asia/Seoul

EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
