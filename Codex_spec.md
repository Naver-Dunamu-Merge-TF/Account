# CryptoOrder Server Spec

## 1. Scope
- 본 문서는 현재 `CryptoOrder` 서버에 구현된 기능을 기준으로 작성한다.
- 외부 공개 API 범위는 인증(`signup/login/refresh`)과 JWKS 조회다.
- 계정/원화/포인트 관련 로직은 서비스/엔티티 수준으로 구현되어 있으나 현재 별도 컨트롤러 API로 노출되지 않는다.

## 2. Runtime / Stack
- Java 21
- Spring Boot 3.5.x
- Spring Web, Data JPA, Validation, Security, Actuator
- DB: PostgreSQL (runtime), H2 (test)
- JWT: Nimbus JOSE + JWT
- Metric: Prometheus registry

## 3. Authentication API

### 3.1 `POST /auth/signup`
- Headers
  - `Idempotency-Key` (필수, 공백 불가, 최대 200자)
- Body
  - `name` (required)
  - `phone` (required, `010-1234-5678` 형식)
  - `birthDate` (required, ISO date)
  - `loginId` (required)
  - `password` (required, 최소 8자)
  - `provider` (optional, 기본값 `MEMBER`)
  - `externalUserId` (optional)
- 처리
  - 사용자/로그인 계정/네이버포인트 지갑 생성
  - `custody.provision.enabled=true`인 경우 커스터디 지갑 프로비저닝 호출
  - access/refresh 토큰 발급
- Response `200 OK`
  - `tokenType`, `accessToken`, `accessTokenExpiresIn`, `refreshToken`, `refreshTokenExpiresIn`, `userId`

### 3.2 `POST /auth/login`
- Headers
  - `Idempotency-Key` (필수)
- Body
  - `loginId` (required)
  - `password` (required)
- 처리
  - ID/PW 검증
  - access/refresh 토큰 발급
- Response `200 OK`

### 3.3 `POST /auth/refresh`
- Headers
  - `Idempotency-Key` (필수)
- Body
  - `refreshToken` (required)
- 처리
  - refresh token 회전(기존 토큰 revoke)
  - 신규 access/refresh 토큰 발급
- Response `200 OK`

## 4. JWT / JWKS Spec

### 4.1 토큰 발급
- Access Token claims
  - `sub`: user UUID
  - `iss`: `auth.jwt-issuer`
  - `aud`: `auth.jwt-audience`
  - `iat`, `exp`, `jti`
  - `roles`: `['USER']`
  - `provider` (값 있을 때만)
  - `externalUserId` (값 있을 때만)
- 알고리즘
  - `auth.mode=JWKS` -> `RS256`
  - `auth.mode=HMAC` -> `HS256`

### 4.2 JWKS endpoint
- `GET /.well-known/jwks.json`
- `auth.mode=JWKS`: 공개키(`kid=auth.jwt-key-id`)를 `keys[]`로 반환
- `auth.mode=HMAC`: `{ "keys": [] }` 반환

### 4.3 키 로딩 정책
- JWKS 모드
  - 다음 쌍 중 하나로 RSA 키 제공
    - base64: `auth.jwt-private-key-base64` + `auth.jwt-public-key-base64`
    - 파일경로: `auth.jwt-private-key-path` + `auth.jwt-public-key-path`
  - base64와 path 동시 지정 금지
  - 키 미지정 시 `auth.allow-ephemeral-keys=true`일 때만 임시 키 허용
- HMAC 모드
  - `auth.hmac-secret-base64` 필수
  - 디코드 기준 최소 32바이트

## 5. Idempotency Spec
- 적용 대상
  - `POST /auth/signup`
  - `POST /auth/login`
  - `POST /auth/refresh`
- 키 구성
  - 내부 request key: `{operation}:{Idempotency-Key}`
  - 예: `POST:/auth/signup:signup-member-a`
- payload 무결성
  - 요청 body SHA-256 해시 저장
  - 동일 키 + 다른 payload면 `409 Conflict`
- 상태
  - `IN_PROGRESS`, `COMPLETED`
- 처리 규칙
  - 동일 키가 `IN_PROGRESS`이고 lock TTL 이내면 `202 Accepted`
  - lock TTL 경과한 stale `IN_PROGRESS`면 락 연장 후 재처리
  - `COMPLETED`면 저장된 응답 재생(replay)
- 응답 저장
  - 상태코드 + body 저장
  - body는 AES-GCM으로 암호화 저장 (`idempotency.response-encryption-key-base64`)
  - 키 미설정 시 `idempotency.allow-ephemeral-encryption-key=true`일 때만 임시 키 허용

## 6. Error Response / Status Mapping
- 공통 오류 응답 포맷
  - `timestamp`, `status`, `error`, `message`, `path`

- 주요 매핑
  - `UnauthorizedException` -> `401`
  - `UpstreamServiceException` -> `503`
  - `IdempotencyConflictException` -> `409`
  - `IdempotencyInProgressException` -> `202`
  - `IllegalArgumentException` -> `400`
  - `IllegalStateException` -> `409`
  - Validation 실패(`MethodArgumentNotValidException`) -> `400`
  - 보안 미인증(`AuthenticationException`) -> `401`
  - 보안 권한없음(`AccessDeniedException`) -> `403`
  - 기타 예외 -> `500`

## 7. Security Policy
- Permit-all 경로
  - `/auth/**`
  - `/.well-known/jwks.json`
  - `/h2-console/**`
  - `/actuator/health`
- 그 외 경로는 인증 필요
- `httpBasic`, `formLogin`, `logout` 비활성화
- Password encoder: `DelegatingPasswordEncoder`(기본 bcrypt)

## 8. Signup + Custody Provision Flow
1. `signup` 요청 수신 + idempotency 처리 시작
2. 사용자/계정 생성(중복 loginId는 예외 처리)
3. 커스터디 프로비저닝 호출 (`member-signup-{userId}` idempotency key 전달)
4. 성공 시 access/refresh 토큰 발급
5. 실패 시 `503` 반환

- 재시도 특성
  - 프로비저닝 실패 후 같은 사용자 데이터로 재시도하면 기존 계정을 재사용하고 프로비저닝을 다시 시도한다.
  - 동일 `loginId`라도 비밀번호/provider/externalUserId가 다르면 중복 오류 처리한다.

## 9. Internal Domain (Implemented, Not Exposed as Public Controller)
- `User`
- `Account` (로그인 정보, provider/externalUserId 포함)
- `NaverPoint`, `NaverPointHistory`
- `KRWAccount`, `KRWTransaction`, `KRWAccountHistory`
- `RefreshToken` (DB에는 token hash만 저장)
- 서비스 레이어에 입금/출금/회원탈퇴 로직 구현 및 비관적 락 적용

## 10. Config Keys (주요)
- Auth
  - `AUTH_MODE` (`JWKS`/`HMAC`)
  - `AUTH_JWT_ISSUER`
  - `AUTH_JWT_AUDIENCE`
  - `AUTH_ACCESS_TOKEN_TTL_SECONDS`
  - `AUTH_REFRESH_TOKEN_TTL_SECONDS`
  - `AUTH_JWT_KEY_ID`
  - `AUTH_JWT_PRIVATE_KEY_BASE64` / `AUTH_JWT_PUBLIC_KEY_BASE64`
  - `AUTH_JWT_PRIVATE_KEY_PATH` / `AUTH_JWT_PUBLIC_KEY_PATH`
  - `AUTH_HMAC_SECRET_BASE64`
  - `AUTH_ALLOW_EPHEMERAL_KEYS`
- Custody
  - `CUSTODY_PROVISION_ENABLED`
  - `CUSTODY_BASE_URL`
  - `CUSTODY_PROVISION_PATH`
  - `SERVICE_TOKEN`
  - `SERVICE_TOKEN_HEADER`
  - `CUSTODY_CONNECT_TIMEOUT_MILLIS`
  - `CUSTODY_READ_TIMEOUT_MILLIS`
- Idempotency
  - `IDEMPOTENCY_IN_PROGRESS_TTL_SECONDS`
  - `IDEMPOTENCY_RESPONSE_ENCRYPTION_KEY_BASE64`
  - `IDEMPOTENCY_ALLOW_EPHEMERAL_ENCRYPTION_KEY`

## 11. Verified by Tests
- 회원가입 시 JWT claim/토큰 발급 검증
- 로그인 + refresh token 회전 검증
- JWKS endpoint 응답 검증
- HMAC 모드 HS256 발급 검증
- Idempotency replay/conflict/in-progress/stale 복구 검증
- 전역 예외 응답 포맷 검증
- 계정/입출금 서비스 통합 테스트 검증
