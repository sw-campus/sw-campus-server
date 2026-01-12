-- Drop unused status column from certificates table
-- This column was always set to 'SUCCESS' and never used in business logic
-- The actual status is managed by approval_status column (PENDING, APPROVED, REJECTED)

ALTER TABLE certificates DROP COLUMN IF EXISTS status;
