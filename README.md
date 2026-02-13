# NexusFi Server (Backend API)

**NexusFi**는 마이데이터를 기반으로 사용자의 자산을 통합 관리하고 소비 패턴을 분석해주는 웹 서비스입니다. 본 프로젝트는 높은 처리량과 안정성을 목표로 최신 기술 스택을 활용하여 구축되었습니다.

## 🛠 Tech Stack

### Framework & Language
- **Language**: Kotlin 1.9.25
- **Runtime**: JDK 21 (LTS)
- **Framework**: Spring Boot 3.4.2

### Infrastructure & Database
- **Database**: PostgreSQL (Composite PK: `email`, `social_type` 적용)
- **Cache**: Redis (Refresh Token 관리 및 로그아웃 블랙리스트)
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback, P6Spy (SQL 파라미터 로깅 최적화)

### Security & Communication
- **Security**: Spring Security, OAuth2 Client
- **Authentication**: JWT (Custom Claim: `social_type` 포함)
- **API Communication**: Spring Cloud OpenFeign (마이데이터 API 연동 최적화)
- **API Docs**: Springdoc OpenAPI (Swagger UI)

---

## 🚀 현재 개발 완료된 기능 (Current Status)

### 1. 인프라 및 핵심 설정
- **Java 21 Virtual Threads**: 가상 스레드를 활성화하여 I/O 집약적인 마이데이터 호출 시 동시성 성능 극대화.
- **Layered Configuration**: 기능별/환경별 YAML 설정 분리 및 관리.
- **Global Exception Handling**: 전역 예외 처리기 및 공통 응답 규격(`ApiResponse`) 구축.
- **Logging Optimization**: P6Spy를 활용한 SQL 한 줄 출력 및 IPv6 IP 주소의 IPv4 변환 로깅 적용.

### 2. 인증 및 보안 시스템 (Auth & Security)
- **Composite PK User Schema**: 이메일과 소셜 타입을 조합한 식별자 구조 구축.
- **Refresh Token Rotation (RTR)**: Redis를 활용하여 리프레시 토큰 재발급 시 기존 토큰을 무효화하여 탈취 위험 방지.
- **Logout & Blacklisting**: 로그아웃 시 Access Token을 Redis 블랙리스트에 등록하여 재사용 원천 차단.
- **Security Auditing**: `SecurityLogger`를 통한 보안 이벤트 구조화 및 로그 파일 분리.

---

## ✨ 핵심 기술 강점 (Technical Excellence)

- **보안성 강화**: 리프레시 토큰 로테이션(RTR), 블랙리스트 시스템, 그리고 **Redis 기반 Rate Limiting**을 구축하여 무상태(Stateless) 인증의 보안 약점 보완 및 무차별 대입 공격(Brute-force) 방어.
- **비동기 인증 컨텍스트 전파**: Kotlin Coroutine(`suspend`) 환경에서 발생할 수 있는 `SecurityContext` 유실 문제를 `SecurityContextRepository` 명시적 저장을 통해 해결하여 비동기 처리의 무결성 확보.
- **유연한 아키텍처**: 순환 참조(Circular Dependency)를 빈 설정 분리로 해결하고, 관점 지향 프로그래밍(AOP) 도입을 위한 기반을 마련하여 높은 유지보수성 지향.
- **구조화된 로깅**: 모든 보안 이벤트를 `[SECURITY_EVENT]` 규격으로 남겨 사후 분석 및 모니터링 시스템(ELK 등) 연동 최적화.
- **코드 무결성**: MockK를 활용한 단위 테스트를 통해 비즈니스 로직의 안정성 확보 및 유지보수성 향상.

---

## 📂 Project Structure

```text
src
├── main/kotlin/com/nexusfi/server
│   ├── api/v1/                # Presentation: REST API 컨트롤러 및 DTO
│   ├── application/           # Application: 비즈니스 로직 조율 (Service)
│   ├── domain/                # Domain: 비즈니스 핵심 규칙 및 엔티티
│   │   ├── user/              # 유저 도메인 (model, repository)
│   │   └── auth/              # 인증 도메인 (RefreshToken)
│   ├── infrastructure/        # Infrastructure: 외부 연동 및 기술적 설정
│   │   ├── security/          # Security: OAuth2, JWT, Security Config
│   │   ├── config/            # 전역 설정 (JPA, Redis, P6Spy, OpenAPI)
│   │   └── utils/             # 공용 유틸리티 (CookieUtils, SecurityLogger)
│   └── common/                # Common: 공통 예외 및 응답 규격 (BaseEntity 포함)
├── main/resources/            # Resources: 환경별 설정 및 SQL
│   ├── application.yml        # 메인 설정 및 프로파일 관리
│   └── (database, security, jwt, logging, redis).yml
└── test/kotlin/com/nexusfi/server
    └── application/           # Unit Tests (Auth, User 로직 검증)
```

---

## 📄 프론트엔드 연동 가이드 (Integration Guide)

### 1. 소셜 로그인 흐름
1.  **로그인 시작**: 아래 엔드포인트로 브라우저를 직접 이동시킵니다.
    - 구글: `GET /oauth2/authorization/google`
    - 카카오: `GET /oauth2/authorization/kakao`
2.  **인증 완료**: 소셜 로그인이 성공하면 서버에서 아래 URL로 리다이렉트합니다.
    - `URL`: `http://localhost:3000/login/callback?token={JWT_ACCESS_TOKEN}`
3.  **토큰 관리**: 쿼리 파라미터의 `token`을 추출하여 저장하고, 이후 모든 요청 헤더에 담아주세요.
    - `Header`: `Authorization: Bearer {JWT_ACCESS_TOKEN}`

### 2. API 명세서 확인
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **JWT 테스트**: Swagger 상단의 **Authorize** 버튼을 클릭하고 `Bearer {token}` 형식으로 입력하면 인증이 필요한 API를 테스트할 수 있습니다.

### 3. 공통 에러 규격
모든 에러 응답은 아래 형식을 따릅니다.
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "A002",
    "message": "만료된 토큰입니다."
  },
  "timestamp": "2026-02-10T12:00:00"
}
```

---

## 📅 개발 로드맵 (Backend Roadmap)

### Step 1: 인증 및 보안 고도화 (Auth & Security) ✅
- [x] **Refresh Token**: Redis를 활용한 토큰 재발급 및 Rotation 적용.
- [x] **Logout & Withdrawal**: 로그아웃 시 토큰 무효화 및 회원 탈퇴 처리.
- [x] **Profile Completion**: 신규 가입 후 추가 정보(생년월일 등) 입력 API.
- [x] **Token Blacklisting**: 로그아웃된 AccessToken의 재사용을 방지하기 위한 블랙리스트 구현.
- [x] **Coroutine Migration**: 전반적인 인증/유저 로직에 Kotlin Coroutine 적용.
- [x] **Auth Unit Test**: MockK를 이용한 인증/인가 로직 테스트 코드 작성.
- [x] **Security Auditing**: 전용 로거를 통한 보안 이벤트 구조화 및 로그 분리.
- [ ] **Rate Limiting**: Redis를 활용한 무차별 대입 공격(Brute-force) 방어 로직 구축.
- [ ] **AOP Migration**: 비즈니스 로직과 보안 로깅의 완전한 분리를 위한 AOP 기반 로깅 도입.

### Step 2: 자산 관리 도메인 구축 (Asset Core) 🏃
- [ ] **Asset Entity Design**: JPA `SINGLE_TABLE` 전략을 활용한 자산 유형별 엔티티 설계.
- [ ] **Asset CRUD API**: 계좌, 카드 등 사용자 자산 통합 관리 기능.
- [ ] **Design Pattern**: 자산 유형별 처리를 위한 **Factory Pattern** 도입.
- [ ] **Soft Delete & Auditing**: 금융 데이터 이력 관리를 위한 삭제 처리 및 생성/수정 시간 자동화.
- [ ] **Data Masking**: 민감 정보(계좌번호 등) 보호를 위한 Jackson 기반 마스킹 처리.
- [ ] **Bulk Insert Optimization**: 대량 자산 데이터 동기화 성능 최적화.

### Step 3: 소비 분석 및 가계부 (Consumption & Ledger)
- [ ] **Ledger Schema**: 일자별 수입/지출 내역 저장 구조 설계.
- [ ] **Calendar API**: 월간 소비 흐름 집계 API.
- [ ] **Query DSL**: 통계 쿼리 최적화.

### Step 4: 대시보드 및 시각화 (Dashboard Aggregation)
- [ ] **Dashboard API**: 총 자산, 월간 소비액 등 핵심 지표 집계.
- [ ] **Chart Data**: 차트 라이브러리 연동을 위한 JSON 응답 규격화.

### Step 5: 배포 및 운영 (DevOps & Deployment)
- [ ] **CI/CD**: GitHub Actions를 통한 자동 배포 파이프라인.
- [ ] **AWS Architecture**: EC2, RDS 기반의 안정적인 운영 환경 구축.

---

## ✅ Test Strategy
포트폴리오로서 기술적 무결성을 증명하기 위해 아래와 같은 테스트 전략을 따릅니다.

### 1. 테스트 기술 스택
- **Unit Test**: JUnit5, MockK (Kotlin Idiomatic Mocking)
- **Integration Test**: Spring Boot Test, Testcontainers (PostgreSQL, Redis)
- **API Simulation**: **WireMock** (마이데이터 외부 API 응답 시뮬레이션)

### 2. 마이데이터 연동 시뮬레이션 (MyData Simulation)
실제 금융망 연동 제약을 극복하고 아키텍처 설계를 증명하기 위해 **시뮬레이션 통신**을 수행합니다.
- **External API Simulation**: OpenFeign을 통해 외부 API를 호출하되, 개발/테스트 환경에서는 WireMock 서버가 실제 마이데이터 표준 규격(JSON)에 맞는 데이터를 반환하도록 구성하여 연동 로직의 무결성을 검증합니다.

---

## ⚙️ 실행 방법 (How to Run)

### 🅰️ 전체 환경 실행 (Docker)
```bash
docker-compose up -d --build
```

### 🅱️ 애플리케이션 로컬 실행 (Hybrid)
인프라(DB, Redis)만 도커로 실행하고 WAS는 IDE에서 직접 실행합니다.
```bash
# 1. 인프라 실행
docker-compose up -d nexusfi-db nexusfi-redis

# 2. 애플리케이션 실행
./gradlew bootRun
```
