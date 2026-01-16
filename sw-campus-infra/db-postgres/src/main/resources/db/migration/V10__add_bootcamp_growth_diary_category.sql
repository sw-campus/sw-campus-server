-- ========================================
-- Community Feature Tables
-- 게시판 카테고리 추가: 부트캠프 성장일기
-- ========================================

-- 3단계 카테고리: 부트캠프 성장일기 (취준생 하위)
-- 취준생 카테고리 ID: 1
-- 새 카테고리 ID: 5 (V8에서 1,2,3,4,10 사용됨)

INSERT INTO "swcampus"."board_categories" ("board_category_id", "board_category_name", "board_pid") VALUES
    (5, '부트캠프 성장일기', null);
