-- Auto-generated migration file: V14__seed_test_users_and_reviews.sql
-- 테스트 데이터: 일반 유저 2명, 기업 유저 2명, 리뷰 2개 (각 일반유저가 각 기업의 강의에 1개씩)
-- WARNING: This is for testing purposes only. DO NOT use in production.

-- ========================================
-- 1. 일반 회원 2명 생성
-- ========================================
-- 비밀번호: admin123 (모두 동일)
INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password, nickname, phone)
VALUES 
    (2, 'user1@test.com', '김철수', 'USER', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', '철수', '010-1234-5678'),
    (3, 'user2@test.com', '이영희', 'USER', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', '영희', '010-2345-6789')
ON CONFLICT DO NOTHING;

-- ========================================
-- 2. 기업 회원 2명 생성 (Organizations에 연결)
-- ========================================
-- (주)우아한형제들 담당자
INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password, org_id)
VALUES 
    (4, 'baemin@woowa.com', '우아한담당자', 'ORGANIZATION', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', 28)
ON CONFLICT DO NOTHING;

-- 멀티캠퍼스 담당자
INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password, org_id)
VALUES 
    (5, 'manager@multicampus.com', '멀티캠퍼스담당자', 'ORGANIZATION', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', 102)
ON CONFLICT DO NOTHING;

-- ========================================
-- 2.1 Organizations 테이블의 user_id 업데이트
-- ========================================
-- (주)우아한형제들의 담당자를 user_id 4로 변경
UPDATE swcampus.organizations SET user_id = 4 WHERE org_id = 28;

-- 멀티캠퍼스의 담당자를 user_id 5로 변경
UPDATE swcampus.organizations SET user_id = 5 WHERE org_id = 102;

-- ========================================
-- 3. 수료증 생성 (리뷰 작성을 위해 필요)
-- ========================================
INSERT INTO swcampus.certificates (certificate_id, user_id, lecture_id, status, approval_status, image_url, created_at, updated_at)
VALUES 
    (1, 2, 66, 'COMPLETED', 'APPROVED', 'https://example.com/cert1.jpg', NOW(), NOW()),
    (2, 3, 1, 'COMPLETED', 'APPROVED', 'https://example.com/cert2.jpg', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ========================================
-- 4. 리뷰 생성 (각 일반유저가 각 기업의 강의에 리뷰 1개씩)
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
-- 5. 리뷰 상세 (reviews_details)
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
-- 6. 설문조사 데이터 (이영희)
-- ========================================
INSERT INTO swcampus.member_surveys (user_id, major, bootcamp_completed, has_gov_card, affordable_amount, wanted_jobs, licenses, created_at, updated_at)
VALUES 
    (3, '컴퓨터공학과', true, true, 500000, '백엔드 개발자, 풀스택 개발자', 'SQLD, 정보처리기사', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ========================================
-- 7. Sequence 업데이트
-- ========================================
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
SELECT setval('swcampus.certificates_certificate_id_seq', (SELECT COALESCE(MAX(certificate_id), 1) FROM swcampus.certificates));
SELECT setval('swcampus.reviews_review_id_seq', (SELECT COALESCE(MAX(review_id), 1) FROM swcampus.reviews));
SELECT setval('swcampus.reviews_details_review_detail_id_seq', (SELECT COALESCE(MAX(review_detail_id), 1) FROM swcampus.reviews_details));
