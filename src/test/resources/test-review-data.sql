-- =====================================================
-- 기관별 후기 페이지네이션 테스트용 데이터
-- org_id: 102, lecture_id: 1
-- =====================================================

-- =====================================================
-- 1. 테스트 회원 15명 생성
-- =====================================================
INSERT INTO members (user_id, email, password, name, nickname, phone, role, org_id, location, created_at, updated_at)
SELECT
    (SELECT COALESCE(MAX(user_id), 0) FROM members) + row_num,
    'test_reviewer_' || LPAD(row_num::text, 2, '0') || '@test.com',
    'password123',
    '테스트유저' || LPAD(row_num::text, 2, '0'),
    '리뷰어' || LPAD(row_num::text, 2, '0'),
    '010-000' || row_num || '-000' || row_num,
    'USER',
    NULL,
    CASE (row_num % 5)
        WHEN 1 THEN '서울'
        WHEN 2 THEN '부산'
        WHEN 3 THEN '대구'
        WHEN 4 THEN '인천'
        ELSE '경기'
    END,
    NOW(),
    NOW()
FROM generate_series(1, 15) AS row_num;

-- 시퀀스 업데이트
SELECT setval('members_user_id_seq', (SELECT MAX(user_id) FROM members));

-- =====================================================
-- 2. 테스트 수료증 15개 생성 (lecture_id = 1)
-- =====================================================
INSERT INTO certificates (certificate_id, user_id, lecture_id, image_url, status, approval_status, created_at, updated_at)
SELECT
    (SELECT COALESCE(MAX(certificate_id), 0) FROM certificates) + ROW_NUMBER() OVER (ORDER BY m.user_id),
    m.user_id,
    1,  -- lecture_id
    'https://test-certificate.com/cert_' || m.user_id || '.jpg',
    'SUCCESS',
    'APPROVED',
    NOW(),
    NOW()
FROM members m
WHERE m.email LIKE 'test_reviewer_%@test.com';

-- 시퀀스 업데이트
SELECT setval('certificates_certificate_id_seq', (SELECT MAX(certificate_id) FROM certificates));

-- =====================================================
-- 3. 테스트 후기 15개 생성 (다양한 점수와 날짜)
-- =====================================================
INSERT INTO reviews (review_id, user_id, lecture_id, certificate_id, comment, score, approval_status, blurred, created_at, updated_at)
SELECT
    (SELECT COALESCE(MAX(review_id), 0) FROM reviews) + ROW_NUMBER() OVER (ORDER BY c.certificate_id),
    c.user_id,
    c.lecture_id,
    c.certificate_id,
    CASE (ROW_NUMBER() OVER (ORDER BY c.certificate_id) % 5)
        WHEN 0 THEN '정말 훌륭한 강의였습니다. 강사님의 설명이 명확하고 실무에 바로 적용할 수 있는 내용이 많았어요. 추천합니다!'
        WHEN 1 THEN '커리큘럼이 체계적이고 프로젝트 경험이 도움이 많이 되었습니다. 취업 준비에 큰 도움이 되었어요.'
        WHEN 2 THEN '시설도 좋고 운영도 잘 되어서 만족스러웠습니다. 다만 수업 시간이 조금 빡빡했어요.'
        WHEN 3 THEN '실무 중심의 교육이라 좋았습니다. 팀 프로젝트를 통해 협업 경험도 쌓을 수 있었어요.'
        ELSE '전반적으로 만족스러운 교육이었습니다. 강사님들이 친절하시고 질문에 잘 답변해주셨어요.'
    END,
    ROUND((3.5 + (RANDOM() * 1.5))::numeric, 1),  -- 3.5 ~ 5.0 랜덤 점수
    'APPROVED',
    false,
    NOW() - ((ROW_NUMBER() OVER (ORDER BY c.certificate_id) * 2) || ' days')::interval,
    NOW() - ((ROW_NUMBER() OVER (ORDER BY c.certificate_id) * 2) || ' days')::interval
FROM certificates c
WHERE c.user_id IN (SELECT user_id FROM members WHERE email LIKE 'test_reviewer_%@test.com');

-- 시퀀스 업데이트
SELECT setval('reviews_review_id_seq', (SELECT MAX(review_id) FROM reviews));

-- =====================================================
-- 4. 각 후기에 대한 상세 점수 생성 (5개 카테고리)
-- =====================================================
INSERT INTO reviews_details (review_detail_id, review_id, category, score, comment)
SELECT
    (SELECT COALESCE(MAX(review_detail_id), 0) FROM reviews_details) + ROW_NUMBER() OVER (),
    r.review_id,
    cat.category,
    ROUND((3.5 + (RANDOM() * 1.5))::numeric, 1),
    CASE cat.category
        WHEN 'TEACHER' THEN '강사님이 친절하고 설명을 잘 해주셨습니다.'
        WHEN 'CURRICULUM' THEN '커리큘럼이 체계적이고 실무에 유용했습니다.'
        WHEN 'MANAGEMENT' THEN '취업 지원이 잘 되어서 좋았습니다.'
        WHEN 'FACILITY' THEN '시설이 깨끗하고 쾌적했습니다.'
        WHEN 'PROJECT' THEN '프로젝트 경험이 매우 유익했습니다.'
    END
FROM reviews r
CROSS JOIN (VALUES ('TEACHER'), ('CURRICULUM'), ('MANAGEMENT'), ('FACILITY'), ('PROJECT')) AS cat(category)
WHERE r.user_id IN (SELECT user_id FROM members WHERE email LIKE 'test_reviewer_%@test.com');

-- 시퀀스 업데이트
SELECT setval('reviews_details_review_detail_id_seq', (SELECT MAX(review_detail_id) FROM reviews_details));

-- =====================================================
-- 확인용 쿼리
-- =====================================================
-- SELECT COUNT(*) FROM reviews WHERE lecture_id = 1 AND approval_status = 'APPROVED';
-- SELECT * FROM reviews WHERE lecture_id = 1 ORDER BY created_at DESC;

-- =====================================================
-- 테스트 데이터 삭제용 (필요시)
-- =====================================================
-- DELETE FROM reviews_details WHERE review_id IN (SELECT review_id FROM reviews WHERE user_id IN (SELECT user_id FROM members WHERE email LIKE 'test_reviewer_%@test.com'));
-- DELETE FROM reviews WHERE user_id IN (SELECT user_id FROM members WHERE email LIKE 'test_reviewer_%@test.com');
-- DELETE FROM certificates WHERE user_id IN (SELECT user_id FROM members WHERE email LIKE 'test_reviewer_%@test.com');
-- DELETE FROM members WHERE email LIKE 'test_reviewer_%@test.com';
