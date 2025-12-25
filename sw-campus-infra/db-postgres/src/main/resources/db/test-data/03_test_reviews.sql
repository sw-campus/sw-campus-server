-- Test Reviews SQL
-- WARNING: This is for testing purposes only. DO NOT use in production.
-- Run AFTER 02_test_certificates.sql

-- ========================================
-- 1. Reviews
-- ========================================
-- 김철수 -> 우아한형제들 강의 리뷰
INSERT INTO swcampus.reviews (review_id, user_id, lecture_id, certificate_id, comment, score, approval_status, blurred, created_at, updated_at)
VALUES
    (1, 2, 66, 1, '우아한테크코스 프론트엔드 과정을 수강하고 정말 많은 것을 배웠습니다. 실무 중심의 커리큘럼과 체계적인 멘토링 덕분에 성장할 수 있었습니다.', 4.8, 'APPROVED', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 이영희 -> 멀티캠퍼스 강의 리뷰
INSERT INTO swcampus.reviews (review_id, user_id, lecture_id, certificate_id, comment, score, approval_status, blurred, created_at, updated_at)
VALUES
    (2, 3, 1, 2, '멀티캠퍼스의 백엔드 과정은 실무에 필요한 기술을 충실히 다루었습니다. 시설도 깨끗하고 강의 자료도 알찼으며, 프로젝트 경험이 특히 유익했습니다.', 4.5, 'APPROVED', false, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ========================================
-- 2. Review Details
-- ========================================
-- 리뷰 1 상세 (김철수 -> 우아한형제들)
INSERT INTO swcampus.reviews_details (review_detail_id, review_id, category, score, comment)
VALUES
    (1, 1, 'TEACHER', 4.9, '강사님들의 실무 경험과 열정이 대단했습니다.'),
    (2, 1, 'CURRICULUM', 4.8, '프론트엔드 기술 스택을 체계적으로 배울 수 있었어요.'),
    (3, 1, 'MANAGEMENT', 4.7, '체계적인 일정 관리와 피드백이 좋았습니다.'),
    (4, 1, 'FACILITY', 4.6, '최신 장비와 쾌적한 환경을 제공해주셨어요.'),
    (5, 1, 'PROJECT', 5.0, '실제 프로젝트 경험이 가장 값진 시간이었습니다.')
ON CONFLICT DO NOTHING;

-- 리뷰 2 상세 (이영희 -> 멀티캠퍼스)
INSERT INTO swcampus.reviews_details (review_detail_id, review_id, category, score, comment)
VALUES
    (6, 2, 'TEACHER', 4.5, '경력이 풍부한 강사님들이 실무 중심으로 가르쳐주셨습니다.'),
    (7, 2, 'CURRICULUM', 4.6, '스프링 부트부터 JPA까지 폭넓게 다뤘어요.'),
    (8, 2, 'MANAGEMENT', 4.4, '전반적으로 관리가 잘 되어 있었습니다.'),
    (9, 2, 'FACILITY', 4.3, '강의실과 PC 사양이 양호했어요.'),
    (10, 2, 'PROJECT', 4.7, '팀 프로젝트를 통해 협업 능력을 기를 수 있었습니다.')
ON CONFLICT DO NOTHING;

-- ========================================
-- 3. Update Sequences
-- ========================================
SELECT setval('swcampus.reviews_review_id_seq', (SELECT COALESCE(MAX(review_id), 1) FROM swcampus.reviews));
SELECT setval('swcampus.reviews_details_review_detail_id_seq', (SELECT COALESCE(MAX(review_detail_id), 1) FROM swcampus.reviews_details));
