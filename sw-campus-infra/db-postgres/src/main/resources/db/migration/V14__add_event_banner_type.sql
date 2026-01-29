-- V14: Add EVENT banner type and make lecture_id nullable

-- 1. Drop existing CHECK constraint
ALTER TABLE "swcampus"."banners" DROP CONSTRAINT IF EXISTS "banners_banner_type_check";

-- 2. Add new CHECK constraint with EVENT type
ALTER TABLE "swcampus"."banners" ADD CONSTRAINT "banners_banner_type_check"
    CHECK (("banner_type"::text = ANY (ARRAY['BIG'::text, 'MIDDLE'::text, 'SMALL'::text, 'EVENT'::text])));

-- 3. Make lecture_id nullable for EVENT type banners
ALTER TABLE "swcampus"."banners" ALTER COLUMN "lecture_id" DROP NOT NULL;
