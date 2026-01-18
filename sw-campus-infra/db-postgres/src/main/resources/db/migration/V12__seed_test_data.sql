-- ========================================
-- Test Data Seeding
-- 테스트용 사용자 및 게시글 데이터 삽입
-- ========================================

-- 1. Test User (테스트 유저)
-- 이미 존재하는 경우(email 기준) 삽입하지 않음
INSERT INTO "swcampus"."members" ("user_id", "email", "password", "name", "nickname", "role", "created_at", "updated_at")
VALUES (101, 'testuser@example.com', '$2a$10$testpasswordhashtestpasswordhash', '테스트유저', 'TestUser', 'USER', NOW(), NOW())
ON CONFLICT ("email") DO NOTHING;

-- 시퀀스 업데이트 (안전을 위해)
SELECT setval('swcampus.members_user_id_seq', GREATEST(102, nextval('swcampus.members_user_id_seq')), false);

-- 2. Test Posts (테스트 게시글)
-- 2.1. 취준생 > 자유게시판 (Category ID: 3)
INSERT INTO "swcampus"."posts" ("post_id", "board_category_id", "user_id", "post_title", "post_body", "created_at", "updated_at")
VALUES
    (101, 3, 101, '취준생 자유게시판 첫 번째 글', '<p>안녕하세요, 테스트 글입니다.</p>', NOW(), NOW()),
    (102, 3, 101, '면접 후기 공유합니다', '<p>오늘 면접 보고 왔어요.</p>', NOW(), NOW()),
    (103, 3, 101, '자소서 첨삭 부탁드립니다', '<p>열심히 썼는데 봐주세요.</p>', NOW(), NOW());

-- 2.2. 현직자 > 자유게시판 (Category ID: 4)
INSERT INTO "swcampus"."posts" ("post_id", "board_category_id", "user_id", "post_title", "post_body", "created_at", "updated_at")
VALUES
    (104, 4, 101, '현직자 질문 받습니다', '<p>3년차 백엔드 개발자입니다.</p>', NOW(), NOW()),
    (105, 4, 101, '이직 고민이 있습니다', '<p>연봉 협상 어떻게 하나요?</p>', NOW(), NOW());

-- 시퀀스 업데이트
SELECT setval('swcampus.posts_post_id_seq', GREATEST(106, nextval('swcampus.posts_post_id_seq')), false);

-- 3. Test Comments (테스트 댓글)
INSERT INTO "swcampus"."comments" ("comment_id", "post_id", "user_id", "comment_body", "created_at", "updated_at")
VALUES
    (101, 101, 101, '첫 댓글입니다!', NOW(), NOW()),
    (102, 101, 101, '환영합니다~', NOW(), NOW());

-- 시퀀스 업데이트
SELECT setval('swcampus.comments_comment_id_seq', GREATEST(103, nextval('swcampus.comments_comment_id_seq')), false);
