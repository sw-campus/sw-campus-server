# DB Schema Generation Scripts

> Supabase DB에서 스키마를 추출하여 Flyway 마이그레이션 파일을 생성하는 스크립트입니다.

## 스크립트 목록

| 파일 | 설명 |
|------|------|
| `generate-migration.sh` | Supabase에서 DDL을 추출하여 `V1__init_schema.sql` 생성 |
| `verify_check_constraints.py` | CHECK 제약조건 검증 스크립트 |

## 사용법

### 1. generate-migration.sh

Supabase CLI를 사용하여 원격 DB의 스키마를 로컬 마이그레이션 파일로 추출합니다.

```bash
# 실행 전 Supabase CLI 로그인 필요
supabase login

# 스크립트 실행
./generate-migration.sh
```

**출력 위치:**
- `sw-campus-infra/db-postgres/src/main/resources/db/migration/V1__init_schema.sql`

### 2. verify_check_constraints.py

생성된 SQL 파일의 CHECK 제약조건이 올바른지 검증합니다.

```bash
python verify_check_constraints.py
```

## 주의사항

- 이 스크립트들은 **스키마(DDL) 생성 전용**입니다.
- 초기 데이터 마이그레이션은 `../data-migration/` 폴더의 스크립트를 사용하세요.
- Supabase 프로젝트 연결이 필요합니다 (`supabase link`).
