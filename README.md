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

## 📅 추후 개발 로드맵 (Roadmap)

### Phase 1: 인증 및 사용자 고도화
- [ ] **Refresh Token**: Redis 연동을 통한 토큰 재발급 및 세션 연장 로직 구현.
- [ ] **Profile Completion**: 신규 가입 사용자의 생년월일, 성별 등 추가 정보 입력 API.
- [ ] **User Withdrawal**: 회원 탈퇴 로직 구현.

### Phase 2: 마이데이터 연동 (Core)
- [ ] **CI (Connecting Information)**: 본인 인증 API 연동 및 고유 식별값 저장.
- [ ] **Asset Scrapping**: FeignClient를 활용한 은행/카드사 자산 정보 수집 엔진.
- [ ] **Data Normalization**: 수집된 금융 데이터를 내부 표준 규격으로 정규화.

### Phase 3: 분석 및 대시보드
- [ ] **Consumption Analysis**: 월별 소비 카테고리 분석 로직.
- [ ] **Asset Visualization**: 자산 총액 및 변동 그래프 데이터 API.
- [ ] **Batch Jobs**: 매일 새벽 자동 데이터 동기화 및 통계 배치 프로세스.

---

## ⚙️ 실행 방법 (How to Run)

### Prerequisites
- Docker (PostgreSQL, Redis)
- JDK 21

### Commands
```bash
# 데이터베이스 실행 (Docker 사용 시)
docker start PostgreSQL Redis

# 어플리케이션 빌드 및 실행
./gradlew bootRun
```