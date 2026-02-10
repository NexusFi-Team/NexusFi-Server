# NexusFi Server

**NexusFi**의 백엔드 API 서버입니다.

## 🛠 Tech Stack
- **Language**: Kotlin 1.9.25 (JDK 21)
- **Framework**: Spring Boot 3.4.2
- **Build Tool**: Gradle 8.5 (Kotlin DSL)
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA, QueryDSL 5.1.0
- **Cache**: Redis (Lettuce)

## 🚀 Key Features
- **Virtual Threads**: Java 21 가상 스레드 활성화로 높은 처리량 보장.
- **Graceful Shutdown**: 안전한 애플리케이션 종료 지원.
- **Layered Configuration**: 기능별/환경별로 분리된 체계적인 YAML 설정 관리.

## ⚙️ Setup
```bash
# 빌드 및 실행
./gradlew bootRun
```
