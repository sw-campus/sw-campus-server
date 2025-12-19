-- Auto-generated migration file: V3__seed_categories.sql
-- Generated at: 2025-12-19 17:30:09

INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (1, NULL, '부트캠프', 1) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (2, 1, '웹개발', 2) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (3, 2, '프론트엔드 개발', 3) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (4, 2, '백엔드 개발', 4) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (5, 2, '풀스텍 개발', 5) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (6, 1, '모바일', 6) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (7, 6, '모바일', 7) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (8, 1, '데이터∙AI', 8) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (9, 8, '데이터 분석', 9) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (10, 8, '데이터 엔지니어', 10) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (11, 8, 'AI', 11) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (12, 1, '클라우드', 12) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (13, 12, '클라우드', 13) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (14, 1, '보안', 14) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (15, 14, '보안', 15) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (16, 1, '임베디드(IOT)', 16) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (17, 16, '임베디드(IOT)', 17) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (18, 16, '로봇', 18) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (19, 1, '게임∙블록체인', 19) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (20, 19, '게임', 20) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (21, 19, '블록체인', 21) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (22, 1, '기획∙마케팅∙디자인', 22) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (23, 22, '기획', 23) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (24, 22, '마케팅', 24) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, pid, category_name, sort) VALUES (25, 22, '디자인', 25) ON CONFLICT DO NOTHING;
SELECT setval('swcampus.categories_category_id_seq', (SELECT COALESCE(MAX(category_id), 1) FROM swcampus.categories));
