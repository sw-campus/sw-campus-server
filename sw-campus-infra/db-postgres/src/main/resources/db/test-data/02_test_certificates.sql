-- Test Certificates SQL
-- WARNING: This is for testing purposes only. DO NOT use in production.
-- Run AFTER 01_test_members.sql

-- ========================================
-- Test Certificates (수료증)
-- ========================================
INSERT INTO swcampus.certificates (certificate_id, user_id, lecture_id, status, approval_status, image_url, created_at, updated_at)
VALUES
    (1, 2, 66, 'COMPLETED', 'APPROVED', 'https://example.com/cert1.jpg', NOW(), NOW()),
    (2, 3, 1, 'COMPLETED', 'APPROVED', 'https://example.com/cert2.jpg', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Update Sequence
SELECT setval('swcampus.certificates_certificate_id_seq', (SELECT COALESCE(MAX(certificate_id), 1) FROM swcampus.certificates));
