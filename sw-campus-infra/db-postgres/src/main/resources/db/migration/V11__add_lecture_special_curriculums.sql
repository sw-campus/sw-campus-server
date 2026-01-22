-- V11: Add lecture special curriculums table
-- 강의별 특화 커리큘럼 기능 추가 (최대 5개, 제목만)

-- Sequence 생성
CREATE SEQUENCE swcampus.lecture_special_curriculums_special_curriculum_id_seq;

-- Table 생성
CREATE TABLE swcampus.lecture_special_curriculums (
    special_curriculum_id BIGINT NOT NULL DEFAULT nextval('swcampus.lecture_special_curriculums_special_curriculum_id_seq'),
    lecture_id BIGINT NOT NULL,
    title VARCHAR(20) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT lecture_special_curriculums_pkey PRIMARY KEY (special_curriculum_id),
    CONSTRAINT fk_lecture_special_curriculums_lecture FOREIGN KEY (lecture_id)
        REFERENCES swcampus.lectures(lecture_id) ON DELETE CASCADE
);

-- Index 생성
CREATE INDEX idx_lecture_special_curriculums_lecture_id
    ON swcampus.lecture_special_curriculums(lecture_id);

COMMENT ON TABLE swcampus.lecture_special_curriculums IS '강의별 특화 커리큘럼 (최대 5개, 제목만)';
COMMENT ON COLUMN swcampus.lecture_special_curriculums.special_curriculum_id IS '특화 커리큘럼 ID';
COMMENT ON COLUMN swcampus.lecture_special_curriculums.lecture_id IS '강의 ID';
COMMENT ON COLUMN swcampus.lecture_special_curriculums.title IS '커리큘럼 제목 (최대 20자)';
COMMENT ON COLUMN swcampus.lecture_special_curriculums.sort_order IS '정렬 순서';
