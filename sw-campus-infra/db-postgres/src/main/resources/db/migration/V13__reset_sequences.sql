-- Auto-generated migration file: V13__reset_sequences.sql
-- Generated at: 2025-12-18 20:20:35

SELECT setval('swcampus.lectures_lecture_id_seq', (SELECT COALESCE(MAX(lecture_id), 1) FROM swcampus.lectures));
SELECT setval('swcampus.lecture_steps_step_id_seq', (SELECT COALESCE(MAX(step_id), 1) FROM swcampus.lecture_steps));
SELECT setval('swcampus.lecture_quals_qual_id_seq', (SELECT COALESCE(MAX(qual_id), 1) FROM swcampus.lecture_quals));
SELECT setval('swcampus.lecture_adds_add_id_seq', (SELECT COALESCE(MAX(add_id), 1) FROM swcampus.lecture_adds));
SELECT setval('swcampus.lecture_teachers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM swcampus.lecture_teachers));
SELECT setval('swcampus.lecture_curriculums_id_seq', (SELECT COALESCE(MAX(id), 1) FROM swcampus.lecture_curriculums));
