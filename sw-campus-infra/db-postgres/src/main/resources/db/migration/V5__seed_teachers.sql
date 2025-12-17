-- Auto-generated migration file: V5__seed_teachers.sql
-- Generated at: 2025-12-17 10:28:23

SELECT setval('swcampus.teachers_teacher_id_seq', (SELECT COALESCE(MAX(teacher_id), 1) FROM swcampus.teachers));
