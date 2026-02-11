# NexusFi Server (Backend API)

**NexusFi**는 마이데이터를 기반으로 사용자의 자산을 통합 관리하고 소비 패턴을 분석해주는 웹 서비스입니다. 본 프로젝트는 높은 처리량과 안정성을 목표로 최신 기술 스택을 활용하여 구축되었습니다.

## 🛠 Tech Stack

### Framework & Language
- **Language**: Kotlin 1.9.25
- **Runtime**: JDK 21 (LTS)
- **Framework**: Spring Boot 3.4.2

### Infrastructure & Database
- **Database**: PostgreSQL (Main DB)
- **Cache**: Redis (Session & Refresh Token 관리 예정)
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback (Profile-specific), P6Spy (SQL 파라미터 로깅)

### Security & Communication
- **Security**: Spring Security, OAuth2 Client
- **Authentication**: JWT (JSON Web Token)
- **API Communication**: Spring Cloud OpenFeign (마이데이터 API 연동 최적화)
- **API Docs**: Springdoc OpenAPI (Swagger UI)

---

## 🚀 현재 개발 완료된 기능 (Current Status)

### 1. 인프라 및 핵심 설정
- **Java 21 Virtual Threads**: 가상 스레드를 활성화하여 I/O 집약적인 마이데이터 호출 시 동시성 성능 극대화.
- **Layered Configuration**: `database.yml`, `jwt.yml`, `security.yml` 등 기능별/환경별 YAML 설정 분리.
- **Global Exception Handling**: 전역 예외 처리기 및 공통 응답 규격(`ApiResponse`) 구축.
- **JPA Auditing**: `BaseEntity`를 통한 생성/수정일 자동화.

### 2. 인증 시스템 (Auth)
- **SSO 연동**: 구글(Google) 및 카카오(Kakao) OAuth2 로그인 구현.
- **JWT Provider**: Access Token 생성 및 5단계 상세 검증(만료, 위조, 서명 등) 로직.
- **JWT Filter**: `OncePerRequestFilter` 기반의 인증 필터 및 필터 레이어 전용 에러 핸들링.
- **User Domain**: 소셜 로그인 정보를 기반으로 한 유연한 사용자 엔티티 설계.

---

## 📄 프론트엔드 연동 가이드 (Integration Guide)

### 1. 소셜 로그인 흐름
1.  **로그인 시작**: 아래 엔드포인트로 브라우저를 직접 이동시킵니다.
    - 구글: `GET http://localhost:8080/oauth2/authorization/google`
    - 카카오: `GET http://localhost:8080/oauth2/authorization/kakao`
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

### Step 1: 인증 및 보안 고도화 (Auth & Security)
로그인 유지 및 안전한 세션 관리를 위한 보안 로직을 완성합니다.
- [ ] **Refresh Token**: Redis를 활용한 토큰 재발급 및 Rotation 적용.
- [ ] **Logout & Withdrawal**: 로그아웃 시 토큰 무효화(Blacklist) 및 회원 탈퇴 데이터 정리.
- [ ] **Profile Completion**: 신규 가입 후 생년월일, 성별 등 필수 정보 입력 API.

### Step 2: 자산 관리 도메인 구축 (Asset Core)
마이데이터 연동 전, 자산 데이터를 체계적으로 관리할 수 있는 구조를 잡습니다.
- [ ] **Asset Entity Design**: 은행, 계좌, 카드, 대출 등 자산 유형별 엔티티 설계.
- [ ] **Asset CRUD API**: 사용자가 자산을 직접 등록/수정/삭제하는 기본 기능 구현.
- [ ] **Design Pattern**: 자산 유형별(예금, 대출 등) 처리를 유연하게 할 **Factory Pattern** 도입 고려.

### Step 3: 소비 분석 및 가계부 (Consumption & Ledger)
대시보드의 핵심인 소비 데이터를 처리하고 분석합니다.
- [ ] **Ledger Schema**: 일자별 수입/지출 내역 저장 구조 설계.
- [ ] **Calendar API**: 월간 소비 흐름을 한눈에 볼 수 있는 달력 형태의 집계 API.
- [ ] **Statistics Optimization**: 카테고리별/기간별 지출 통계 쿼리 최적화 (**QueryDSL** 도입 예정).

### Step 4: 대시보드 및 시각화 (Dashboard Aggregation)
프론트엔드에서 즉시 사용할 수 있도록 데이터를 가공하여 제공합니다.
- [ ] **Dashboard API**: 총 자산, 월간 소비액, 전월 대비 증감률 등 핵심 지표 집계.
- [ ] **Chart Data**: 도넛 차트(자산 비율), 막대 그래프(소비 추이)용 JSON 응답 규격화.

### Step 5: 배포 및 운영 (DevOps & Deployment)
안정적인 서비스 운영을 위한 클라우드 환경을 구축합니다.
- [ ] **CI/CD**: GitHub Actions를 통한 자동 빌드 및 배포 파이프라인 구축.
- [ ] **AWS Architecture**: 비용 효율적인 프리티어(Free-tier) 인프라 구성.

---

## ☁️ 인프라 아키텍처 전략 (Infrastructure)
포트폴리오용 프로젝트로서 **비용 효율성**과 **운영 편의성**을 모두 잡기 위해 AWS 프리티어를 적극 활용합니다.

| 서비스 | 구성 방식 | 스펙 (Free Tier) | 비고 |
| :--- | :--- | :--- | :--- |
| **WAS** | **AWS EC2** | `t2.micro` (1 vCPU, 1GB RAM) | Docker 기반 애플리케이션 실행 |
| **DB** | **Amazon RDS** | `db.t3.micro` (PostgreSQL) | 관리형 DB 사용, 자동 백업 지원 |
| **Cache** | **Redis Cloud** | Free Plan (30MB) | EC2 메모리 절약을 위해 외부 관리형 서비스 사용 |
| **Web** | **Vercel / Netlify** | Free Plan | 정적 프론트엔드 호스팅 및 CDN 제공 |

---

## ⚙️ 실행 방법 (How to Run)

### Prerequisites
- Docker & Docker Compose
- JDK 21

### 1. 인프라 실행 (Database, Redis)
프로젝트 루트에서 아래 명령어를 실행하면 필요한 모든 데이터베이스 환경이 구축됩니다.
```bash
docker-compose up -d
```
- **DB (PostgreSQL)**: `localhost:5432` (ID: `root` / PW: `1361`)
- **Cache (Redis)**: `localhost:6379`
- **DB GUI (PgAdmin)**: [http://localhost:5050](http://localhost:5050) (ID: `admin@nexusfi.com` / PW: `admin`)

### 2. 어플리케이션 실행
```bash
# 빌드 및 실행
./gradlew bootRun
```