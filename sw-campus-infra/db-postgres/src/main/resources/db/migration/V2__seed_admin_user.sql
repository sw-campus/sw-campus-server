-- Auto-generated migration file: V2__seed_admin_user.sql
-- Generated at: 2025-12-18 20:20:35

INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password) VALUES (1, 'admin@swcampus.com', 'Admin', 'ADMIN', NOW(), NOW(), '$2b$10$x6p6qO43e.2DnCPQAXBQ7eMffEuwtkFH1DhXJQfS1vZ9CKNV.TGwi') ON CONFLICT DO NOTHING;
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
