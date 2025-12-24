-- Test Cart SQL
-- WARNING: This is for testing purposes only. DO NOT use in production.
-- Run AFTER 01_test_members.sql

-- ========================================
-- Test Cart Items
-- ========================================
-- 김철수의 장바구니
INSERT INTO swcampus.cart (id, user_id, lecture_id)
VALUES
    (nextval('swcampus.cart_id_seq'), 2, 1),
    (nextval('swcampus.cart_id_seq'), 2, 15),
    (nextval('swcampus.cart_id_seq'), 2, 49)
ON CONFLICT DO NOTHING;

-- 이영희의 장바구니
INSERT INTO swcampus.cart (id, user_id, lecture_id)
VALUES
    (nextval('swcampus.cart_id_seq'), 3, 66),
    (nextval('swcampus.cart_id_seq'), 3, 72)
ON CONFLICT DO NOTHING;
