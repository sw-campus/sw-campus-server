-- Auto-generated migration file: V2__seed_admin_user.sql
-- Generated at: 2025-12-17 09:24:26

INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password) VALUES (1, 'admin@swcampus.com', 'Admin', 'ADMIN', NOW(), NOW(), 'admin1234') ON CONFLICT DO NOTHING;
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
