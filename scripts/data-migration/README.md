# Data Migration Scripts

> CSV 원본 데이터를 Flyway SQL 마이그레이션 파일로 변환하는 스크립트입니다.

## 폴더 구조

```
data-migration/
├── README.md           # 이 문서
├── requirements.txt    # Python 의존성
├── data/               # 원본 CSV 파일
│   ├── 소프트웨어캠퍼스과정정보.csv  # 메인 과정 정보
│   ├── 웹개발(백엔드).csv           # 카테고리별 커리큘럼
│   └── ...
├── output/             # 생성된 SQL 파일 (gitignore 대상)
│   └── V2__seed_*.sql
└── src/
    ├── convert.py      # 메인 변환 스크립트
    ├── sql_gen.py      # SQL 생성 유틸리티
    └── utils.py        # 공통 유틸리티
```

## 사용법

### 1. 환경 설정

```bash
cd scripts/data-migration

# 가상환경 생성 (권장)
python3 -m venv .venv
source .venv/bin/activate

# 의존성 설치
pip install -r requirements.txt
```

### 2. SQL 파일 생성

```bash
# 환경변수 설정 (필수: 관리자 비밀번호)
export MIGRATION_ADMIN_PASSWORD="your_secure_password"

# 변환 스크립트 실행
python src/convert.py
```

**출력 결과:**
- `output/V2__seed_admin_user.sql`
- `output/V3__seed_categories.sql`
- `output/V4__seed_organizations.sql`
- ...
- `output/V13__reset_sequences.sql`

### 3. Flyway 마이그레이션 적용

```bash
# 생성된 SQL 파일을 마이그레이션 폴더로 복사
cp output/*.sql ../../sw-campus-infra/db-postgres/src/main/resources/db/migration/

# DB 초기화 후 마이그레이션 실행
cd ../..
docker-compose down -v
docker-compose up -d postgres

# Spring Boot 앱 기동 (Flyway 자동 실행)
./gradlew :sw-campus-api:bootRun
```

## 생성되는 테이블

| SQL 파일 | 테이블 | 설명 |
|----------|--------|------|
| V2 | `members` | 관리자 계정 |
| V3 | `categories` | 직무 카테고리 (17개) |
| V4 | `organizations` | 교육기관 (153개) |
| V5 | `teachers` | 강사 정보 |
| V6 | `curriculums` | 커리큘럼 메타데이터 (50개) |
| V7 | `lectures` | 강좌 정보 (389개) |
| V8 | `lecture_steps` | 선발 절차 (313개) |
| V9 | `lecture_quals` | 지원 자격 (558개) |
| V10 | `lecture_adds` | 추가 혜택 |
| V11 | `lecture_teachers` | 강좌-강사 매핑 |
| V12 | `lecture_curriculums` | 강좌-커리큘럼 매핑 (545개) |
| V13 | - | Sequence 리셋 |

## 주의사항

- `MIGRATION_ADMIN_PASSWORD` 환경변수가 설정되지 않으면 스크립트가 실행되지 않습니다.
- `output/` 폴더는 `.gitignore`에 포함되어 있습니다.
- 원본 CSV 파일 수정 시 스크립트를 다시 실행하세요.

## 문제 해결

### CSV 인코딩 오류
원본 CSV 파일이 `cp949`로 인코딩된 경우 자동으로 감지하여 처리합니다.

### 데이터 누락
`지원혜택_추가혜택` 등 일부 컬럼은 원본 데이터가 모두 '없음'인 경우 테이블에 적재되지 않습니다. 이는 정상 동작입니다.
