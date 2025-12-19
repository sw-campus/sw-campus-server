-- Auto-generated migration file: V5__seed_teachers.sql
-- Generated at: 2025-12-19 17:30:09

SELECT setval('swcampus.teachers_teacher_id_seq', (SELECT COALESCE(MAX(teacher_id), 1) FROM swcampus.teachers));
