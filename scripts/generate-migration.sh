#!/bin/bash

# -----------------------------------------------------------------------------
# Flyway 마이그레이션 스크립트 자동 생성기 (Enhanced)
# -----------------------------------------------------------------------------
# Django makemigrations 스타일로 변경 사항을 자동 감지하여 파일명 생성
# 예: V2__create_user_profile_add_column_email.sql
# -----------------------------------------------------------------------------

set -e
set -o pipefail

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정 - 스크립트 위치 기준으로 프로젝트 루트 계산
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MIGRATION_DIR="$PROJECT_ROOT/sw-campus-infra/db-postgres/src/main/resources/db/migration"
CREATE_SQL_PATH="$PROJECT_ROOT/sw-campus-api/create.sql"
SCHEMA_NAME="swcampus"
POSTGRES_VERSION="15"
LOG_FILE="$PROJECT_ROOT/migration-gen.log"

# 로깅 함수
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 스피너 함수 (백그라운드 작업 진행 표시)
spinner() {
    local pid=$1
    local message=$2
    local spinstr='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
    local i=0
    while kill -0 "$pid" 2>/dev/null; do
        local temp=${spinstr:i++%${#spinstr}:1}
        printf "\r${BLUE}[INFO]${NC} %s %s " "$message" "$temp"
        sleep 0.1
    done
    printf "\r\033[K"  # 라인 클리어
}

# SQL diff에서 마이그레이션 이름 자동 생성
generate_migration_name() {
    local sql_file="$1"
    local operations=()
    
    # CREATE TABLE 감지
    while IFS= read -r table; do
        operations+=("create_${table}")
    done < <(grep -ioE 'CREATE TABLE[^(]+' "$sql_file" 2>/dev/null | sed -E 's/CREATE TABLE[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i' | head -3)
    
    # DROP TABLE 감지
    while IFS= read -r table; do
        operations+=("drop_${table}")
    done < <(grep -ioE 'DROP TABLE[^;]+' "$sql_file" 2>/dev/null | sed -E 's/DROP TABLE[[:space:]]+(IF EXISTS[[:space:]]+)?"?([a-zA-Z_]+)"?.*/\2/i' | head -3)
    
    # ADD COLUMN 감지
    while IFS= read -r match; do
        local table=$(echo "$match" | sed -E 's/.*ALTER TABLE[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i')
        local column=$(echo "$match" | sed -E 's/.*ADD COLUMN[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i')
        operations+=("add_${column}_to_${table}")
    done < <(grep -ioE 'ALTER TABLE[^;]+ADD COLUMN[^;]+' "$sql_file" 2>/dev/null | head -3)
    
    # DROP COLUMN 감지
    while IFS= read -r match; do
        local table=$(echo "$match" | sed -E 's/.*ALTER TABLE[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i')
        local column=$(echo "$match" | sed -E 's/.*DROP COLUMN[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i')
        operations+=("drop_${column}_from_${table}")
    done < <(grep -ioE 'ALTER TABLE[^;]+DROP COLUMN[^;]+' "$sql_file" 2>/dev/null | head -3)
    
    # ALTER COLUMN 감지 (타입 변경 등)
    while IFS= read -r match; do
        local table=$(echo "$match" | sed -E 's/.*ALTER TABLE[[:space:]]+"?([a-zA-Z_]+)"?.*/\1/i')
        operations+=("alter_${table}")
    done < <(grep -ioE 'ALTER TABLE[^;]+ALTER COLUMN[^;]+' "$sql_file" 2>/dev/null | head -2)
    
    # CREATE INDEX 감지
    if grep -qiE 'CREATE (UNIQUE )?INDEX' "$sql_file" 2>/dev/null; then
        local idx_count=$(grep -ciE 'CREATE (UNIQUE )?INDEX' "$sql_file" 2>/dev/null || echo "0")
        if [ "$idx_count" -gt 0 ]; then
            operations+=("add_indexes")
        fi
    fi
    
    # 결과 조합
    if [ ${#operations[@]} -eq 0 ]; then
        echo "auto_generated"
    else
        # 최대 3개까지만, 언더스코어로 연결, 소문자로
        local result=$(printf "%s_" "${operations[@]:0:3}" | sed 's/_$//' | tr '[:upper:]' '[:lower:]')
        # 파일명으로 안전하게 (특수문자 제거, 길이 제한)
        echo "$result" | sed 's/[^a-z0-9_]/_/g' | cut -c1-50
    fi
}

# 정리 함수
cleanup() {
    echo ""
    log_info "임시 환경 정리 중..."
    docker rm -f migration-db-gen > /dev/null 2>&1 || true
    docker network rm migration-net > /dev/null 2>&1 || true
    rm -f "$CREATE_SQL_PATH" > /dev/null 2>&1 || true
    rm -f "$MIGRATION_DIR"/.temp_migration_*.sql > /dev/null 2>&1 || true
}
trap cleanup EXIT

# 0. 사전 점검
log_info "사전 점검 중..."
if ! docker info > /dev/null 2>&1; then
    log_error "Docker가 실행 중이지 않습니다. Docker를 먼저 실행해주세요."
    exit 1
fi

# 마이그레이션 디렉토리 존재 확인
if [ ! -d "$MIGRATION_DIR" ]; then
    log_error "마이그레이션 디렉토리가 존재하지 않습니다: $MIGRATION_DIR"
    exit 1
fi

# 다음 버전 번호 계산
get_next_version() {
    local max_version=0
    for file in "$MIGRATION_DIR"/V*.sql; do
        if [ -f "$file" ]; then
            local version=$(basename "$file" | sed -E 's/^V([0-9]+)__.*/\1/')
            if [ "$version" -gt "$max_version" ] 2>/dev/null; then
                max_version=$version
            fi
        fi
    done
    echo $((max_version + 1))
}

NEXT_VERSION=$(get_next_version)
TEMP_OUTPUT_FILE="$MIGRATION_DIR/.temp_migration_v${NEXT_VERSION}.sql"

# V1 생성 여부 확인
IS_FIRST_RUN=false
if [ "$NEXT_VERSION" -eq 1 ]; then
    IS_FIRST_RUN=true
fi

echo "=================================================="
echo "🚀 Flyway 마이그레이션 자동 생성기"
echo "📌 Target Version: V${NEXT_VERSION}"
if [ "$IS_FIRST_RUN" = true ]; then
    echo "📌 Mode: Initial Schema Generation (V1)"
fi
echo "=================================================="

# 1. Docker 환경 구성 (DB 먼저 실행)
log_info "1. 임시 DB 컨테이너 실행 중..."
docker rm -f migration-db-gen > /dev/null 2>&1 || true
docker network rm migration-net > /dev/null 2>&1 || true
docker network create migration-net > /dev/null 2>&1

# 호스트 포트 15432 바인딩 (Hibernate가 실제 Postgres 메타데이터를 읽을 수 있도록)
docker run -d --name migration-db-gen \
  --network migration-net \
  -p 15432:5432 \
  -e POSTGRES_PASSWORD=password \
  postgres:$POSTGRES_VERSION > /dev/null 2>&1

# DB 대기
attempt=0
while [ $attempt -lt 30 ]; do
    if docker exec migration-db-gen pg_isready -U postgres > /dev/null 2>&1; then
        break
    fi
    attempt=$((attempt + 1))
    sleep 1
done

if [ $attempt -eq 30 ]; then
    log_error "DB 시작 시간 초과"
    exit 1
fi

# 2. JPA 엔티티 기반 create.sql 생성 (Real Postgres 연결)
log_info "2. 최신 엔티티 기반 스키마 추출 중 (using Real Postgres)..."
cd "$PROJECT_ROOT"

# 기존 파일 삭제
rm -f "$CREATE_SQL_PATH"

# bootRun을 백그라운드로 실행
# H2 대신 실제 Postgres에 연결하여 정확한 DDL 생성 유도
./gradlew :sw-campus-api:bootRun --args="--spring.profiles.active=ddl-gen --spring.datasource.url=jdbc:postgresql://localhost:15432/postgres --spring.datasource.username=postgres --spring.datasource.password=password --spring.datasource.driver-class-name=org.postgresql.Driver" > "$LOG_FILE" 2>&1 &
GRADLE_PID=$!

# create.sql 파일이 생성될 때까지 대기 (최대 60초)
local_attempt=0
while [ $local_attempt -lt 60 ]; do
    if [ -f "$CREATE_SQL_PATH" ] && [ -s "$CREATE_SQL_PATH" ]; then
        # 파일이 생성되고 내용이 있으면 Gradle 종료
        sleep 1  # 파일 쓰기 완료 대기
        kill $GRADLE_PID 2>/dev/null || true
        wait $GRADLE_PID 2>/dev/null || true
        break
    fi
    printf "\r${BLUE}[INFO]${NC} 2. Gradle 빌드 및 스키마 추출 중... (%ds) " "$local_attempt"
    local_attempt=$((local_attempt + 1))
    sleep 1
done
printf "\r\033[K"  # 라인 클리어

# Gradle이 아직 살아있으면 강제 종료
if kill -0 $GRADLE_PID 2>/dev/null; then
    kill $GRADLE_PID 2>/dev/null || true
    wait $GRADLE_PID 2>/dev/null || true
fi

if [ ! -f "$CREATE_SQL_PATH" ]; then
    log_error "create.sql 생성 실패. 로그: $LOG_FILE"
    exit 1
fi

# 빈 파일 체크 (중요!)
if [ ! -s "$CREATE_SQL_PATH" ]; then
    log_error "생성된 create.sql이 비어있습니다! 설정이나 엔티티 스캔 경로를 확인하세요."
    exit 1
fi
log_success "create.sql 생성 완료 ($(wc -l < "$CREATE_SQL_PATH" | xargs) lines)"

# 3. DB 생성
log_info "3. 비교용 데이터베이스 생성 (Baseline vs Target)..."
docker exec migration-db-gen psql -U postgres -c "CREATE DATABASE baseline;" > /dev/null 2>&1
docker exec migration-db-gen psql -U postgres -c "CREATE DATABASE target;" > /dev/null 2>&1

# 4. Baseline 구성
if [ "$IS_FIRST_RUN" = true ]; then
    log_info "4. Baseline DB 구성 생략 (초기 생성)"
else
    log_info "4. Baseline DB에 기존 마이그레이션 적용 중..."
    if ! docker run --rm --network migration-net \
      -v "$MIGRATION_DIR":/flyway/sql \
      flyway/flyway \
      -url=jdbc:postgresql://migration-db-gen:5432/baseline \
      -user=postgres -password=password \
      -schemas=$SCHEMA_NAME \
      -defaultSchema=$SCHEMA_NAME \
      -connectRetries=60 \
      migrate >> "$LOG_FILE" 2>&1; then
        log_error "Flyway 마이그레이션 적용 실패. 기존 SQL 파일에 문제가 있을 수 있습니다."
        log_error "로그: $LOG_FILE"
        exit 1
    fi
fi

# 5. Target 구성
log_info "5. Target DB에 최신 스키마 적용 중..."
docker cp "$CREATE_SQL_PATH" migration-db-gen:/create.sql > /dev/null 2>&1
docker exec migration-db-gen psql -U postgres -d target -c "CREATE SCHEMA IF NOT EXISTS $SCHEMA_NAME;" > /dev/null 2>&1
if ! docker exec migration-db-gen psql -U postgres -d target -c "SET search_path TO $SCHEMA_NAME;" -f /create.sql >> "$LOG_FILE" 2>&1; then
    log_error "create.sql 적용 실패. JPA 엔티티 설정을 확인하세요."
    log_error "로그: $LOG_FILE"
    exit 1
fi

# 6. Diff 생성 (임시 파일로)
log_info "6. 스키마 차이(Diff) 계산 중..."

# migra를 사용한 스키마 비교 (PostgreSQL 전용, IDENTITY 문제 없음)
docker run --rm --network migration-net \
  python:3.11-slim bash -c "
    pip install migra psycopg2-binary -q 2>/dev/null
    migra --unsafe \
      'postgresql://postgres:password@migration-db-gen:5432/baseline?options=-csearch_path%3D$SCHEMA_NAME' \
      'postgresql://postgres:password@migration-db-gen:5432/target?options=-csearch_path%3D$SCHEMA_NAME' \
      2>/dev/null || true
  " > "$TEMP_OUTPUT_FILE" 2>> "$LOG_FILE"

# 7. 결과 처리
if [ -f "$TEMP_OUTPUT_FILE" ]; then
    # flyway_schema_history 제거
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' '/flyway_schema_history/d' "$TEMP_OUTPUT_FILE"
    else
        sed -i '/flyway_schema_history/d' "$TEMP_OUTPUT_FILE"
    fi
    
    # [Smart Fix] CHECK 제약조건 변경 검증
    # 단순 텍스트 삭제가 아니라, DB의 실제 정의와 비교하여 동일한 경우에만 제거합니다.
    if [ "$IS_FIRST_RUN" != true ]; then
        log_info "🔍 CHECK 제약조건 변경 사항 검증 중..."
        
        SKIPPED_LOG_FILE="$PROJECT_ROOT/skipped_constraints.log"
        
        # 로그 파일 미리 생성 (Docker 볼륨 마운트 시 디렉토리로 생성되는 것 방지)
        touch "$SKIPPED_LOG_FILE"
        
        # Python 검증 스크립트 실행
        docker run --rm --network migration-net \
          -v "$TEMP_OUTPUT_FILE":/migration.sql \
          -v "$SCRIPT_DIR/verify_check_constraints.py":/verify_check_constraints.py \
          -v "$SKIPPED_LOG_FILE":/skipped_constraints.log \
          -e DB_DSN='postgresql://postgres:password@migration-db-gen:5432/baseline' \
          -e MIGRATION_FILE='/migration.sql' \
          -e SKIPPED_LOG_FILE='/skipped_constraints.log' \
          python:3.11-slim bash -c "
            pip install psycopg2-binary -q 2>/dev/null
            python /verify_check_constraints.py
          " 2>&1 | tee -a "$LOG_FILE"
    fi
    
    if [ ! -s "$TEMP_OUTPUT_FILE" ]; then
        log_success "✨ 변경 사항이 없습니다. (DB가 이미 최신 상태입니다)"
        rm "$TEMP_OUTPUT_FILE"
    else
        # 변경 사항 자동 감지하여 파일명 생성
        if [ "$IS_FIRST_RUN" = true ]; then
            MIGRATION_NAME="init_schema"
        else
            MIGRATION_NAME=$(generate_migration_name "$TEMP_OUTPUT_FILE")
        fi
        OUTPUT_FILE="$MIGRATION_DIR/V${NEXT_VERSION}__${MIGRATION_NAME}.sql"
        
        # 기존 파일 존재 확인 (덮어쓰기 방지)
        if [ -f "$OUTPUT_FILE" ]; then
            log_error "파일이 이미 존재합니다: $OUTPUT_FILE"
            rm "$TEMP_OUTPUT_FILE"
            exit 1
        fi
        
        # 임시 파일을 최종 파일로 이동
        mv "$TEMP_OUTPUT_FILE" "$OUTPUT_FILE"
        
        echo ""
        log_success "마이그레이션 파일 생성 완료!"
        echo "📂 파일: $OUTPUT_FILE"
        echo ""
        
        # 위험 키워드 감지
        if grep -q -E "DROP|DELETE" "$OUTPUT_FILE"; then
            log_warn "⚠️  주의: 생성된 파일에 파괴적인 변경(DROP/DELETE)이 포함되어 있습니다!"
            grep -E --color=always "DROP|DELETE" "$OUTPUT_FILE" || true
        fi
        
        echo ""
        echo -e "${YELLOW}💡 Tip: 제약조건(FK, PK) 이름이 달라 불필요한 변경이 발생했는지 확인하세요.${NC}"
    fi
else
    log_error "Diff 생성 실패. 로그: $LOG_FILE"
    exit 1
fi

