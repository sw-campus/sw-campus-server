import os
import pandas as pd
import bcrypt
from utils import load_csv, clean_str, clean_int, parse_date
from sql_gen import write_sql, generate_insert_sql, generate_sequence_reset_sql

# Constants
STATUS_RECRUITING = 'RECRUITING'
AUTH_APPROVED = 'APPROVED'
LOC_OFFLINE = 'OFFLINE'


# 데이터 디렉토리 설정 (src/../data)
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.abspath(os.path.join(CURRENT_DIR, '../data'))

def hash_password(plain_password: str) -> str:
    """bcrypt를 사용하여 비밀번호 해싱 (Spring Security BCryptPasswordEncoder 호환)"""
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(plain_password.encode('utf-8'), salt)
    return hashed.decode('utf-8')


def process_admin_user():
    """Step 0: Admin User 생성"""
    print("Processing Step 0: Admin User...")
    sqls = []
    
    admin_password = os.getenv('MIGRATION_ADMIN_PASSWORD')
    if not admin_password:
        raise ValueError("MIGRATION_ADMIN_PASSWORD environment variable is required")
    
    # 평문 비밀번호를 bcrypt로 해싱
    hashed_password = hash_password(admin_password)
    
    cols = ['user_id', 'email', 'name', 'role', 'created_at', 'updated_at', 'password']
    vals = [1, 'admin@swcampus.com', 'Admin', 'ADMIN', 'NOW()', 'NOW()', hashed_password]
    
    sqls.append(generate_insert_sql('members', cols, vals))
    sqls.append(generate_sequence_reset_sql('members', 'user_id'))
    
    write_sql('V2__seed_admin_user.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements for Admin User.")

def process_categories(df_main):
    """Step 1: Categories 생성
    
    Args:
        df_main: 통합데이터.csv DataFrame (중복 읽기 방지를 위해 외부에서 전달)
    """
    print("Processing Step 1: Categories...")
    sqls = []
    categories = set()
    
    # 1. 통합데이터.csv에서 대분류 추출
    if df_main is not None and '대분류' in df_main.columns:
        for cat in df_main['대분류'].dropna().unique():
            categories.add(clean_str(cat))
    
    # 2. DATA_DIR 내의 csv 파일명에서 추출 (통합데이터, 과정정보 제외)
    exclude_files = ['통합데이터.csv', '소프트웨어캠퍼스과정정보.csv']
    if os.path.exists(DATA_DIR):
        for f in os.listdir(DATA_DIR):
            if f.endswith('.csv') and f not in exclude_files:
                cat_name = f.replace('.csv', '')
                categories.add(clean_str(cat_name))
    
    # Normalize categories to remove duplicates
    normalized_cats = {}
    for cat in categories:
        if not cat: continue
        # Remove spaces and lowercase for comparison
        norm = cat.replace(' ', '').lower()
        # Manual mapping for specific duplicates
        if norm == '데이터분석': norm = '데이터분석가'
        
        if norm not in normalized_cats:
            normalized_cats[norm] = cat
        else:
            # Prefer the one without spaces if available
            if ' ' in normalized_cats[norm] and ' ' not in cat:
                normalized_cats[norm] = cat

    sorted_cats = sorted(list(normalized_cats.values()))
    
    cat_map = {}
    for idx, cat in enumerate(sorted_cats, 1):
        cols = ['category_id', 'category_name', 'sort']
        vals = [idx, cat, idx]
        sqls.append(generate_insert_sql('categories', cols, vals))
        cat_map[cat] = idx
        
    sqls.append(generate_sequence_reset_sql('categories', 'category_id'))
    write_sql('V3__seed_categories.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements for Categories.")
    return sorted_cats, cat_map

def process_organizations():
    """Step 2: Organizations 생성"""
    print("Processing Step 2: Organizations...")
    sqls = []
    orgs = set()
    
    source_csv = os.path.join(DATA_DIR, '소프트웨어캠퍼스과정정보.csv')
    # Header is on row 3 (index 2)
    try:
        df = pd.read_csv(source_csv, header=2, encoding='utf-8')
    except UnicodeDecodeError:
        print(f"  UTF-8 decoding failed, trying cp949: {source_csv}")
        df = pd.read_csv(source_csv, header=2, encoding='cp949')
    except Exception as e:
        print(f"  Error reading {source_csv}: {e}")
        return {}
    
    if '교육기관명' in df.columns:
        for org in df['교육기관명'].dropna().unique():
            orgs.add(clean_str(org))
            
    sorted_orgs = sorted([o for o in orgs if o])
    
    org_map = {}
    for idx, org in enumerate(sorted_orgs, 1):
        cols = ['org_id', 'org_name', 'user_id', 'approval_status', 'created_at', 'updated_at']
        vals = [idx, org, 1, 1, 'NOW()', 'NOW()']
        sqls.append(generate_insert_sql('organizations', cols, vals))
        org_map[org] = idx
        
    sqls.append(generate_sequence_reset_sql('organizations', 'org_id'))
    print(f"  -> Generated {len(sqls)} SQL statements for Organizations.")
    write_sql('V4__seed_organizations.sql', sqls)
    return org_map

def process_teachers():
    """Step 3: Teachers 생성"""
    print("Processing Step 3: Teachers...")
    sqls = []
    teachers = set()
    
    source_csv = os.path.join(DATA_DIR, '소프트웨어캠퍼스과정정보.csv')
    try:
        df = pd.read_csv(source_csv, header=2, encoding='utf-8')
    except UnicodeDecodeError:
        df = pd.read_csv(source_csv, header=2, encoding='cp949')
    except Exception as e:
        print(f"  Error reading {source_csv}: {e}")
        return {}
    
    invalid_names = ['미상', '강사', '확인중', '강사명', 'nan', '']
    
    if '강사명' in df.columns:
        for t in df['강사명'].dropna().unique():
            t_clean = clean_str(t)
            if t_clean in invalid_names:
                continue
            teachers.add(t_clean)
            
    sorted_teachers = sorted([t for t in teachers if t])
    
    teacher_map = {}
    for idx, t in enumerate(sorted_teachers, 1):
        cols = ['teacher_id', 'teacher_name']
        vals = [idx, t]
        sqls.append(generate_insert_sql('teachers', cols, vals))
        teacher_map[t] = idx
        
    sqls.append(generate_sequence_reset_sql('teachers', 'teacher_id'))
    print(f"  -> Generated {len(sqls)} SQL statements for Teachers.")
    write_sql('V5__seed_teachers.sql', sqls)
    return teacher_map

def process_curriculums(category_list):
    """Step 4: Curriculums 생성"""
    print("Processing Step 4: Curriculums...")
    sqls = []
    
    cat_map = {name: i+1 for i, name in enumerate(category_list)}
    curr_id = 1
    curr_map = {} # name -> id
    cat_curr_list_map = {} # category_name -> [curr_name1, curr_name2, ...]
    
    # 카테고리명과 파일명이 불일치하는 경우만 매핑
    # 매핑에 없는 카테고리는 cat_file_map.get(cat_name, cat_name)에서 
    # cat_name 그대로 파일명으로 사용됨
    # 예: '보안' -> '보안.csv', '기획' -> '기획.csv'
    cat_file_map = {
        '데이터 분석': '데이터분석가',
        '데이터 엔지니어': '데이터엔지니어',
        '백엔드개발': '웹개발(백엔드)',
        '풀스텍 개발': '웹개발(풀스택)',
        '프론트엔드개발': '웹개발(프론트엔드)',
        '임베디드(IOT)': '임베디드(IoT)'
    }
    
    for cat_name, cat_id in cat_map.items():
        fname = cat_file_map.get(cat_name, cat_name)
        csv_path = os.path.join(DATA_DIR, f"{fname}.csv")
        
        if not os.path.exists(csv_path):
            print(f"Warning: CSV for category '{cat_name}' not found at {csv_path}")
            continue
            
        try:
            # utf-8-sig로 BOM 처리, header=None으로 전체 행 읽기
            df_raw = pd.read_csv(csv_path, header=None, encoding='utf-8-sig')
        except UnicodeDecodeError:
            try:
                df_raw = pd.read_csv(csv_path, header=None, encoding='cp949')
            except Exception as e:
                print(f"Error reading {csv_path}: {e}")
                continue
        except Exception as e:
            print(f"Error reading {csv_path}: {e}")
            continue
        
        if len(df_raw) < 2:
            print(f"Warning: {csv_path} has insufficient rows")
            continue
        
        # CSV 구조 자동 감지:
        # Type A/B (정상): Row 0 = 메타헤더("분 류", "커리큘럼"), Row 1 = 커리큘럼 이름들
        # Type C (비정상): Row 0 = 컬럼헤더("훈련기관명"...), 커리큘럼 이름 행 없음
        first_cell = clean_str(df_raw.iloc[0][0]) if not pd.isna(df_raw.iloc[0][0]) else ''
        
        curriculum_names = []
        if first_cell == '훈련기관명':
            # Type C: 비정상 CSV (데이터엔지니어.csv 등)
            # Row 0에 컬럼 헤더가 바로 있음 - 커리큘럼 이름 추출 불가
            # col 3부터 커리큘럼 컬럼 헤더들이 있음 (예: "프로그래밍 언어 핵심", "자료구조 및 알고리즘")
            print(f"  Warning: {fname}.csv has non-standard structure (no curriculum name row)")
            if df_raw.shape[1] > 3:
                for val in df_raw.iloc[0][3:]:  # Row 0의 col 3부터 컬럼 헤더 추출
                    if pd.isna(val): continue
                    val_str = clean_str(val)
                    if val_str and val_str not in ['훈련기관명', '훈련과정명', '대분류', '분 류', '분류', '프로젝트'] and not val_str.isdigit():
                        curriculum_names.append(val_str)
        else:
            # Type A/B: 정상 CSV 구조
            # Row 0: 메타 헤더 (예: "분 류", "커리큘럼")
            # Row 1: 실제 커리큘럼 이름들 (col 3부터)
            # Row 2: 컬럼 헤더 (훈련기관명, 훈련과정명, 대분류, 1, 2, ...)
            if df_raw.shape[1] > 3:
                for val in df_raw.iloc[1][3:]:  # Row 1에서 커리큘럼 이름 추출
                    if pd.isna(val): continue
                    val_str = clean_str(val)
                    # Filter invalid names
                    if val_str and val_str not in ['훈련기관명', '훈련과정명', '대분류', '분 류', '분류', '프로젝트'] and not val_str.isdigit():
                        curriculum_names.append(val_str)
        
        cat_curr_list_map[cat_name] = curriculum_names
        
        for curr_name in curriculum_names:
            cols = ['curriculum_id', 'category_id', 'curriculum_name']
            vals = [curr_id, cat_id, curr_name]
            sqls.append(generate_insert_sql('curriculums', cols, vals))
            curr_map[curr_name] = curr_id 
            curr_id += 1
            
    sqls.append(generate_sequence_reset_sql('curriculums', 'curriculum_id'))
    print(f"  -> Generated {len(sqls)} SQL statements for Curriculums.")
    write_sql('V6__seed_curriculums.sql', sqls)
    return curr_map, cat_curr_list_map

def process_lectures(org_map, teacher_map, curr_map, cat_curr_list_map, cat_map, df_main):
    """Step 5: Lectures & Related Tables 생성
    
    Args:
        df_main: 통합데이터.csv DataFrame (중복 읽기 방지를 위해 외부에서 전달)
    """
    print("Processing Step 5: Lectures & Relations...")
    
    source_csv = os.path.join(DATA_DIR, '소프트웨어캠퍼스과정정보.csv')
    # Multi-header: Row 3 and 4 (indices 2 and 3)
    try:
        df = pd.read_csv(source_csv, header=[2, 3], encoding='utf-8')
    except UnicodeDecodeError:
        df = pd.read_csv(source_csv, header=[2, 3], encoding='cp949')
    except Exception as e:
        print(f"  Error reading {source_csv}: {e}")
        return

    # Flatten columns for easier access
    new_cols = []
    last_c1 = ""
    for col in df.columns:
        c1 = clean_str(col[0])
        c2 = clean_str(col[1])
        
        # Handle merged cells (forward fill c1)
        if c1.startswith('Unnamed') or not c1:
            c1 = last_c1
        else:
            last_c1 = c1
            
        if c2 and not c2.startswith('Unnamed'):
            new_cols.append(f"{c1}_{c2}")
        else:
            new_cols.append(c1)
    df.columns = new_cols
    
    sqls_lectures = []
    sqls_steps = []
    sqls_quals = []
    sqls_adds = []
    sqls_teachers = []
    sqls_curriculums = []
    
    lecture_id = 1
    lecture_name_map = {} # name -> id
    
    # ID sequences for sub-tables
    step_id_seq = 1
    qual_id_seq = 1
    add_id_seq = 1
    lt_id_seq = 1
    lc_id_seq = 1
    
    for _, row in df.iterrows():
        # Basic Info
        l_name = clean_str(row.get('과정명'))
        org_name = clean_str(row.get('교육기관명'))
        start_date_raw = row.get('교육시작일자')
        
        if not l_name or not org_name or not start_date_raw:
            # print(f"Skipping row: Missing required fields (Name: {l_name}, Org: {org_name})")
            continue
            
        org_id = org_map.get(org_name)
        if not org_id:
            # print(f"Skipping row: Org not found ({org_name})")
            continue
            
        # Duplicate check
        if l_name in lecture_name_map:
            print(f"Warning: Duplicate lecture skipped: {l_name}")
            continue
        lecture_name_map[l_name] = lecture_id
        
        # Mappings
        loc_map = {'온라인': 'ONLINE', '오프라인': LOC_OFFLINE, '혼합': 'MIXED'}
        loc_raw = clean_str(row.get('온라인/오프라인'))
        lecture_loc = loc_map.get(loc_raw, LOC_OFFLINE) # Default
        
        recruit_map = {'유': 'CARD_REQUIRED', '무': 'GENERAL'}
        recruit_raw = clean_str(row.get('내일배움카드 필요 유뮤'))
        recruit_type = recruit_map.get(recruit_raw, 'GENERAL')
        
        equip_raw = clean_str(row.get('훈련시설 및 장비_장비'))
        if 'PC' in equip_raw.upper(): equip_pc = 'PC'
        elif '개인' in equip_raw: equip_pc = 'PERSONAL'
        else: equip_pc = 'NONE'
        
        # Booleans
        def is_o(val): return clean_str(val) == 'O'
        
        books = is_o(row.get('훈련시설 및 장비_교재지원'))
        employment_help = (is_o(row.get('취업지원서비스_이력서/자소서')) or 
                           is_o(row.get('취업지원서비스_모의면접')) or 
                           is_o(row.get('취업지원서비스_협력사 매칭')))
        mock_interview = is_o(row.get('취업지원서비스_모의면접'))
        resume = is_o(row.get('취업지원서비스_이력서/자소서'))
        after_completion = is_o(row.get('취업지원서비스_수료 후 사후관리'))
        project_mentor = is_o(row.get('프로젝트_멘토링'))
        
        # Numerics
        price = clean_int(row.get('수강료 합계'))
        subsidy = clean_int(row.get('지원혜택_훈련수당(월)'))
        edu_subsidy = clean_int(row.get('수강료 지원금'))
        total_days = clean_int(row.get('교육일수'))
        total_times = clean_int(row.get('교육시간'))
        project_num = clean_int(row.get('프로젝트_숫자'))
        project_time = clean_int(row.get('프로젝트_기간'))
        
        # Dates
        start_date = parse_date(start_date_raw)
        end_date = parse_date(row.get('교육종료일자'))
        
        if not start_date or not end_date:
             # print(f"Skipping row: Invalid dates ({l_name})")
             continue

        # Insert Lecture
        cols = [
            'lecture_id', 'org_id', 'lecture_name', 'lecture_loc', 'recruit_type',
            'start_date', 'end_date', 'total_days', 'total_times',
            'start_time', 'end_time', 'lecture_fee', 'subsidy', 'edu_subsidy',
            'books', 'employment_help', 'equip_pc', 'status', 'lecture_auth_status',
            'created_at', 'updated_at',
            'goal', 'equip_merit', 'project_num', 'project_time', 'project_team', 'project_tool',
            'project_mentor', 'resume', 'mock_interview', 'after_completion', 'location'
        ]
        
        vals = [
            lecture_id, org_id, l_name, lecture_loc, recruit_type,
            start_date, end_date, total_days, total_times,
            '09:00:00', '18:00:00', price, subsidy, edu_subsidy,
            books, employment_help, equip_pc, STATUS_RECRUITING, AUTH_APPROVED,
            'NOW()', 'NOW()',
            clean_str(row.get('훈련목표')), clean_str(row.get('프로젝트_장점')),
            project_num, project_time, clean_str(row.get('프로젝트_팀구성방식')), clean_str(row.get('프로젝트_협업툴')),
            project_mentor, resume, mock_interview, after_completion, clean_str(row.get('교육장소'))
        ]
        
        sqls_lectures.append(generate_insert_sql('lectures', cols, vals))
        
        # Steps
        step_order = 1
        step_map = {
            '선발절차_서류심사': 'DOCUMENT',
            '선발절차_면접': 'INTERVIEW',
            '선발절차_코딩테스트': 'CODING_TEST',
            '선발절차_사전학습과제': 'PRE_TASK'
        }
        for col_name, step_type in step_map.items():
            if is_o(row.get(col_name)):
                cols_s = ['step_id', 'lecture_id', 'step_type', 'step_order', 'created_at', 'updated_at']
                vals_s = [step_id_seq, lecture_id, step_type, step_order, 'NOW()', 'NOW()']
                sqls_steps.append(generate_insert_sql('lecture_steps', cols_s, vals_s))
                step_id_seq += 1
                step_order += 1
                
        # Quals
        qual_map = {'지원자격_필수': 'REQUIRED', '지원자격_우대': 'PREFERRED'}
        for col_name, q_type in qual_map.items():
            val = clean_str(row.get(col_name))
            if val:
                for item in val.split(','):
                    item = item.strip()
                    if item:
                        cols_q = ['qual_id', 'lecture_id', 'type', 'text']
                        vals_q = [qual_id_seq, lecture_id, q_type, item]
                        sqls_quals.append(generate_insert_sql('lecture_quals', cols_q, vals_q))
                        qual_id_seq += 1
                        
        # Adds
        val_adds = clean_str(row.get('지원혜택_추가혜택'))
        if val_adds and val_adds != '없음':
            for item in val_adds.split(','):
                item = item.strip()
                if item and item != '없음':
                    cols_a = ['add_id', 'lecture_id', 'add_name']
                    # Schema check: lecture_adds has 'add_name' or 'text'?
                    # V1__init_schema.sql: "add_name" character varying(255) not null
                    vals_a = [add_id_seq, lecture_id, item]
                    sqls_adds.append(generate_insert_sql('lecture_adds', cols_a, vals_a))
                    add_id_seq += 1
                    
        # Teachers
        t_name = clean_str(row.get('강사명'))
        if t_name and t_name in teacher_map:
            t_id = teacher_map[t_name]
            cols_t = ['id', 'lecture_id', 'teacher_id']
            vals_t = [lt_id_seq, lecture_id, t_id]
            sqls_teachers.append(generate_insert_sql('lecture_teachers', cols_t, vals_t))
            lt_id_seq += 1
            
        lecture_id += 1

    # Lecture Curriculums
    # df_main은 외부에서 전달받음 (중복 읽기 방지)
    if df_main is not None:
        for _, row in df_main.iterrows():
            l_name = clean_str(row.get('훈련과정명'))
            cat_name = clean_str(row.get('대분류'))
            
            if not l_name or not cat_name: continue
            
            l_id = lecture_name_map.get(l_name)
            if not l_id: continue 
            
            curr_list = cat_curr_list_map.get(cat_name)
            if not curr_list: continue
            
            # Columns 1 to 10
            for i in range(1, 11):
                col_key = str(i)
                if col_key not in row: continue
                
                val = clean_str(row[col_key])
                if val in ['기본', '심화']:
                    level = 'BASIC' if val == '기본' else 'ADVANCED'
                    
                    if i-1 < len(curr_list):
                        curr_name = curr_list[i-1]
                        curr_id = curr_map.get(curr_name)
                        
                        if curr_id:
                            cols_lc = ['id', 'lecture_id', 'curriculum_id', 'level']
                            vals_lc = [lc_id_seq, l_id, curr_id, level]
                            sqls_curriculums.append(generate_insert_sql('lecture_curriculums', cols_lc, vals_lc))
                            lc_id_seq += 1

    # Write SQLs
    print(f"  -> Generated {len(sqls_lectures)} SQL statements for Lectures.")
    write_sql('V7__seed_lectures.sql', sqls_lectures)
    
    print(f"  -> Generated {len(sqls_steps)} SQL statements for Lecture Steps.")
    write_sql('V8__seed_lecture_steps.sql', sqls_steps)
    
    print(f"  -> Generated {len(sqls_quals)} SQL statements for Lecture Quals.")
    write_sql('V9__seed_lecture_quals.sql', sqls_quals)
    
    print(f"  -> Generated {len(sqls_adds)} SQL statements for Lecture Adds.")
    write_sql('V10__seed_lecture_adds.sql', sqls_adds)
    
    print(f"  -> Generated {len(sqls_teachers)} SQL statements for Lecture Teachers.")
    write_sql('V11__seed_lecture_teachers.sql', sqls_teachers)
    
    print(f"  -> Generated {len(sqls_curriculums)} SQL statements for Lecture Curriculums.")
    write_sql('V12__seed_lecture_curriculums.sql', sqls_curriculums)
    
    # Reset sequences
    write_sql('V13__reset_sequences.sql', [
        generate_sequence_reset_sql('lectures', 'lecture_id'),
        generate_sequence_reset_sql('lecture_steps', 'step_id'),
        generate_sequence_reset_sql('lecture_quals', 'qual_id'),
        generate_sequence_reset_sql('lecture_adds', 'add_id'),
        generate_sequence_reset_sql('lecture_teachers', 'id'),
        generate_sequence_reset_sql('lecture_curriculums', 'id')
    ])

if __name__ == "__main__":
    print("Starting convert.py...")
    
    # 통합데이터.csv를 한 번만 읽어서 재사용
    main_csv_path = os.path.join(DATA_DIR, '통합데이터.csv')
    df_main = load_csv(main_csv_path)
    
    process_admin_user()
    categories, cat_map = process_categories(df_main)
    org_map = process_organizations()
    teacher_map = process_teachers()
    if categories:
        curr_map, cat_curr_list_map = process_curriculums(categories)
        process_lectures(org_map, teacher_map, curr_map, cat_curr_list_map, cat_map, df_main)
    else:
        print("Skipping Step 4 & 5 due to no categories found.")
