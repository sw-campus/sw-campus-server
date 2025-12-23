-- Auto-generated migration file: V2__seed_admin_user.sql
-- Generated at: 2025-12-19 17:30:09

INSERT INTO swcampus.members (user_id, email, name, role, created_at, updated_at, password) VALUES (1, 'admin@swcampus.com', 'Admin', 'ADMIN', NOW(), NOW(), '$2b$10$iBA5qPX7qtKqaAbTNOkP5OHgCeLays0lsOHG.W07objGW27hZQ7K.') ON CONFLICT DO NOTHING;
SELECT setval('swcampus.members_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM swcampus.members));
