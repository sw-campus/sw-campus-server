# GitHub Copilot Instructions for sw-campus-server

> 이 문서는 GitHub Copilot이 PR 리뷰 및 코드 작성 시 참고하는 지침입니다.

## ⚠️ 중요: 언어 설정

- **모든 PR 리뷰 코멘트는 반드시 한국어로 작성해주세요.**
- **코드 제안, 설명, 피드백 등 모든 응답은 한국어로 작성합니다.**

---

## 프로젝트 개요

- **프로젝트**: sw-campus-server (교육 플랫폼 백엔드)
- **기술 스택**: Spring Boot 3.x, Java 17, PostgreSQL, Gradle Multi-Module
- **아키텍처**: Multi Module + Layer Architecture

---

## 모듈 구조

```
sw-campus-server/
├── sw-campus-api/        # Presentation Layer (Controller, DTO)
├── sw-campus-domain/     # Business Logic Layer (Service, Domain, Repository Interface)
├── sw-campus-infra/      # Infrastructure Layer
│   └── db-postgres/      # JPA Entity, Repository 구현체
└── sw-campus-shared/     # Cross-cutting Concerns (Logging, 공통 예외)
```

---

## PR 리뷰 중점 사항

### 1. 코드 스타일/네이밍 컨벤션 (높은 우선순위)

#### 클래스 네이밍
| 모듈 | 패턴 | 예시 |
|------|------|------|
| api | `{Domain}Controller` | `UserController` |
| api | `{Action}{Domain}Request` | `CreateUserRequest` |
| api | `{Domain}Response` | `UserResponse` |
| domain | `{Domain}` | `User` |
| domain | `{Domain}Service` | `UserService` |
| domain | `{Domain}Repository` | `UserRepository` |
| infra | `{Domain}Entity` | `UserEntity` |
| infra | `{Domain}JpaRepository` | `UserJpaRepository` |
| infra | `{Domain}EntityRepository` | `UserEntityRepository` |

#### 메서드 네이밍
- Controller: `getUser()`, `getUserList()`, `createUser()`, `updateUser()`, `deleteUser()`
- Service: `getUser()`, `findUserByEmail()`, `createUser()`, `updateUser()`, `deleteUser()`
- Repository: `findById()`, `findByEmail()`, `existsByEmail()`, `save()`, `delete()`

#### 변수 네이밍
- camelCase 사용
- Boolean: `is`, `has`, `can` 접두사 (예: `isActive`, `hasPermission`)
- 의미 없는 이름 금지 (`s`, `temp`, `flag`, `list`)

### 2. 아키텍처/레이어 분리 (높은 우선순위)

#### 의존성 방향
```
api → domain ← infra
         ↑
      shared
```

#### 금지 사항
- ❌ `infra` → `api` 의존
- ❌ `domain` → `api` 또는 `infra` 의존
- ❌ `api`에서 `infra` 패키지 직접 import
- ❌ Controller에 비즈니스 로직 작성
- ❌ Domain 모듈에 JPA Entity 배치

#### 필수 분리
- Repository 인터페이스: `domain` 모듈
- Repository 구현체: `infra` 모듈
- Domain 객체와 JPA Entity 분리

### 3. YAGNI 원칙 (중간 우선순위)

#### 확인 사항
- "나중에 쓸 것 같아서" 미리 만든 코드가 있는가?
- 현재 요구사항에 필요하지 않은 추상화가 있는가?
- 실제로 사용되지 않는 메서드/필드가 있는가?

#### 예외 (허용)
- 보안 관련 코드
- 명확한 로드맵에 있는 기능
- 아키텍처 결정 (모듈 구조)

### 4. 중복 코드 검사 (약한 수준)

#### 허용하는 중복
- 레이어 간 DTO 중복 (Request, Response, Entity, Domain)
- 모듈 간 유사 코드
- 도메인 간 유사 로직

#### 지적해야 하는 중복
- 동일 모듈 내 완전히 동일한 코드 복붙
- 동일 비즈니스 규칙이 여러 곳에 하드코딩

### 5. API 설계 규칙 (중간 우선순위)

#### URL 설계
- 소문자, 복수형, 케밥 케이스
- 예: `/api/v1/users`, `/api/v1/user-profiles`

#### HTTP Method
- GET: 조회
- POST: 생성
- PUT: 전체 수정
- PATCH: 부분 수정
- DELETE: 삭제

#### Status Code
- 200: 조회/수정 성공
- 201: 생성 성공
- 204: 삭제 성공
- 400: 잘못된 요청
- 401: 인증 필요
- 403: 권한 없음
- 404: 리소스 없음
- 409: 충돌

#### Controller 규칙
- `@Valid` 사용 필수
- `ResponseEntity` 사용
- 비즈니스 로직 금지

---

## 예외 처리 규칙

- 도메인별 예외는 `BusinessException` 상속
- 일반 `Exception` 던지기 금지
- 에러 메시지에 민감 정보 포함 금지

---

## 테스트 정책

- Domain 레이어: 테스트 권장 (TDD)
- API/Infra 레이어: 선택적
- 목표 커버리지: 90%

---

## 리뷰 시 확인 체크리스트

```
□ 네이밍 컨벤션 준수 (클래스, 메서드, 변수)
□ 의존성 방향 준수 (api → domain ← infra)
□ Controller에 비즈니스 로직 없음
□ Repository 인터페이스와 구현체 분리
□ YAGNI - 불필요한 코드 없음
□ 동일 모듈 내 중복 코드 없음
□ API URL 설계 규칙 준수
□ 적절한 HTTP Method 사용
□ 적절한 Status Code 반환
□ @Valid 사용
```

---

## 코드 규칙 문서 참조

상세 규칙은 `sw-campus-docs/code-rules/` 참조:
- 01-module-structure.md
- 02-naming-convention.md
- 03-dependency-rules.md
- 04-api-design.md
- 05-exception-handling.md
- 06-design-principles.md
