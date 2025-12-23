-- V15__update_default_lecture_images.sql
-- 기존 강의 중 이미지가 없는 강의에 카테고리별 기본 이미지 설정

-- 기본 이미지 base URL (환경에 따라 다름 - staging/local용)
-- prod 환경에서는 별도 마이그레이션 또는 수동 업데이트 필요

WITH category_image_map AS (
    -- 중분류 ID와 기본 이미지 파일명 매핑
    SELECT 2 AS middle_category_id, 'web-development.png' AS image_file UNION ALL
    SELECT 6, 'mobile.png' UNION ALL
    SELECT 8, 'data-ai.png' UNION ALL
    SELECT 12, 'cloud.png' UNION ALL
    SELECT 14, 'security.png' UNION ALL
    SELECT 16, 'embedded-iot.png' UNION ALL
    SELECT 19, 'game-blockchain.png' UNION ALL
    SELECT 22, 'planning-marketing-design.png'
),
lecture_category AS (
    -- 강의별 중분류 ID 조회 (첫 번째 커리큘럼 기준)
    SELECT DISTINCT ON (l.lecture_id)
        l.lecture_id,
        CASE
            WHEN c.pid = 1 THEN c.category_id  -- 이미 중분류인 경우
            ELSE c.pid                          -- 소분류인 경우 부모(중분류) ID
        END AS middle_category_id
    FROM swcampus.lectures l
    JOIN swcampus.lecture_curriculums lc ON l.lecture_id = lc.lecture_id
    JOIN swcampus.curriculums cu ON lc.curriculum_id = cu.curriculum_id
    JOIN swcampus.categories c ON cu.category_id = c.category_id
    WHERE l.lecture_image_url IS NULL OR l.lecture_image_url = ''
    ORDER BY l.lecture_id, lc.id
)
UPDATE swcampus.lectures l
SET lecture_image_url = 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/defaults/' || cim.image_file
FROM lecture_category lc
JOIN category_image_map cim ON lc.middle_category_id = cim.middle_category_id
WHERE l.lecture_id = lc.lecture_id;
