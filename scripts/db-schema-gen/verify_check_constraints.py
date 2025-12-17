#!/usr/bin/env python3
"""
CHECK 제약조건 변경 검증 스크립트

Hibernate가 생성한 CHECK 제약조건과 PostgreSQL 내부 표현 차이로 인해
migra가 "변경됨"으로 잘못 감지하는 경우를 필터링합니다.

- Enum 값 집합이 동일하면: 불필요한 DROP/ADD/VALIDATE 구문 제거
- Enum 값이 실제로 변경되면: 해당 구문 유지
"""

import os
import re
import sys
from datetime import datetime

# 환경 변수로 설정 가능 (기본값 제공)
DB_DSN = os.environ.get('DB_DSN', 'postgresql://postgres:password@migration-db-gen:5432/baseline')
MIGRATION_FILE = os.environ.get('MIGRATION_FILE', '/migration.sql')
SKIPPED_LOG_FILE = os.environ.get('SKIPPED_LOG_FILE', '/skipped_constraints.log')


def get_db_connection():
    """데이터베이스 연결"""
    try:
        import psycopg2
    except ImportError:
        print("ERROR: psycopg2 not installed", file=sys.stderr)
        sys.exit(1)
    
    try:
        return psycopg2.connect(DB_DSN)
    except Exception as e:
        print(f"ERROR: DB connection failed: {e}", file=sys.stderr)
        sys.exit(1)


def get_constraint_def(conn, schema: str, constraint_name: str) -> str | None:
    """DB에서 제약조건 정의 조회"""
    try:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT pg_get_constraintdef(c.oid)
                FROM pg_constraint c
                JOIN pg_namespace n ON n.oid = c.connamespace
                WHERE n.nspname = %s AND c.conname = %s
            """, (schema, constraint_name))
            row = cur.fetchone()
            return row[0] if row else None
    except Exception as e:
        print(f"  Warning: Failed to get constraint {constraint_name}: {e}", file=sys.stderr)
        return None


def extract_check_values(sql: str) -> set[str]:
    """
    CHECK 제약조건에서 ARRAY 값들만 추출하여 비교
    
    예: CHECK ((status)::text = ANY (ARRAY['PENDING', 'APPROVED']))
    결과: {'PENDING', 'APPROVED'}
    """
    if not sql:
        return set()
    # ARRAY['VALUE1', 'VALUE2', ...] 패턴에서 값만 추출
    matches = re.findall(r"'([A-Z_]+)'", sql, re.IGNORECASE)
    return set(m.upper() for m in matches)


def process_migration_file(migration_file: str, log_file: str) -> None:
    """마이그레이션 파일에서 불필요한 CHECK 제약조건 변경 제거"""
    
    if not os.path.exists(migration_file):
        print("Migration file not found")
        return

    with open(migration_file, 'r') as f:
        lines = f.readlines()

    if not lines:
        return

    # 로그 파일 초기화
    log_entries: list[str] = []
    log_entries.append(f"="*60)
    log_entries.append(f"CHECK 제약조건 검증 결과")
    log_entries.append(f"실행 시각: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    log_entries.append(f"="*60)
    log_entries.append("")

    conn = get_db_connection()
    
    # 정규표현식 패턴 정의
    add_re = re.compile(
        r'alter table "([^"]+)"."([^"]+)" add constraint "([^"]+)" (CHECK.*)', 
        re.IGNORECASE
    )
    validate_re = re.compile(
        r'alter table "([^"]+)"."([^"]+)" validate constraint "([^"]+)"', 
        re.IGNORECASE
    )
    drop_re = re.compile(
        r'alter table "([^"]+)"."([^"]+)" drop constraint "([^"]+)"', 
        re.IGNORECASE
    )
    
    skip_indices: set[int] = set()
    constraints_to_skip: set[tuple[str, str, str]] = set()  # (schema, table, name)
    
    # 1단계: ADD CONSTRAINT 분석
    for i, line in enumerate(lines):
        m = add_re.search(line)
        if m:
            schema, table, name, definition = m.groups()
            
            # CHECK 제약조건만 처리
            if not definition.strip().upper().startswith('CHECK'):
                continue
            
            # DB에서 현재 정의 조회
            current_def = get_constraint_def(conn, schema, name)
            
            if current_def:
                # Enum 값 집합 비교
                new_values = extract_check_values(definition)
                cur_values = extract_check_values(current_def)
                
                # Enum 값 집합이 동일하면 무시 (표현 차이만 있는 경우)
                if new_values == cur_values:
                    log_entries.append(f"[SKIPPED] {schema}.{table}.{name}")
                    log_entries.append(f"  Values: {sorted(new_values)}")
                    log_entries.append("")
                    skip_indices.add(i)
                    constraints_to_skip.add((schema, table, name))
                else:
                    log_entries.append(f"[KEPT - MODIFIED] {schema}.{table}.{name}")
                    log_entries.append(f"  Old values: {sorted(cur_values)}")
                    log_entries.append(f"  New values: {sorted(new_values)}")
                    log_entries.append("")
    
    # 2단계: 연관된 DROP, VALIDATE 구문 제거
    for i, line in enumerate(lines):
        # DROP 구문
        m = drop_re.search(line)
        if m:
            schema, table, name = m.groups()
            if (schema, table, name) in constraints_to_skip:
                skip_indices.add(i)
                continue
        
        # VALIDATE 구문
        m = validate_re.search(line)
        if m:
            schema, table, name = m.groups()
            if (schema, table, name) in constraints_to_skip:
                skip_indices.add(i)

    conn.close()
    
    # 3단계: 통계 및 파일 재작성
    log_entries.append(f"="*60)
    log_entries.append(f"Summary")
    log_entries.append(f"="*60)
    
    if skip_indices:
        skipped_count = len(constraints_to_skip)
        log_entries.append(f"Skipped constraints: {skipped_count}")
        log_entries.append(f"Removed lines: {len(skip_indices)}")
        
        with open(migration_file, 'w') as f:
            for i, line in enumerate(lines):
                if i not in skip_indices:
                    f.write(line)
        
        # 빈 줄만 남은 경우 파일 비우기
        with open(migration_file, 'r') as f:
            content = f.read().strip()
        if not content:
            log_entries.append("Result: File is now empty (all changes were false positives)")
            with open(migration_file, 'w') as f:
                pass  # 빈 파일
        else:
            log_entries.append("Result: Some real changes remain in the file")
        
        # 콘솔에는 간단한 요약만 출력
        print(f"  - Skipped {skipped_count} identical constraints, removed {len(skip_indices)} lines")
        print(f"  - Details: {log_file}")
    else:
        log_entries.append("Skipped constraints: 0")
        log_entries.append("Result: No identical constraints found")
        print("  - No identical constraints found to skip")
    
    # 로그 파일 작성
    with open(log_file, 'w') as f:
        f.write('\n'.join(log_entries))


def main():
    """메인 진입점"""
    migration_file = sys.argv[1] if len(sys.argv) > 1 else MIGRATION_FILE
    log_file = sys.argv[2] if len(sys.argv) > 2 else SKIPPED_LOG_FILE
    process_migration_file(migration_file, log_file)


if __name__ == "__main__":
    main()
