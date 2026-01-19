-- ========================================
-- Community Feature Tables
-- 게시판, 게시글, 댓글, 좋아요, 북마크, 신고, 알림
-- ========================================

-- ========================================
-- 1. Sequences
-- ========================================
CREATE SEQUENCE "swcampus"."board_categories_board_category_id_seq";
CREATE SEQUENCE "swcampus"."posts_post_id_seq";
CREATE SEQUENCE "swcampus"."comments_comment_id_seq";
CREATE SEQUENCE "swcampus"."post_likes_id_seq";
CREATE SEQUENCE "swcampus"."comment_likes_id_seq";
CREATE SEQUENCE "swcampus"."bookmarks_id_seq";
CREATE SEQUENCE "swcampus"."reports_report_id_seq";
CREATE SEQUENCE "swcampus"."notifications_noti_id_seq";

-- ========================================
-- 2. Tables
-- ========================================

-- Board Categories (게시판 분류)
CREATE TABLE "swcampus"."board_categories" (
    "board_category_id" bigint NOT NULL DEFAULT nextval('swcampus.board_categories_board_category_id_seq'),
    "board_category_name" text NOT NULL,
    "board_pid" bigint,
    CONSTRAINT "board_categories_pkey" PRIMARY KEY ("board_category_id")
);

-- Posts (게시글)
CREATE TABLE "swcampus"."posts" (
    "post_id" bigint NOT NULL DEFAULT nextval('swcampus.posts_post_id_seq'),
    "board_category_id" bigint NOT NULL,
    "user_id" bigint NOT NULL,
    "post_title" text NOT NULL,
    "post_body" text NOT NULL,
    "post_images" text[],
    "tags" text[],
    "view_count" bigint NOT NULL DEFAULT 0,
    "like_count" bigint NOT NULL DEFAULT 0,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    "updated_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    "is_deleted" boolean NOT NULL DEFAULT FALSE,
    "selected_comment_id" bigint,
    CONSTRAINT "posts_pkey" PRIMARY KEY ("post_id")
);

-- Comments (댓글)
CREATE TABLE "swcampus"."comments" (
    "comment_id" bigint NOT NULL DEFAULT nextval('swcampus.comments_comment_id_seq'),
    "post_id" bigint NOT NULL,
    "user_id" bigint NOT NULL,
    "comment_pid" bigint,
    "comment_body" text NOT NULL,
    "comment_image_url" text,
    "comment_like_count" bigint NOT NULL DEFAULT 0,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    "updated_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    "is_deleted" boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT "comments_pkey" PRIMARY KEY ("comment_id")
);

-- Post Likes (게시글 추천)
CREATE TABLE "swcampus"."post_likes" (
    "id" bigint NOT NULL DEFAULT nextval('swcampus.post_likes_id_seq'),
    "post_id" bigint NOT NULL,
    "user_id" bigint NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT "post_likes_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "post_likes_user_post_unique" UNIQUE ("user_id", "post_id")
);

-- Comment Likes (댓글 추천)
CREATE TABLE "swcampus"."comment_likes" (
    "id" bigint NOT NULL DEFAULT nextval('swcampus.comment_likes_id_seq'),
    "user_id" bigint NOT NULL,
    "comment_id" bigint NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT "comment_likes_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "comment_likes_user_comment_unique" UNIQUE ("user_id", "comment_id")
);

-- Bookmarks (북마크)
CREATE TABLE "swcampus"."bookmarks" (
    "id" bigint NOT NULL DEFAULT nextval('swcampus.bookmarks_id_seq'),
    "user_id" bigint NOT NULL,
    "post_id" bigint NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT "bookmarks_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "bookmarks_user_post_unique" UNIQUE ("user_id", "post_id")
);

-- Reports (신고)
CREATE TABLE "swcampus"."reports" (
    "report_id" bigint NOT NULL DEFAULT nextval('swcampus.reports_report_id_seq'),
    "user_id" bigint NOT NULL,
    "report_type" text NOT NULL,
    "target_id" bigint NOT NULL,
    "report_reason" text NOT NULL,
    "status" text NOT NULL DEFAULT 'PENDING',
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT "reports_pkey" PRIMARY KEY ("report_id"),
    CONSTRAINT "reports_report_type_check" CHECK ("report_type" IN ('POST', 'COMMENT')),
    CONSTRAINT "reports_status_check" CHECK ("status" IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Notifications (알림)
CREATE TABLE "swcampus"."notifications" (
    "noti_id" bigint NOT NULL DEFAULT nextval('swcampus.notifications_noti_id_seq'),
    "user_id" bigint NOT NULL,
    "sender_id" bigint NOT NULL,
    "target_id" bigint NOT NULL,
    "type" text NOT NULL,
    "is_read" boolean NOT NULL DEFAULT FALSE,
    "created_at" timestamp(6) with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT "notifications_pkey" PRIMARY KEY ("noti_id"),
    CONSTRAINT "notifications_type_check" CHECK ("type" IN ('COMMENT', 'LIKE', 'REPLY', 'ADOPT'))
);

-- ========================================
-- 3. Indexes
-- ========================================

-- Board Categories indexes
CREATE INDEX "idx_board_categories_pid" ON "swcampus"."board_categories" ("board_pid");

-- Posts indexes
CREATE INDEX "idx_posts_board_category_id" ON "swcampus"."posts" ("board_category_id");
CREATE INDEX "idx_posts_user_id" ON "swcampus"."posts" ("user_id");
CREATE INDEX "idx_posts_created_at" ON "swcampus"."posts" ("created_at" DESC);
CREATE INDEX "idx_posts_is_deleted" ON "swcampus"."posts" ("is_deleted");

-- Comments indexes
CREATE INDEX "idx_comments_post_id" ON "swcampus"."comments" ("post_id");
CREATE INDEX "idx_comments_user_id" ON "swcampus"."comments" ("user_id");
CREATE INDEX "idx_comments_comment_pid" ON "swcampus"."comments" ("comment_pid");
CREATE INDEX "idx_comments_is_deleted" ON "swcampus"."comments" ("is_deleted");

-- Post Likes indexes
CREATE INDEX "idx_post_likes_post_id" ON "swcampus"."post_likes" ("post_id");
CREATE INDEX "idx_post_likes_user_id" ON "swcampus"."post_likes" ("user_id");

-- Comment Likes indexes
CREATE INDEX "idx_comment_likes_comment_id" ON "swcampus"."comment_likes" ("comment_id");
CREATE INDEX "idx_comment_likes_user_id" ON "swcampus"."comment_likes" ("user_id");

-- Bookmarks indexes
CREATE INDEX "idx_bookmarks_user_id" ON "swcampus"."bookmarks" ("user_id");
CREATE INDEX "idx_bookmarks_post_id" ON "swcampus"."bookmarks" ("post_id");

-- Reports indexes
CREATE INDEX "idx_reports_user_id" ON "swcampus"."reports" ("user_id");
CREATE INDEX "idx_reports_status" ON "swcampus"."reports" ("status");
CREATE INDEX "idx_reports_target" ON "swcampus"."reports" ("report_type", "target_id");

-- Notifications indexes
CREATE INDEX "idx_notifications_user_id" ON "swcampus"."notifications" ("user_id");
CREATE INDEX "idx_notifications_is_read" ON "swcampus"."notifications" ("is_read");
CREATE INDEX "idx_notifications_created_at" ON "swcampus"."notifications" ("created_at" DESC);

-- ========================================
-- 4. Foreign Key Constraints
-- ========================================

-- Board Categories FK (self-referencing for hierarchy)
ALTER TABLE "swcampus"."board_categories"
    ADD CONSTRAINT "fk_board_categories_parent"
    FOREIGN KEY ("board_pid") REFERENCES "swcampus"."board_categories"("board_category_id") ON DELETE SET NULL;

-- Posts FK
ALTER TABLE "swcampus"."posts"
    ADD CONSTRAINT "fk_posts_board_category"
    FOREIGN KEY ("board_category_id") REFERENCES "swcampus"."board_categories"("board_category_id");

ALTER TABLE "swcampus"."posts"
    ADD CONSTRAINT "fk_posts_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

ALTER TABLE "swcampus"."posts"
    ADD CONSTRAINT "fk_posts_selected_comment"
    FOREIGN KEY ("selected_comment_id") REFERENCES "swcampus"."comments"("comment_id") ON DELETE SET NULL;

-- Comments FK
ALTER TABLE "swcampus"."comments"
    ADD CONSTRAINT "fk_comments_post"
    FOREIGN KEY ("post_id") REFERENCES "swcampus"."posts"("post_id") ON DELETE CASCADE;

ALTER TABLE "swcampus"."comments"
    ADD CONSTRAINT "fk_comments_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

ALTER TABLE "swcampus"."comments"
    ADD CONSTRAINT "fk_comments_parent"
    FOREIGN KEY ("comment_pid") REFERENCES "swcampus"."comments"("comment_id") ON DELETE CASCADE;

-- Post Likes FK
ALTER TABLE "swcampus"."post_likes"
    ADD CONSTRAINT "fk_post_likes_post"
    FOREIGN KEY ("post_id") REFERENCES "swcampus"."posts"("post_id") ON DELETE CASCADE;

ALTER TABLE "swcampus"."post_likes"
    ADD CONSTRAINT "fk_post_likes_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

-- Comment Likes FK
ALTER TABLE "swcampus"."comment_likes"
    ADD CONSTRAINT "fk_comment_likes_comment"
    FOREIGN KEY ("comment_id") REFERENCES "swcampus"."comments"("comment_id") ON DELETE CASCADE;

ALTER TABLE "swcampus"."comment_likes"
    ADD CONSTRAINT "fk_comment_likes_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

-- Bookmarks FK
ALTER TABLE "swcampus"."bookmarks"
    ADD CONSTRAINT "fk_bookmarks_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

ALTER TABLE "swcampus"."bookmarks"
    ADD CONSTRAINT "fk_bookmarks_post"
    FOREIGN KEY ("post_id") REFERENCES "swcampus"."posts"("post_id") ON DELETE CASCADE;

-- Reports FK
ALTER TABLE "swcampus"."reports"
    ADD CONSTRAINT "fk_reports_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

-- Notifications FK
ALTER TABLE "swcampus"."notifications"
    ADD CONSTRAINT "fk_notifications_user"
    FOREIGN KEY ("user_id") REFERENCES "swcampus"."members"("user_id");

ALTER TABLE "swcampus"."notifications"
    ADD CONSTRAINT "fk_notifications_sender"
    FOREIGN KEY ("sender_id") REFERENCES "swcampus"."members"("user_id");

-- ========================================
-- 5. Seed Data (기본 게시판 카테고리)
-- ========================================
-- 상위 카테고리: 취준생, 현직자
-- 최상위 카테고리: 커뮤니티
INSERT INTO "swcampus"."board_categories" ("board_category_id", "board_category_name", "board_pid") VALUES
    (10, '커뮤니티', NULL);

-- 2단계 카테고리: 취준생, 현직자
INSERT INTO "swcampus"."board_categories" ("board_category_id", "board_category_name", "board_pid") VALUES
    (1, '취준생', 10),
    (2, '현직자', 10);

-- 3단계 카테고리: 자유게시판
INSERT INTO "swcampus"."board_categories" ("board_category_id", "board_category_name", "board_pid") VALUES
    (3, '자유게시판', 1),  -- 커뮤니티 > 취준생 > 자유게시판
    (4, '자유게시판', 2);  -- 커뮤니티 > 현직자 > 자유게시판

-- Sequence 값 업데이트 (수동 삽입된 ID 이후부터 시작)
SELECT setval('swcampus.board_categories_board_category_id_seq', 11, true);
