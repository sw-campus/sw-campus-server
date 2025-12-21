-- Banner table creation
-- Generated at: 2025-12-20

-- Create sequence for banner_id
CREATE SEQUENCE IF NOT EXISTS "swcampus"."banners_banner_id_seq";

-- Create banners table
CREATE TABLE IF NOT EXISTS "swcampus"."banners" (
    "banner_id" bigint NOT NULL DEFAULT nextval('swcampus.banners_banner_id_seq'),
    "lecture_id" bigint NOT NULL,
    "banner_type" character varying(50) NOT NULL,
    "url" text,
    "image_url" text,
    "start_date" timestamp with time zone NOT NULL,
    "end_date" timestamp with time zone NOT NULL,
    "is_active" boolean DEFAULT true,
    "created_at" timestamp(6) without time zone DEFAULT NOW(),
    "updated_at" timestamp(6) without time zone DEFAULT NOW(),
    CONSTRAINT "banners_pkey" PRIMARY KEY ("banner_id"),
    CONSTRAINT "banners_lecture_fk" FOREIGN KEY ("lecture_id") REFERENCES "swcampus"."lectures"("lecture_id") ON DELETE CASCADE,
    CONSTRAINT "banners_banner_type_check" CHECK (("banner_type"::text = ANY (ARRAY['BIG'::text, 'MIDDLE'::text, 'SMALL'::text])))
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS "idx_banners_lecture_id" ON "swcampus"."banners" ("lecture_id");
CREATE INDEX IF NOT EXISTS "idx_banners_banner_type" ON "swcampus"."banners" ("banner_type");
CREATE INDEX IF NOT EXISTS "idx_banners_is_active" ON "swcampus"."banners" ("is_active");
