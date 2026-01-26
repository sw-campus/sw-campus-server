-- 회원 탈퇴 시 작성자를 NULL로 설정하기 위한 FK 제약 변경

-- posts 테이블: NOT NULL 제약 제거 후 FK 변경
ALTER TABLE posts ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE posts DROP CONSTRAINT IF EXISTS fk_posts_user;
ALTER TABLE posts ADD CONSTRAINT fk_posts_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- comments 테이블
ALTER TABLE comments ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE comments DROP CONSTRAINT IF EXISTS fk_comments_user;
ALTER TABLE comments ADD CONSTRAINT fk_comments_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- reviews 테이블
ALTER TABLE reviews ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_reviews_user;
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- certificates 테이블
ALTER TABLE certificates ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE certificates DROP CONSTRAINT IF EXISTS fk_certificates_user;
ALTER TABLE certificates ADD CONSTRAINT fk_certificates_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- post_likes 테이블
ALTER TABLE post_likes ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS fk_post_likes_user;
ALTER TABLE post_likes ADD CONSTRAINT fk_post_likes_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- comment_likes 테이블
ALTER TABLE comment_likes ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE comment_likes DROP CONSTRAINT IF EXISTS fk_comment_likes_user;
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_likes_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE SET NULL;

-- notifications 테이블
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notifications_user;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user
    FOREIGN KEY (user_id) REFERENCES members(user_id) ON DELETE CASCADE;

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notifications_sender;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_sender
    FOREIGN KEY (sender_id) REFERENCES members(user_id) ON DELETE CASCADE;
