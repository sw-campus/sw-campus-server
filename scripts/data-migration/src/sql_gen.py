import os
import datetime

def write_sql(filename, sql_list):
    """
    SQL 문장 리스트를 파일로 저장합니다.
    저장 경로는 ../output/ 입니다.
    """
    # src 디렉토리 기준 상위 output 디렉토리
    current_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(os.path.dirname(current_dir), 'output')
    
    os.makedirs(output_dir, exist_ok=True)
    filepath = os.path.join(output_dir, filename)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(f"-- Auto-generated migration file: {filename}\n")
        f.write(f"-- Generated at: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        for sql in sql_list:
            f.write(sql + "\n")
            
    print(f"✅ Created: {filepath} ({len(sql_list)} statements)")

def generate_insert_sql(table, columns, values):
    """
    단일 INSERT 문을 생성합니다.
    ON CONFLICT DO NOTHING을 포함합니다.
    """
    escaped_values = []
    for v in values:
        if v is None:
            escaped_values.append("NULL")
        elif isinstance(v, str):
            # Special handling for SQL functions passed as strings
            if v.upper() == 'NOW()':
                escaped_values.append("NOW()")
            # Date handling (YYYY-MM-DD)
            elif len(v) == 10 and v[4] == '-' and v[7] == '-':
                 escaped_values.append(f"'{v}'::timestamp")
            else:
                # 싱글 쿼트 이스케이프 (' -> '')
                safe_str = v.replace("\\", "\\\\").replace("'", "''")
                escaped_values.append(f"'{safe_str}'")
        elif isinstance(v, bool):
            escaped_values.append("TRUE" if v else "FALSE")
        else:
            escaped_values.append(str(v))
            
    cols_str = ", ".join(columns)
    vals_str = ", ".join(escaped_values)
    
    return f"INSERT INTO swcampus.{table} ({cols_str}) VALUES ({vals_str}) ON CONFLICT DO NOTHING;"

def generate_sequence_reset_sql(table, id_column, seq_name=None):
    """
    Sequence 값을 테이블의 Max ID 값으로 동기화하는 SQL을 생성합니다.
    """
    if not seq_name:
        seq_name = f"{table}_{id_column}_seq"
    
    # COALESCE(MAX(...), 1) -> 데이터가 없으면 1로 설정 (혹은 0)
    # setval의 두 번째 인자는 nextval이 반환할 값이 아니라 '현재' 값임.
    # 따라서 MAX(id)로 설정하면 다음 nextval은 MAX(id)+1이 됨.
    return f"SELECT setval('swcampus.{seq_name}', (SELECT COALESCE(MAX({id_column}), 1) FROM swcampus.{table}));"
