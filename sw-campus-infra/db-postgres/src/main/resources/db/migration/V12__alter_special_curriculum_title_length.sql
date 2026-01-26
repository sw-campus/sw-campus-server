-- 특화 커리큘럼 제목 컬럼 길이 확장 (20 → 255)
ALTER TABLE lecture_special_curriculums
    ALTER COLUMN title TYPE VARCHAR(255);
