# SW Campus Server

Spring Boot 기반 **멀티모듈 구조**로, **Layered Architecture** (presentation → business → persistence →
database) 를 따른다.

<br />

## 🧩 프로젝트 구조

```shell
sw-campus-server
 ├─ sw-campus-api       # Presentation Layer (Controller)
 ├─ sw-campus-domain    # Business Logic Layer (Service)
 ├─ sw-campus-infra               
 │    ├─ db-postgres          # Write Storage (JPA, PostgreSQL)
 │    └─ file-s3              # File Storage (AWS S3)
 └─ sw-campus-shared    # Cross-cutting Layer (logging, security, monitoring 등)
```

### api

- REST API 엔드포인트
- 요청/응답 DTO
- 인증/인가, 예외 핸들링
- domain의 서비스 호출

---

### domain

- 도메인 서비스 / 비즈니스 로직
- Command / Query 로직 분리 (CQRS)
- 외부 기술(JPA, S3, Redis 등)에 직접 의존하지 않도록 설계

---

### infra

#### db-postgres

- JPA 엔티티(`@Entity`)
- `JpaRepository` 구현
- `BaseEntity` 등 DB 전용 상위 클래스

#### file-s3

- AWS S3 연동
- 파일 업로드/다운로드, Presigned URL 발급 등

---

### shared

> ⚠️ **공통 플랫폼 기능만 포함!** 비즈니스(도메인)에 강하게 결합되는 예외/로직은 넣지 않기

- 로깅, 보안 유틸, 모니터링
- 공통 에러 모델 / 에러 코드 규격

<br />

## 🚀 로컬 실행 방법

### 1. PostgreSQL 띄우기 (예시: Docker)

```shell
docker run \
  --name postgres \
  -e POSTGRES_PASSWORD=<your-local-password> \
  -e POSTGRES_DB=sw-campus \
  -p 5432:5432 \
  -d postgres:18
```

<br />

### 2. Submodule 설정 가져오기

```shell
git submodule update --init --recursive
```

업데이트 시:

```shell
git submodule update --remote --merge
```

<br />

### 3. 서버 실행
