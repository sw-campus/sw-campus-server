-- ========================================
-- Survey System Refactoring
-- 기존 6개 컬럼 구조 → JSONB + 문항 관리 테이블 구조
-- ========================================

-- ========================================
-- 1. member_surveys 테이블 수정
-- ========================================

-- 기존 컬럼 삭제 (데이터 삭제 - spec.md 설계 결정 참조)
ALTER TABLE swcampus.member_surveys
    DROP COLUMN IF EXISTS major,
    DROP COLUMN IF EXISTS bootcamp_completed,
    DROP COLUMN IF EXISTS wanted_jobs,
    DROP COLUMN IF EXISTS licenses,
    DROP COLUMN IF EXISTS has_gov_card,
    DROP COLUMN IF EXISTS affordable_amount;

-- JSONB 컬럼 추가
ALTER TABLE swcampus.member_surveys
    ADD COLUMN basic_survey JSONB,
    ADD COLUMN aptitude_test JSONB,
    ADD COLUMN results JSONB;

-- 인덱싱용 컬럼 추가 (AI 추천 쿼리 성능)
ALTER TABLE swcampus.member_surveys
    ADD COLUMN aptitude_grade VARCHAR(20),
    ADD COLUMN recommended_job VARCHAR(20),
    ADD COLUMN aptitude_score INTEGER,
    ADD COLUMN question_set_version INTEGER,
    ADD COLUMN completed_at TIMESTAMP(6);

-- AI 추천용 인덱스
CREATE INDEX idx_member_surveys_aptitude_grade ON swcampus.member_surveys(aptitude_grade);
CREATE INDEX idx_member_surveys_recommended_job ON swcampus.member_surveys(recommended_job);

-- ========================================
-- 2. 설문 세트 테이블 (버전 관리)
-- ========================================
CREATE TABLE swcampus.survey_question_sets (
    question_set_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP(6) DEFAULT NOW(),
    updated_at TIMESTAMP(6) DEFAULT NOW(),
    published_at TIMESTAMP(6),
    CONSTRAINT uk_question_set_type_version UNIQUE (type, version)
);

-- type 체크 제약조건
ALTER TABLE swcampus.survey_question_sets ADD CONSTRAINT survey_question_sets_type_check
    CHECK (type IN ('BASIC', 'APTITUDE'));

-- status 체크 제약조건
ALTER TABLE swcampus.survey_question_sets ADD CONSTRAINT survey_question_sets_status_check
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

-- 인덱스
CREATE INDEX idx_survey_question_sets_type_status ON swcampus.survey_question_sets(type, status);

-- ========================================
-- 3. 개별 문항 테이블
-- ========================================
CREATE TABLE swcampus.survey_questions (
    question_id BIGSERIAL PRIMARY KEY,
    question_set_id BIGINT NOT NULL,
    question_order INTEGER NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT true,
    field_key VARCHAR(50),
    parent_question_id BIGINT,
    show_condition JSONB,
    metadata JSONB,
    part VARCHAR(20),
    created_at TIMESTAMP(6) DEFAULT NOW(),
    CONSTRAINT fk_survey_questions_set FOREIGN KEY (question_set_id)
        REFERENCES swcampus.survey_question_sets(question_set_id) ON DELETE CASCADE,
    CONSTRAINT fk_survey_questions_parent FOREIGN KEY (parent_question_id)
        REFERENCES swcampus.survey_questions(question_id) ON DELETE SET NULL
);

-- question_type 체크 제약조건
ALTER TABLE swcampus.survey_questions ADD CONSTRAINT survey_questions_type_check
    CHECK (question_type IN ('TEXT', 'RADIO', 'CHECKBOX', 'RANGE', 'CONDITIONAL'));

-- part 체크 제약조건 (NULL 허용 - 기초 설문은 part 없음)
ALTER TABLE swcampus.survey_questions ADD CONSTRAINT survey_questions_part_check
    CHECK (part IS NULL OR part IN ('PART1', 'PART2', 'PART3'));

-- 인덱스
CREATE INDEX idx_survey_questions_set_id ON swcampus.survey_questions(question_set_id);
CREATE INDEX idx_survey_questions_order ON swcampus.survey_questions(question_set_id, question_order);

-- ========================================
-- 4. 선택지 테이블
-- ========================================
CREATE TABLE swcampus.survey_options (
    option_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_order INTEGER NOT NULL,
    option_text VARCHAR(200) NOT NULL,
    option_value VARCHAR(100),
    score INTEGER DEFAULT 0,
    job_type VARCHAR(10),
    is_correct BOOLEAN DEFAULT false,
    CONSTRAINT fk_survey_options_question FOREIGN KEY (question_id)
        REFERENCES swcampus.survey_questions(question_id) ON DELETE CASCADE
);

-- job_type 체크 제약조건 (NULL 허용 - Part3 문항만 사용)
ALTER TABLE swcampus.survey_options ADD CONSTRAINT survey_options_job_type_check
    CHECK (job_type IS NULL OR job_type IN ('F', 'B', 'D'));

-- 인덱스
CREATE INDEX idx_survey_options_question_id ON swcampus.survey_options(question_id);
CREATE INDEX idx_survey_options_order ON swcampus.survey_options(question_id, option_order);
