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
- **Java 21 Virtual Threads & Coroutines**: 가상 스레드를 활성화하여 I/O 집약적인 마이데이터 호출 및 DB 조회 시 동시성 성능 극대화. 코루틴과 결합하여 비동기 프로그래밍 모델을 유지하면서도 가상 스레드의 경량성을 활용함.
- **Layered Configuration**: 기능별/환경별 YAML 설정 분리 및 관리.
- **Global Exception Handling**: 전역 예외 처리기 및 공통 응답 규격(`ApiResponse`) 구축.
- **Logging Optimization**: P6Spy를 활용한 SQL 한 줄 출력 및 IPv6 IP 주소의 IPv4 변환 로깅 적용.

### 2. 인증 및 보안 시스템 (Auth & Security)
- **Composite PK User Schema**: 이메일과 소셜 타입을 조합한 식별자 구조 구축.
- **Refresh Token Rotation (RTR)**: Redis를 활용하여 리프레시 토큰 재발급 시 기존 토큰을 무효화하여 탈취 위험 방지.
- **Logout & Blacklisting**: 로그아웃 시 Access Token을 Redis 블랙리스트에 등록하여 재사용 원천 차단.
- **OAuth2 Stateless Persistence**: 쿠키 기반 `AuthorizationRequestRepository`를 구현하여 `STATELESS` 환경에서도 소셜 로그인 인증 상태를 안정적으로 유지.
- **Security Auditing**: `SecurityLogger` 및 AOP를 통한 보안 이벤트 구조화 및 로그 파일 분리.
- **User Status Management**: `ACTIVE`(기본), `LINKED`(자산 연동 완료) 등 정교한 사용자 상태 관리 로직 구현.

### 3. 자산 및 거래 내역 도메인 (Asset & Transaction Core)
- **Independent Asset Tables**: 상속 구조의 복잡함과 조인 성능 저하를 피하기 위해 `Account`, `Card`, `Loan`을 각각 독립적인 테이블로 설계(`@MappedSuperclass` 활용).
- **Integrated Transaction Engine**: 모든 자산의 거래 내역을 단일 테이블에서 관리하며, ID 참조 방식을 채택하여 조인 없이 빠른 조회 성능 확보.
- **MyData Integration Simulation**: 
    - **자산 생성**: 계좌(+1,000 ~ 1억), 카드(-100 ~ -200만), 대출(0 ~ -1.5억) 범위의 현실적인 가상 데이터 생성 엔진 구축.
    - **거래 내역 생성**: 최근 90일간 매일 0~3건의 랜덤 거래를 생성하여 실제 데이터와 유사한 분포 구현.
    - **Category & Merchant**: 식비, 쇼핑 등 카테고리별 랜덤 가맹점 매칭 및 입/출금 타입 구분 로직 적용.

---

## ✨ 핵심 기술 강점 (Technical Excellence)

- **가상 스레드(Virtual Threads) 최적화**: 
    - **Transaction Integrity**: `Dispatchers.IO`를 이용한 강제 스레드 전환을 제거하고 가상 스레드 위에서 직접 Blocking I/O를 수행하도록 설계하여, JPA 트랜잭션 컨텍스트 유실 및 데이터베이스 업데이트 누락 문제를 원천 해결.
    - **Simplified Concurrency**: 가상 스레드가 블로킹 호출 시 스스로 캐리어 스레드를 양보(Yield)하는 특성을 활용하여, 복잡한 디스패처 관리 없이도 높은 처리량 확보.
- **보안성 강화**: 리프레시 토큰 로테이션(RTR), 블랙리스트 시스템, 그리고 **Redis 기반 Rate Limiting**을 구축하여 무상태(Stateless) 인증의 보안 약점 보완 및 무차별 대입 공격(Brute-force) 방어.
- **Non-blocking Coroutine 활용**: 
    - **Parallel Fetching**: 전체 자산 조회 시 `async`를 활용하여 여러 자산 테이블을 병렬로 조회함으로써 응답 속도를 획기적으로 개선.
- **OAuth2 인증 유지 전략**: `STATELESS` 세션 정책 하에서 소셜 로그인 시 인증 요청 정보(`state` 등) 유실 문제를 해결하기 위해 쿠키 기반의 `AuthorizationRequestRepository`를 커스텀 구현하여 보안성과 편의성 동시 확보.
- **비동기 인증 컨텍스트 전파**: Kotlin Coroutine(`suspend`) 환경에서 발생할 수 있는 `SecurityContext` 유실 문제를 `SecurityContextRepository` 명시적 저장을 통해 해결하여 비동기 처리의 무결성 확보.
- **확장성 있는 도메인 설계**: 
    - 상속의 복잡함을 배제하고 **ID 참조 기반의 통합 거래 테이블** 방식을 사용하여 대용량 데이터 집계 및 통계(QueryDSL)를 위한 최적의 기반 마련.
    - **Factory Pattern**을 도입하여 자산 및 거래 내역 생성 로직을 캡슐화.
- **빌드 및 개발 환경 최적화**: 
    - **Gradle Build Cache** 및 병렬 프로젝트 빌드 활성화로 빌드 속도 극대화.
    - **Kotlin Parallel Compilation** 및 테스트 병렬 실행(`maxParallelForks`) 설정을 통한 CI/CD 효율성 증대.
- **구조화된 로깅**: 모든 보안 이벤트를 `[SECURITY_EVENT]` 규격으로 남겨 사후 분석 및 모니터링 시스템(ELK 등) 연동 최적화.

---

## 📂 Project Structure

```text
src
├── main/kotlin/com/nexusfi/server
│   ├── api/v1/                # Presentation: REST API 컨트롤러 및 DTO (User, Asset, Transaction)
│   ├── application/           # Application: 비즈니스 로직 조율 (Service, Factory)
│   ├── domain/                # Domain: 비즈니스 핵심 규칙 및 엔티티
│   │   ├── user/              # 유저 도메인 (Status, Profile 관리)
│   │   ├── auth/              # 인증 도메인 (RefreshToken)
│   │   ├── asset/             # 자산 도메인 (Account, Card, Loan)
│   │   └── transaction/       # 거래 내역 도메인 (Integrated Transaction)
│   ├── infrastructure/        # Infrastructure: 외부 연동 및 기술적 설정
│   │   ├── security/          # Security: OAuth2, JWT, Security Config
│   │   ├── config/            # 전역 설정 (JPA, Redis, P6Spy, OpenAPI)
│   │   └── utils/             # 공용 유틸리티 (CookieUtils, SecurityLogger)
│   └── common/                # Common: 공통 예외 및 응답 규격 (BaseEntity 포함)
├── main/resources/            # Resources: 환경별 설정 및 SQL
└── test/kotlin/com/nexusfi/server
    └── application/           # Unit Tests (Auth, User, Asset 로직 검증)
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

### 2. 자산 연동 시뮬레이션
1.  **자산 연동**: `POST /api/v1/assets/link` 호출 시 가상의 마이데이터 연동이 수행됩니다. (최근 90일치 거래 내역 자동 생성)
2.  **상태 체크**: `GET /api/v1/users/me` 호출 시 `status` 필드를 통해 연동 완료 여부(`LINKED`)를 확인할 수 있습니다.

### 3. API 명세서 확인
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **공통 에러 규격**: `success`, `data`, `error` 필드를 포함한 표준 응답 규격 사용.

---

## 📅 개발 로드맵 (Backend Roadmap)

### Step 1: 인증 및 보안 고도화 (Auth & Security) ✅
- [x] **Refresh Token**: Redis를 활용한 토큰 재발급 및 Rotation 적용.
- [x] **Logout & Withdrawal**: 로그아웃 시 토큰 무효화 및 회원 탈퇴 처리.
- [x] **Profile Completion**: 신규 가입 후 추가 정보 입력 및 유저 상태 관리.
- [x] **Rate Limiting**: Redis를 활용한 무차별 대입 공격 방어 로직 구축.

### Step 2: 자산 및 거래 내역 도메인 구축 (Asset & Transaction Core) ✅
- [x] **Asset Entity Design**: 독립 테이블 전략을 활용한 자산 유형별 엔티티 설계.
- [x] **Integrated Transaction**: 모든 자산의 거래 내역 통합 관리 및 ID 참조 방식 도입.
- [x] **MyData Simulation**: 최근 90일치 거래 데이터 및 랜덤 자산 생성 엔진 구축.
- [x] **Virtual Thread Optimization**: 가상 스레드 환경에 최적화된 트랜잭션 전파 및 스레드 격리 제거.

### Step 3: 소비 분석 및 통계 (Analysis & Statistics) 🏃
- [ ] **Query DSL Aggregation**: 기간별, 카테고리별 지출 통계 쿼리 최적화.
- [ ] **Spending Trends**: 전월 대비 소비 증감 분석 API.
- [ ] **Category-wise Insight**: 가장 많이 소비한 카테고리 TOP 3 추출 로직.

### Step 4: 대시보드 시각화 및 고도화 (Advanced Features)
- [ ] **Dashboard API**: 총 자산, 순자산, 이번 달 지출액 통합 집계.
- [ ] **Data Masking**: 개인정보 보호를 위한 계좌/카드번호 마스킹 처리.

### Step 5: 배포 및 운영 (DevOps & Deployment)
- [ ] **CI/CD**: GitHub Actions를 통한 자동 배포 파이프라인.
- [ ] **AWS Architecture**: EC2, RDS 기반의 운영 환경 구축.

---

## ✅ Test Strategy
포트폴리오로서 기술적 무결성을 증명하기 위해 아래와 같은 테스트 전략을 따릅니다.

### 1. 테스트 기술 스택
- **Unit Test**: JUnit5, MockK (Kotlin Idiomatic Mocking)
- **Integration Test**: Spring Boot Test, Testcontainers (PostgreSQL, Redis)
- **API Simulation**: **WireMock** (마이데이터 외부 API 응답 시뮬레이션 기반 마련)

### 2. 마이데이터 연동 시나리오
실제 금융망 연동 제약을 극복하기 위해 **시나리오 기반 가상 데이터 생성** 엔진을 구축하였으며, 향후 WireMock을 통해 외부 API 호출 로직의 무결성을 검증할 예정입니다.

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

---
**Yeonghoon Mo (Backend Engineer) & Gemini Agent**
*Last Updated: 2026-02-26*
