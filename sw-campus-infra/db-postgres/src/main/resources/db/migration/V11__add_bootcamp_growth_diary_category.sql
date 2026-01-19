-- ========================================
-- Community Feature Tables
-- 게시판 카테고리 추가: 부트캠프 성장일기
-- 게시글 댓글 수 컬럼 추가
-- ========================================

-- 3단계 카테고리: 부트캠프 성장일기 (취준생 하위)
-- 취준생 카테고리 ID: 1
-- 새 카테고리 ID: 5 (V8에서 1,2,3,4,10 사용됨)

INSERT INTO "swcampus"."board_categories" ("board_category_id", "board_category_name", "board_pid") VALUES
    (5, '부트캠프 성장일기', null);

-- 게시글에 댓글 수 컬럼 추가 (비정규화)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS comment_count BIGINT NOT NULL DEFAULT 0;

-- 기존 게시글의 댓글 수 초기화
UPDATE posts p
SET comment_count = (
    SELECT COUNT(*) 
    FROM comments c 
    WHERE c.post_id = p.post_id AND c.is_deleted = false
);

-- 인덱스 추가 (댓글순 정렬 성능)
CREATE INDEX IF NOT EXISTS idx_posts_comment_count ON posts(comment_count DESC);

-- ========================================
-- 고정 게시글(공지) 기능
-- 관리자가 고정한 게시글 상단 표시
-- ========================================

-- 게시글 고정 여부 컬럼 추가
ALTER TABLE swcampus.posts ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN NOT NULL DEFAULT FALSE;

-- 고정 게시글 조회 성능을 위한 부분 인덱스
CREATE INDEX IF NOT EXISTS idx_posts_is_pinned ON swcampus.posts(is_pinned) WHERE is_pinned = true;

COMMENT ON COLUMN swcampus.posts.is_pinned IS '게시글 상단 고정 여부 (관리자만 설정 가능)';
