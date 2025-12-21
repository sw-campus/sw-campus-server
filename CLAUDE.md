# CLAUDE.md - Server

This file provides guidance to Claude Code when working with sw-campus-server.

## Project Overview

SW Campus 백엔드 서버 (Spring Boot, Java 17)

## Development Commands

```bash
./gradlew build                    # 빌드
./gradlew bootRun                  # 개발 서버 실행
./gradlew test                     # 전체 테스트
./gradlew :sw-campus-api:test      # API 모듈 테스트
./gradlew :sw-campus-domain:test   # Domain 모듈 테스트
```

## Architecture: Multi Module + Layer

**의존성 방향**: `api → domain ← infra`

```
sw-campus-server/
├── sw-campus-api/        # Presentation (Controller, DTO)
├── sw-campus-domain/     # Business Logic (Service, Repository Interface, Domain POJO)
├── sw-campus-infra/      # Infrastructure
│   ├── db-postgres/      # JPA Entity, Repository 구현체
│   ├── db-redis/         # Redis
│   ├── oauth/            # OAuth Client
│   ├── ocr/              # OCR Client
│   └── s3/               # S3 Storage
└── sw-campus-shared/     # Cross-cutting (Logging)
```

**핵심 규칙**:
- Controller는 `api` 모듈에만, Entity는 `infra` 모듈에만, Domain POJO는 `domain` 모듈에만
- Repository 인터페이스는 `domain`에, 구현체는 `infra`에
- `api → infra` 직접 의존 금지 (runtimeOnly만 허용)
- `domain → api` 또는 `domain → infra` 의존 금지
- Controller에 비즈니스 로직 작성 금지

## Naming Convention

- 패키지: 소문자, 단수형 (`com.swcampus.domain.user`)
- Controller: `{Domain}Controller`
- Service: `{Domain}Service`
- Repository 인터페이스: `{Domain}Repository`
- Repository 구현체: `{Domain}EntityRepository`
- Entity: `{Domain}Entity`
- Request DTO: `{Action}{Domain}Request`
- Response DTO: `{Domain}Response`

## Design Principles

- **YAGNI**: 현재 필요한 것만 구현
- **레이어 간 중복 허용**: api/domain/infra 각 레이어의 DTO/Model은 중복 허용
- **예광탄 개발**: 핵심 기능 먼저 end-to-end 구현
- **Domain 테스트 권장**: domain 레이어는 TDD 권장, api/infra는 선택적
- **Swagger 문서화**: 파일 업로드 시 `@RequestPart` 필수, `@ModelAttribute` 금지

## Code Rules Reference

상세 규칙은 `../sw-campus-docs/code-rules/` 참조:
- `server/01-module-structure.md` ~ `server/07-swagger-documentation.md`

AI 협업 시 `../sw-campus-docs/code-rules/00-index.md` 참조 권장.
