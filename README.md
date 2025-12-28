# SW Campus Server

// trigger image build

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

<br />

## 🗄️ Flyway 마이그레이션 자동 생성

JPA 엔티티 변경 사항을 기반으로 Flyway 마이그레이션 SQL 파일을 자동 생성합니다.

### 사전 요구사항

- Docker 실행 중
- Gradle 빌드 가능 상태

### 실행 방법

```shell
./scripts/generate-migration.sh
```

### 자동 파일명 생성 (Django 스타일)

변경 사항을 자동으로 감지하여 파일명을 생성합니다:

| 변경 유형 | 생성되는 파일명 예시 |
|----------|---------------------|
| 테이블 생성 | `V2__create_user_profile.sql` |
| 컬럼 추가 | `V3__add_email_to_users.sql` |
| 테이블 삭제 | `V4__drop_old_table.sql` |
| 복합 변경 | `V5__create_orders_add_status_to_users.sql` |

### 동작 원리

1. JPA 엔티티 기반으로 최신 스키마(`create.sql`)를 추출
2. Docker로 임시 PostgreSQL 컨테이너 실행
3. **Baseline DB**: 기존 Flyway 마이그레이션(V1~Vn) 적용
4. **Target DB**: 최신 엔티티 스키마 적용
5. migra를 사용하여 두 DB 간의 차이(Diff)를 SQL로 추출
6. `V{n+1}__{변경내용}.sql` 파일 생성

### 주의사항

> ⚠️ **생성된 SQL 파일은 반드시 검토 후 커밋하세요!**
> - `DROP`, `DELETE` 문이 포함되어 있다면 데이터 손실 위험이 있습니다.
> - FK/PK 제약조건 이름이 달라 불필요한 변경이 발생할 수 있습니다.

### 생성 파일 위치

```
sw-campus-infra/db-postgres/src/main/resources/db/migration/V{n}__{변경내용}.sql
```
