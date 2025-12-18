"""
데이터 마이그레이션 스크립트 (통합 CSV 버전)

입력:
- 통합_훈련과정.csv (메인 데이터)
- 메인분류-표 1.csv (카테고리)
- 커리큘럼 CSV 파일들 (16개)

출력:
- V2~V13 SQL 마이그레이션 파일
"""

import os
import pandas as pd
import bcrypt
from utils import clean_str, clean_int, parse_date
from sql_gen import write_sql, generate_insert_sql, generate_sequence_reset_sql

# Constants
STATUS_FINISHED = 'FINISHED'
AUTH_APPROVED = 'APPROVED'
LOC_OFFLINE = 'OFFLINE'

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.abspath(os.path.join(CURRENT_DIR, '../data'))

# 소분류 카테고리명 → 커리큘럼 CSV 파일명 매핑
CATEGORY_TO_CURRICULUM_CSV = {
    '프론트엔드 개발': '웹개발(프론트)-표 1.csv',
    '백엔드 개발': '웹개발(백엔드)-표 1.csv',
    '풀스텍 개발': '웹개발(풀스텍)-표 1.csv',
    '모바일': '모바일-표 1.csv',
    '데이터 분석': '데이터 분석-표 1.csv',
    '데이터 엔지니어': '데이터엔지니어-표 1.csv',
    'AI': 'AI엔지니어-표 1.csv',
    '클라우드': '클라우드-표 1.csv',
    '보안': '보안-표 1.csv',
    '임베디드(IOT)': '임베디드(IOT)-표 1.csv',
    '로봇': '로봇-표 1.csv',
    '게임': '게임-표 1.csv',
    '블록체인': '블록체인-표 1.csv',
    '기획': '기획 -표 1.csv',
    '마케팅': '마케팅-표 1.csv',
    '디자인': '디자인-표 1.csv',
}

# 통합CSV 대분류 → 소분류 카테고리명 매핑
UNIFIED_TO_CATEGORY = {
    '웹개발(프론트엔드)': '프론트엔드 개발',
    '웹개발(백엔드)': '백엔드 개발',
    '백엔드개발': '백엔드 개발',
    '웹개발(풀스택)': '풀스텍 개발',
    '풀스텍 개발': '풀스텍 개발',
    '모바일': '모바일',
    '데이터분석가': '데이터 분석',
    '데이터 분석': '데이터 분석',
    '데이터': '데이터 분석',
    '데이터엔지니어': '데이터 엔지니어',
    '데이터 엔지니어': '데이터 엔지니어',
    'AI': 'AI',
    '클라우드': '클라우드',
    '보안': '보안',
    '임베디드(IoT)': '임베디드(IOT)',
    '임베디드(IOT)': '임베디드(IOT)',
    '로봇': '로봇',
    '게임': '게임',
    '블록체인': '블록체인',
    '기획': '기획',
    '마케팅': '마케팅',
    '디자인': '디자인',
    '메타버스': '모바일',
}


def hash_password(plain_password: str) -> str:
    """bcrypt 비밀번호 해싱"""
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(plain_password.encode('utf-8'), salt)
    return hashed.decode('utf-8')


def load_unified_csv():
    """통합_훈련과정.csv 로드"""
    csv_path = os.path.join(DATA_DIR, '통합_훈련과정.csv')
    try:
        df = pd.read_csv(csv_path, encoding='utf-8-sig')
    except UnicodeDecodeError:
        df = pd.read_csv(csv_path, encoding='cp949')
    print(f"  통합 CSV: {len(df)} rows loaded")
    return df


def process_admin_user():
    """Step 0: Admin User 생성"""
    print("Processing Step 0: Admin User...")
    sqls = []
    
    admin_password = os.getenv('MIGRATION_ADMIN_PASSWORD')
    if not admin_password:
        raise ValueError("MIGRATION_ADMIN_PASSWORD environment variable is required")
    
    hashed_password = hash_password(admin_password)
    
    cols = ['user_id', 'email', 'name', 'role', 'created_at', 'updated_at', 'password']
    vals = [1, 'admin@swcampus.com', 'Admin', 'ADMIN', 'NOW()', 'NOW()', hashed_password]
    
    sqls.append(generate_insert_sql('members', cols, vals))
    sqls.append(generate_sequence_reset_sql('members', 'user_id'))
    
    write_sql('V2__seed_admin_user.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements")


def process_categories():
    """Step 1: Categories 생성"""
    print("Processing Step 1: Categories...")
    sqls = []
    
    csv_path = os.path.join(DATA_DIR, '메인분류-표 1.csv')
    try:
        with open(csv_path, 'r', encoding='utf-8-sig') as f:
            lines = f.readlines()
    except UnicodeDecodeError:
        with open(csv_path, 'r', encoding='cp949') as f:
            lines = f.readlines()
    
    cat_map = {}
    cat_id = 1
    sort_order = 1
    large_cat_id = None
    
    for line in lines[1:]:  # 헤더 스킵
        parts = [p.strip() for p in line.strip().split(',')]
        if len(parts) < 3:
            continue
        
        large_val = clean_str(parts[0])
        mid_val = clean_str(parts[1])
        small_vals = [clean_str(p) for p in parts[2:] if clean_str(p)]
        
        # 대분류
        if large_val and large_cat_id is None:
            cols = ['category_id', 'pid', 'category_name', 'sort']
            vals = [cat_id, None, large_val, sort_order]
            sqls.append(generate_insert_sql('categories', cols, vals))
            cat_map[large_val] = cat_id
            large_cat_id = cat_id
            cat_id += 1
            sort_order += 1
            print(f"  대분류: {large_val}")
        
        # 중분류
        if not mid_val:
            continue
        
        cols = ['category_id', 'pid', 'category_name', 'sort']
        vals = [cat_id, large_cat_id, mid_val, sort_order]
        sqls.append(generate_insert_sql('categories', cols, vals))
        cat_map[mid_val] = cat_id
        mid_cat_id = cat_id
        cat_id += 1
        sort_order += 1
        
        # 소분류
        for small_val in small_vals:
            cols = ['category_id', 'pid', 'category_name', 'sort']
            vals = [cat_id, mid_cat_id, small_val, sort_order]
            sqls.append(generate_insert_sql('categories', cols, vals))
            cat_map[small_val] = cat_id
            cat_id += 1
            sort_order += 1
    
    sqls.append(generate_sequence_reset_sql('categories', 'category_id'))
    write_sql('V3__seed_categories.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements")
    return cat_map


def process_organizations(df):
    """Step 2: Organizations 생성"""
    print("Processing Step 2: Organizations...")
    sqls = []
    
    orgs = df['교육기관명'].dropna().apply(clean_str).unique()
    sorted_orgs = sorted([o for o in orgs if o])
    
    org_map = {}
    for idx, org in enumerate(sorted_orgs, 1):
        cols = ['org_id', 'org_name', 'user_id', 'approval_status', 'created_at', 'updated_at']
        vals = [idx, org, 1, 'APPROVED', 'NOW()', 'NOW()']
        sqls.append(generate_insert_sql('organizations', cols, vals))
        org_map[org] = idx
    
    sqls.append(generate_sequence_reset_sql('organizations', 'org_id'))
    write_sql('V4__seed_organizations.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements")
    return org_map


def process_teachers(df):
    """Step 3: Teachers 생성"""
    print("Processing Step 3: Teachers...")
    sqls = []
    
    invalid_names = ['미상', '강사', '확인중', '강사명', 'nan', '', '-']
    teachers = set()
    
    for t in df['강사명'].dropna().unique():
        t_clean = clean_str(t)
        if t_clean.lower() not in [x.lower() for x in invalid_names]:
            teachers.add(t_clean)
    
    sorted_teachers = sorted([t for t in teachers if t])
    
    teacher_map = {}
    for idx, t in enumerate(sorted_teachers, 1):
        cols = ['teacher_id', 'teacher_name']
        vals = [idx, t]
        sqls.append(generate_insert_sql('teachers', cols, vals))
        teacher_map[t] = idx
    
    sqls.append(generate_sequence_reset_sql('teachers', 'teacher_id'))
    write_sql('V5__seed_teachers.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements")
    return teacher_map


def load_curriculum_from_csv(csv_filename):
    """커리큘럼 CSV 로드"""
    csv_path = os.path.join(DATA_DIR, csv_filename)
    if not os.path.exists(csv_path):
        return []
    
    try:
        df = pd.read_csv(csv_path, header=2, encoding='utf-8-sig')
    except UnicodeDecodeError:
        df = pd.read_csv(csv_path, header=2, encoding='cp949')
    except:
        return []
    
    curriculums = []
    for _, row in df.iterrows():
        name = clean_str(row.iloc[0]) if len(row) > 0 else ''
        desc = clean_str(row.iloc[1]) if len(row) > 1 else ''
        if name and name != '분 류':
            curriculums.append({'name': name, 'desc': desc})
    
    return curriculums


def process_curriculums(cat_map):
    """Step 4: Curriculums 생성"""
    print("Processing Step 4: Curriculums...")
    sqls = []
    curr_id = 1
    cat_curr_list_map = {}  # category_name -> [(curr_id, curr_name), ...]
    
    for cat_name, csv_filename in CATEGORY_TO_CURRICULUM_CSV.items():
        cat_id = cat_map.get(cat_name)
        if not cat_id:
            continue
        
        curriculums = load_curriculum_from_csv(csv_filename)
        if not curriculums:
            continue
        
        cat_curr_list_map[cat_name] = []
        
        for curr in curriculums:
            cols = ['curriculum_id', 'category_id', 'curriculum_name', 'curriculum_desc']
            vals = [curr_id, cat_id, curr['name'], curr['desc'] if curr['desc'] else None]
            sqls.append(generate_insert_sql('curriculums', cols, vals))
            cat_curr_list_map[cat_name].append((curr_id, curr['name']))
            curr_id += 1
        
        print(f"  {cat_name}: {len(curriculums)} curriculums")
    
    sqls.append(generate_sequence_reset_sql('curriculums', 'curriculum_id'))
    write_sql('V6__seed_curriculums.sql', sqls)
    print(f"  -> Generated {len(sqls)} SQL statements")
    return cat_curr_list_map


def process_lectures(df, org_map, teacher_map, cat_curr_list_map):
    """Step 5: Lectures & Related Tables 생성"""
    print("Processing Step 5: Lectures...")
    
    sqls_lectures = []
    sqls_steps = []
    sqls_quals = []
    sqls_adds = []
    sqls_teachers = []
    sqls_curriculums = []
    
    lecture_id = 1
    lecture_name_map = {}
    step_id = qual_id = add_id = lt_id = lc_id = 1
    
    for _, row in df.iterrows():
        l_name = clean_str(row.get('훈련과정명'))
        org_name = clean_str(row.get('교육기관명'))
        
        if not l_name or not org_name:
            continue
        
        org_id = org_map.get(org_name)
        if not org_id:
            continue
        
        # 중복 체크
        if l_name in lecture_name_map:
            continue
        lecture_name_map[l_name] = lecture_id
        
        # 매핑
        loc_map = {'온라인': 'ONLINE', '오프라인': LOC_OFFLINE, '혼합': 'MIXED'}
        lecture_loc = loc_map.get(clean_str(row.get('온라인/오프라인')), LOC_OFFLINE)
        
        recruit_map = {'유': 'CARD_REQUIRED', '무': 'GENERAL'}
        recruit_type = recruit_map.get(clean_str(row.get('내일배움카드필요유무')), 'GENERAL')
        
        equip_raw = clean_str(row.get('장비'))
        if 'PC' in equip_raw.upper():
            equip_pc = 'PC'
        elif '개인' in equip_raw:
            equip_pc = 'PERSONAL'
        else:
            equip_pc = 'NONE'
        
        # Boolean
        def is_o(val):
            return clean_str(val).upper() == 'O'
        
        books = is_o(row.get('교재지원'))
        employment_help = is_o(row.get('취업지원_이력서자소서')) or is_o(row.get('취업지원_모의면접'))
        mock_interview = is_o(row.get('취업지원_모의면접'))
        resume = is_o(row.get('취업지원_이력서자소서'))
        after_completion = is_o(row.get('취업지원_수료후사후관리'))
        project_mentor = is_o(row.get('프로젝트_멘토링'))
        
        # Numerics
        lecture_fee = clean_int(row.get('자비부담금'))
        subsidy = clean_int(row.get('수강료지원금'))
        edu_subsidy = clean_int(row.get('훈련수당(월)'))
        total_days = clean_int(row.get('교육일수'))
        total_times = clean_int(row.get('교육시간'))
        project_num = clean_int(row.get('프로젝트_숫자'))
        project_time = clean_int(row.get('프로젝트_기간'))
        
        # Dates
        start_date = parse_date(row.get('교육시작일자'))
        end_date = parse_date(row.get('교육종료일자'))
        deadline_raw = row.get('모집마감일')
        deadline = parse_date(deadline_raw) if deadline_raw and pd.notna(deadline_raw) and clean_str(deadline_raw) else start_date
        
        if not start_date or not end_date:
            continue
        
        # Insert Lecture
        cols = [
            'lecture_id', 'org_id', 'lecture_name', 'lecture_loc', 'recruit_type',
            'start_date', 'end_date', 'deadline', 'total_days', 'total_times',
            'start_time', 'end_time', 'days', 'lecture_fee', 'subsidy', 'edu_subsidy',
            'books', 'employment_help', 'equip_pc', 'status', 'lecture_auth_status',
            'created_at', 'updated_at',
            'goal', 'equip_merit', 'project_num', 'project_time', 'project_team', 'project_tool',
            'project_mentor', 'resume', 'mock_interview', 'after_completion', 'location'
        ]
        vals = [
            lecture_id, org_id, l_name, lecture_loc, recruit_type,
            start_date, end_date, deadline, total_days, total_times,
            '09:00:00', '18:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', lecture_fee, subsidy, edu_subsidy,
            books, employment_help, equip_pc, STATUS_FINISHED, AUTH_APPROVED,
            'NOW()', 'NOW()',
            clean_str(row.get('훈련목표')), None if clean_str(row.get('장점')) == '없음' else clean_str(row.get('장점')),
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
                vals_s = [step_id, lecture_id, step_type, step_order, 'NOW()', 'NOW()']
                sqls_steps.append(generate_insert_sql('lecture_steps', cols_s, vals_s))
                step_id += 1
                step_order += 1
        
        # Quals
        for col_name, q_type in [('지원자격_필수', 'REQUIRED'), ('지원자격_우대', 'PREFERRED')]:
            val = clean_str(row.get(col_name))
            if val:
                for item in val.split(','):
                    item = item.strip()
                    if item:
                        cols_q = ['qual_id', 'lecture_id', 'type', 'text']
                        vals_q = [qual_id, lecture_id, q_type, item]
                        sqls_quals.append(generate_insert_sql('lecture_quals', cols_q, vals_q))
                        qual_id += 1
        
        # Adds
        val_adds = clean_str(row.get('추가혜택'))
        if val_adds and val_adds != '없음':
            for item in val_adds.split(','):
                item = item.strip()
                if item and item != '없음':
                    cols_a = ['add_id', 'lecture_id', 'add_name']
                    vals_a = [add_id, lecture_id, item]
                    sqls_adds.append(generate_insert_sql('lecture_adds', cols_a, vals_a))
                    add_id += 1
        
        # Teachers
        t_name = clean_str(row.get('강사명'))
        if t_name and t_name in teacher_map:
            cols_t = ['id', 'lecture_id', 'teacher_id']
            vals_t = [lt_id, lecture_id, teacher_map[t_name]]
            sqls_teachers.append(generate_insert_sql('lecture_teachers', cols_t, vals_t))
            lt_id += 1
        
        # Curriculums (통합 CSV의 커리큘럼1~11 컬럼 사용)
        unified_cat = clean_str(row.get('대분류'))
        cat_name = UNIFIED_TO_CATEGORY.get(unified_cat)
        
        if cat_name and cat_name in cat_curr_list_map:
            curr_list = cat_curr_list_map[cat_name]
            for i in range(1, 12):
                col_key = f'커리큘럼{i}'
                val = clean_str(row.get(col_key))
                if val in ['기본', '심화']:
                    level = 'BASIC' if val == '기본' else 'ADVANCED'
                    if i - 1 < len(curr_list):
                        curr_id, _ = curr_list[i - 1]
                        cols_lc = ['id', 'lecture_id', 'curriculum_id', 'level']
                        vals_lc = [lc_id, lecture_id, curr_id, level]
                        sqls_curriculums.append(generate_insert_sql('lecture_curriculums', cols_lc, vals_lc))
                        lc_id += 1
        
        lecture_id += 1
    
    # Write SQL files
    write_sql('V7__seed_lectures.sql', sqls_lectures)
    write_sql('V8__seed_lecture_steps.sql', sqls_steps)
    write_sql('V9__seed_lecture_quals.sql', sqls_quals)
    write_sql('V10__seed_lecture_adds.sql', sqls_adds)
    write_sql('V11__seed_lecture_teachers.sql', sqls_teachers)
    write_sql('V12__seed_lecture_curriculums.sql', sqls_curriculums)
    
    print(f"  -> Lectures: {len(sqls_lectures)}")
    print(f"  -> Steps: {len(sqls_steps)}")
    print(f"  -> Quals: {len(sqls_quals)}")
    print(f"  -> Adds: {len(sqls_adds)}")
    print(f"  -> Teachers: {len(sqls_teachers)}")
    print(f"  -> Curriculums: {len(sqls_curriculums)}")
    
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
    print("=" * 60)
    print("Data Migration Script (Unified CSV Version)")
    print("=" * 60)
    
    # 통합 CSV 로드
    df = load_unified_csv()
    
    # Step 0: Admin
    process_admin_user()
    
    # Step 1: Categories
    cat_map = process_categories()
    
    # Step 2: Organizations
    org_map = process_organizations(df)
    
    # Step 3: Teachers
    teacher_map = process_teachers(df)
    
    # Step 4: Curriculums
    cat_curr_list_map = process_curriculums(cat_map)
    
    # Step 5: Lectures
    process_lectures(df, org_map, teacher_map, cat_curr_list_map)
    
    print("=" * 60)
    print("✅ Migration Complete!")
    print("=" * 60)
