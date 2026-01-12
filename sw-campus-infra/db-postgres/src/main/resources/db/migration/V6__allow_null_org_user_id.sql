-- 기관 회원 탈퇴 시 기관은 유지하고 user_id만 null로 설정하기 위해 NOT NULL 제약조건 제거
ALTER TABLE organizations ALTER COLUMN user_id DROP NOT NULL;

-- Drop unused status column from certificates table
-- This column was always set to 'SUCCESS' and never used in business logic
-- The actual status is managed by approval_status column (PENDING, APPROVED, REJECTED)
ALTER TABLE certificates DROP COLUMN IF EXISTS status;
