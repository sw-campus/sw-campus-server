-- Auto-generated migration file: V2__seed_admin_user.sql
-- Generated at: 2025-12-17 10:28:23

INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password) VALUES (1, 'admin@swcampus.com', 'Admin', 'ADMIN', NOW(), NOW(), '$2b$10$KelIqJHcSlgeHCsNY.XFIuirS7QZ9Z5PiJY2gjI7Kx0qQ3X.rPU5O') ON CONFLICT DO NOTHING;
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
