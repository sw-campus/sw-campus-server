-- Auto-generated migration file: V3__seed_categories.sql
-- Generated at: 2025-12-17 10:28:23

INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (1, 'AI', 1) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (2, '게임', 2) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (3, '기획', 3) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (4, '데이터분석가', 4) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (5, '데이터엔지니어', 5) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (6, '디자인', 6) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (7, '로봇', 7) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (8, '마케팅', 8) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (9, '메타버스', 9) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (10, '백엔드개발', 10) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (11, '보안', 11) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (12, '웹개발(백엔드)', 12) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (13, '웹개발(풀스택)', 13) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (14, '웹개발(프론트엔드)', 14) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (15, '임베디드(IoT)', 15) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (16, '클라우드', 16) ON CONFLICT DO NOTHING;
INSERT INTO swcampus.categories (category_id, category_name, sort) VALUES (17, '풀스텍 개발', 17) ON CONFLICT DO NOTHING;
SELECT setval('swcampus.categories_category_id_seq', (SELECT COALESCE(MAX(category_id), 1) FROM swcampus.categories));
