-- Test Members SQL
-- WARNING: This is for testing purposes only. DO NOT use in production.
-- Run this AFTER Flyway migrations have been applied.

-- ========================================
-- 1. Test Users (일반 회원 2명)
-- ========================================
-- Password: admin123 (bcrypt hashed)
INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password, nickname, phone)
VALUES
    (2, 'user1@test.com', '김철수', 'USER', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', '철수', '010-1234-5678'),
    (3, 'user2@test.com', '이영희', 'USER', NOW(), NOW(), '$2b$10$4qgwq7HZkrt87Y3s6WL3NuPTUXlIDjwFQ7GorV3nxG3d6.Zyj3pSC', '영희', '010-2345-6789')
ON CONFLICT DO NOTHING;

-- ========================================
-- 2. Organization Members (기업 회원 2명)
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
-- 3. Update Organizations with user_id
-- ========================================
UPDATE swcampus.organizations SET user_id = 4, approval_status = 'APPROVED' WHERE org_id = 28;
UPDATE swcampus.organizations SET user_id = 5, approval_status = 'APPROVED' WHERE org_id = 102;

-- ========================================
-- 4. Member Survey (테스트용)
-- ========================================
INSERT INTO swcampus.member_surveys (user_id, major, bootcamp_completed, has_gov_card, affordable_amount, wanted_jobs, licenses, created_at, updated_at)
VALUES
    (3, '컴퓨터공학과', true, true, 500000, '백엔드 개발자, 풀스택 개발자', 'SQLD, 정보처리기사', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ========================================
-- 5. Update Sequence
-- ========================================
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
