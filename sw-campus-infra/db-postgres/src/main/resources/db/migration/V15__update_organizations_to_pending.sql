-- 기존 시드 데이터(userId=1)의 기관을 PENDING으로 변경
-- 관리자가 승인할 때 해당 기관에 사용자를 매핑하기 위함
UPDATE swcampus.organizations
SET approval_status = 'PENDING', updated_at = NOW()
WHERE user_id = 1 AND approval_status = 'APPROVED';
