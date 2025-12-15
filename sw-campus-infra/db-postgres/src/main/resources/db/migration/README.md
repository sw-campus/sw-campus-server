# Database Migrations

이 디렉토리는 Flyway를 사용한 데이터베이스 마이그레이션 파일을 관리합니다.

## 파일 명명 규칙

```
V{버전번호}__{설명}.sql
```

예시:
- `V1__init_schema.sql`
- `V2__add_user_table.sql`
- `V3__add_index_to_users.sql`

## 사용 방법

### 1. 로컬에서 마이그레이션 파일 생성

```bash
# 새로운 마이그레이션 파일 생성
touch src/main/resources/db/migration/V{다음버전}__{설명}.sql
```

### 2. 마이그레이션 파일 작성

SQL 파일에 DDL 또는 DML을 작성합니다.

```sql
-- V2__add_user_table.sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Git에 커밋 및 푸시

```bash
git add src/main/resources/db/migration/V2__add_user_table.sql
git commit -m "feat: add user table"
git push origin develop
```

### 4. 자동 적용

- **로컬**: Spring Boot 애플리케이션 실행 시 자동으로 마이그레이션 적용
- **서버**: 애플리케이션 배포 시 자동으로 마이그레이션 적용

## 주의사항

⚠️ **절대 하지 말아야 할 것:**
- `DROP TABLE`, `DROP DATABASE` 같은 파괴적 작업
- `DELETE FROM table` (WHERE 절 없이)
- 기존 마이그레이션 파일 수정 (이미 적용된 파일은 수정하면 안 됨)

✅ **안전한 작업:**
- `CREATE TABLE`, `ALTER TABLE`, `CREATE INDEX` 등
- 새로운 마이그레이션 파일 추가
- `IF NOT EXISTS` 사용 권장

## 마이그레이션 확인

로컬에서 마이그레이션 상태 확인:
```bash
# Spring Boot 실행 후 Flyway가 자동으로 적용
# 또는 Flyway CLI 사용 (별도 설치 필요)
```

## 롤백

Flyway는 기본적으로 롤백을 지원하지 않습니다. 
롤백이 필요한 경우 새로운 마이그레이션 파일로 복구 작업을 수행하세요.

예시:
```sql
-- V4__rollback_user_changes.sql
-- 이전 변경사항을 되돌리는 SQL 작성
```

