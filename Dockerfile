# Build stage: 빌드 도구 설치 없이 Jar 파일을 생성하는 단계
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 모든 소스 복사
COPY . .

# 실행 권한 부여 및 빌드 진행 (테스트 제외하여 속도 향상)
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# Run stage: 실행에 필요한 최소한의 환경만 구성하는 단계
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
