create schema if not exists "swcampus";

create sequence "swcampus"."banners_banner_id_seq";

create sequence "swcampus"."cart_id_seq";

create sequence "swcampus"."categories_category_id_seq";

create sequence "swcampus"."certificates_certificate_id_seq";

create sequence "swcampus"."curriculums_curriculum_id_seq";

create sequence "swcampus"."email_verifications_id_seq";

create sequence "swcampus"."lecture_adds_add_id_seq";

create sequence "swcampus"."lecture_curriculums_id_seq";

create sequence "swcampus"."lecture_quals_qual_id_seq";

create sequence "swcampus"."lecture_steps_step_id_seq";

create sequence "swcampus"."lecture_teachers_id_seq";

create sequence "swcampus"."lectures_lecture_id_seq";

create sequence "swcampus"."members_user_id_seq";

create sequence "swcampus"."organizations_org_id_seq";

create sequence "swcampus"."refresh_tokens_id_seq";

create sequence "swcampus"."reviews_details_review_detail_id_seq";

create sequence "swcampus"."reviews_review_id_seq";

create sequence "swcampus"."social_accounts_id_seq";

create sequence "swcampus"."teachers_teacher_id_seq";

create table "swcampus"."banners" (
    "is_active" boolean,
    "banner_id" bigint not null,
    "created_at" timestamp(6) without time zone,
    "end_date" timestamp(6) with time zone not null,
    "lecture_id" bigint not null,
    "start_date" timestamp(6) with time zone not null,
    "updated_at" timestamp(6) without time zone,
    "banner_type" character varying(255),
    "image_url" text,
    "url" text
);


create table "swcampus"."cart" (
    "id" bigint not null,
    "lecture_id" bigint not null,
    "user_id" bigint not null
);

-- Categories
CREATE TABLE "swcampus"."categories" (
    "sort" integer NOT NULL,
    "category_id" bigint NOT NULL,
    "pid" bigint,
    "category_name" character varying(255) NOT NULL
);

-- Certificates
CREATE TABLE "swcampus"."certificates" (
    "certificate_id" bigint NOT NULL,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint NOT NULL,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint NOT NULL,
    "approval_status" character varying(255) NOT NULL,
    "image_url" character varying(255) NOT NULL,
    "status" character varying(255) NOT NULL
);

-- Curriculums
CREATE TABLE "swcampus"."curriculums" (
    "category_id" bigint NOT NULL,
    "curriculum_id" bigint NOT NULL,
    "curriculum_desc" character varying(255),
    "curriculum_name" character varying(255)
);

-- Email Verifications
CREATE TABLE "swcampus"."email_verifications" (
    "verified" boolean NOT NULL,
    "created_at" timestamp(6) without time zone,
    "expires_at" timestamp(6) without time zone NOT NULL,
    "id" bigint NOT NULL,
    "email" character varying(255) NOT NULL,
    "token" character varying(255) NOT NULL
);

-- Lecture Adds
CREATE TABLE "swcampus"."lecture_adds" (
    "add_id" bigint NOT NULL,
    "lecture_id" bigint,
    "add_name" character varying(255) NOT NULL
);

-- Lecture Curriculums
CREATE TABLE "swcampus"."lecture_curriculums" (
    "curriculum_id" bigint NOT NULL,
    "id" bigint NOT NULL,
    "lecture_id" bigint NOT NULL,
    "level" character varying(255)
);

-- Lecture Quals
CREATE TABLE "swcampus"."lecture_quals" (
    "lecture_id" bigint,
    "qual_id" bigint NOT NULL,
    "text" character varying(255) NOT NULL,
    "type" character varying(255) NOT NULL
);

-- Lecture Steps
CREATE TABLE "swcampus"."lecture_steps" (
    "step_order" integer NOT NULL,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint,
    "step_id" bigint NOT NULL,
    "updated_at" timestamp(6) without time zone,
    "step_type" character varying(255) NOT NULL
);

-- Lecture Teachers
CREATE TABLE "swcampus"."lecture_teachers" (
    "id" bigint NOT NULL,
    "lecture_id" bigint NOT NULL,
    "teacher_id" bigint NOT NULL
);

-- Lectures
CREATE TABLE "swcampus"."lectures" (
    "after_completion" boolean,
    "books" boolean NOT NULL,
    "edu_subsidy" numeric(38,2) NOT NULL,
    "employment_help" boolean NOT NULL,
    "end_time" time(6) without time zone NOT NULL,
    "lecture_fee" numeric(38,2) NOT NULL,
    "max_capacity" integer,
    "mock_interview" boolean NOT NULL,
    "project_mentor" boolean,
    "project_num" integer,
    "project_time" integer,
    "resume" boolean NOT NULL,
    "start_time" time(6) without time zone NOT NULL,
    "subsidy" numeric(38,2) NOT NULL,
    "total_days" integer NOT NULL,
    "total_times" integer NOT NULL,
    "created_at" timestamp(6) without time zone,
    "deadline" timestamp(6) without time zone,
    "end_date" timestamp(6) without time zone NOT NULL,
    "lecture_id" bigint NOT NULL,
    "org_id" bigint NOT NULL,
    "start_date" timestamp(6) without time zone NOT NULL,
    "updated_at" timestamp(6) without time zone,
    "days" character varying(255),
    "equip_merit" character varying(255),
    "equip_pc" character varying(255),
    "goal" character varying(255),
    "lecture_auth_status" character varying(255),
    "lecture_image_url" character varying(255),
    "lecture_loc" character varying(255) NOT NULL,
    "lecture_name" character varying(255) NOT NULL,
    "location" character varying(255),
    "project_team" character varying(255),
    "project_tool" character varying(255),
    "recruit_type" character varying(255) NOT NULL,
    "status" character varying(255) NOT NULL,
    "url" character varying(255)
);

-- Member Surveys
CREATE TABLE "swcampus"."member_surveys" (
    "affordable_amount" numeric(15,2),
    "bootcamp_completed" boolean,
    "has_gov_card" boolean,
    "created_at" timestamp(6) without time zone,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint NOT NULL,
    "major" character varying(100),
    "licenses" character varying(500),
    "wanted_jobs" character varying(255)
);

-- Members
CREATE TABLE "swcampus"."members" (
    "created_at" timestamp(6) without time zone,
    "org_id" bigint,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint NOT NULL,
    "email" character varying(255) NOT NULL,
    "location" character varying(255),
    "name" character varying(255) NOT NULL,
    "nickname" character varying(255),
    "password" character varying(255),
    "phone" character varying(255),
    "role" character varying(255) NOT NULL
);

-- Organizations
CREATE TABLE "swcampus"."organizations" (
    "created_at" timestamp(6) without time zone,
    "org_id" bigint NOT NULL,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint NOT NULL,
    "gov_auth" character varying(100),
    "approval_status" character varying(255),
    "certificate_url" text,
    "description" text,
    "facility_image_url" text,
    "facility_image_url2" text,
    "facility_image_url3" text,
    "facility_image_url4" text,
    "homepage" text,
    "org_logo_url" text,
    "org_name" character varying(255)
);

-- Refresh Tokens
CREATE TABLE "swcampus"."refresh_tokens" (
    "created_at" timestamp(6) without time zone,
    "expires_at" timestamp(6) without time zone NOT NULL,
    "id" bigint NOT NULL,
    "user_id" bigint NOT NULL,
    "token" character varying(500) NOT NULL
);

-- Reviews
CREATE TABLE "swcampus"."reviews" (
    "blurred" boolean NOT NULL,
    "score" double precision,
    "certificate_id" bigint NOT NULL,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint NOT NULL,
    "review_id" bigint NOT NULL,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint NOT NULL,
    "approval_status" character varying(255) NOT NULL,
    "comment" character varying(255) NOT NULL
);

-- Reviews Details
CREATE TABLE "swcampus"."reviews_details" (
    "score" double precision NOT NULL,
    "review_detail_id" bigint NOT NULL,
    "review_id" bigint NOT NULL,
    "category" character varying(255) NOT NULL,
    "comment" character varying(255)
);

-- Social Accounts
CREATE TABLE "swcampus"."social_accounts" (
    "created_at" timestamp(6) without time zone NOT NULL,
    "id" bigint NOT NULL,
    "member_id" bigint NOT NULL,
    "provider" character varying(255) NOT NULL,
    "provider_id" character varying(255) NOT NULL
);

-- Teachers
CREATE TABLE "swcampus"."teachers" (
    "teacher_id" bigint NOT NULL,
    "teacher_name" character varying(50) NOT NULL,
    "teacher_description" character varying(255),
    "teacher_image_url" character varying(255)
);

-- Banners (from V15)
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
    CONSTRAINT "banners_banner_type_check" CHECK (("banner_type"::text = ANY (ARRAY['BIG'::text, 'MIDDLE'::text, 'SMALL'::text])))
);

-- ========================================
-- 4. Indexes
-- ========================================
CREATE UNIQUE INDEX cart_pkey ON swcampus.cart USING btree (id);
CREATE UNIQUE INDEX cart_user_id_lecture_id_key ON swcampus.cart USING btree (user_id, lecture_id);
CREATE UNIQUE INDEX categories_pkey ON swcampus.categories USING btree (category_id);
CREATE UNIQUE INDEX certificates_pkey ON swcampus.certificates USING btree (certificate_id);
CREATE UNIQUE INDEX curriculums_pkey ON swcampus.curriculums USING btree (curriculum_id);
CREATE UNIQUE INDEX email_verifications_pkey ON swcampus.email_verifications USING btree (id);
CREATE UNIQUE INDEX email_verifications_token_key ON swcampus.email_verifications USING btree (token);
CREATE UNIQUE INDEX lecture_adds_pkey ON swcampus.lecture_adds USING btree (add_id);
CREATE UNIQUE INDEX lecture_curriculums_pkey ON swcampus.lecture_curriculums USING btree (id);
CREATE UNIQUE INDEX lecture_quals_pkey ON swcampus.lecture_quals USING btree (qual_id);
CREATE UNIQUE INDEX lecture_steps_pkey ON swcampus.lecture_steps USING btree (step_id);
CREATE UNIQUE INDEX lecture_teachers_pkey ON swcampus.lecture_teachers USING btree (id);
CREATE UNIQUE INDEX lectures_pkey ON swcampus.lectures USING btree (lecture_id);
CREATE UNIQUE INDEX member_surveys_pkey ON swcampus.member_surveys USING btree (user_id);
CREATE UNIQUE INDEX members_email_key ON swcampus.members USING btree (email);
CREATE UNIQUE INDEX members_pkey ON swcampus.members USING btree (user_id);
CREATE UNIQUE INDEX organizations_pkey ON swcampus.organizations USING btree (org_id);
CREATE UNIQUE INDEX refresh_tokens_pkey ON swcampus.refresh_tokens USING btree (id);
CREATE UNIQUE INDEX refresh_tokens_token_key ON swcampus.refresh_tokens USING btree (token);
CREATE UNIQUE INDEX refresh_tokens_user_id_key ON swcampus.refresh_tokens USING btree (user_id);
CREATE UNIQUE INDEX reviews_details_pkey ON swcampus.reviews_details USING btree (review_detail_id);
CREATE UNIQUE INDEX reviews_pkey ON swcampus.reviews USING btree (review_id);
CREATE UNIQUE INDEX social_accounts_pkey ON swcampus.social_accounts USING btree (id);
CREATE UNIQUE INDEX social_accounts_provider_provider_id_key ON swcampus.social_accounts USING btree (provider, provider_id);
CREATE UNIQUE INDEX teachers_pkey ON swcampus.teachers USING btree (teacher_id);

-- Banners indexes
CREATE INDEX IF NOT EXISTS "idx_banners_lecture_id" ON "swcampus"."banners" ("lecture_id");
CREATE INDEX IF NOT EXISTS "idx_banners_banner_type" ON "swcampus"."banners" ("banner_type");
CREATE INDEX IF NOT EXISTS "idx_banners_is_active" ON "swcampus"."banners" ("is_active");

-- Nickname unique index (case-insensitive, from V19)
CREATE UNIQUE INDEX members_nickname_lower_key ON swcampus.members (LOWER(nickname)) WHERE nickname IS NOT NULL;

-- ========================================
-- 5. Primary Key Constraints
-- ========================================
ALTER TABLE "swcampus"."cart" ADD CONSTRAINT "cart_pkey" PRIMARY KEY USING INDEX "cart_pkey";
ALTER TABLE "swcampus"."categories" ADD CONSTRAINT "categories_pkey" PRIMARY KEY USING INDEX "categories_pkey";
ALTER TABLE "swcampus"."certificates" ADD CONSTRAINT "certificates_pkey" PRIMARY KEY USING INDEX "certificates_pkey";
ALTER TABLE "swcampus"."curriculums" ADD CONSTRAINT "curriculums_pkey" PRIMARY KEY USING INDEX "curriculums_pkey";
ALTER TABLE "swcampus"."email_verifications" ADD CONSTRAINT "email_verifications_pkey" PRIMARY KEY USING INDEX "email_verifications_pkey";
ALTER TABLE "swcampus"."lecture_adds" ADD CONSTRAINT "lecture_adds_pkey" PRIMARY KEY USING INDEX "lecture_adds_pkey";
ALTER TABLE "swcampus"."lecture_curriculums" ADD CONSTRAINT "lecture_curriculums_pkey" PRIMARY KEY USING INDEX "lecture_curriculums_pkey";
ALTER TABLE "swcampus"."lecture_quals" ADD CONSTRAINT "lecture_quals_pkey" PRIMARY KEY USING INDEX "lecture_quals_pkey";
ALTER TABLE "swcampus"."lecture_steps" ADD CONSTRAINT "lecture_steps_pkey" PRIMARY KEY USING INDEX "lecture_steps_pkey";
ALTER TABLE "swcampus"."lecture_teachers" ADD CONSTRAINT "lecture_teachers_pkey" PRIMARY KEY USING INDEX "lecture_teachers_pkey";
ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_pkey" PRIMARY KEY USING INDEX "lectures_pkey";
ALTER TABLE "swcampus"."member_surveys" ADD CONSTRAINT "member_surveys_pkey" PRIMARY KEY USING INDEX "member_surveys_pkey";
ALTER TABLE "swcampus"."members" ADD CONSTRAINT "members_pkey" PRIMARY KEY USING INDEX "members_pkey";
ALTER TABLE "swcampus"."organizations" ADD CONSTRAINT "organizations_pkey" PRIMARY KEY USING INDEX "organizations_pkey";
ALTER TABLE "swcampus"."refresh_tokens" ADD CONSTRAINT "refresh_tokens_pkey" PRIMARY KEY USING INDEX "refresh_tokens_pkey";
ALTER TABLE "swcampus"."reviews" ADD CONSTRAINT "reviews_pkey" PRIMARY KEY USING INDEX "reviews_pkey";
ALTER TABLE "swcampus"."reviews_details" ADD CONSTRAINT "reviews_details_pkey" PRIMARY KEY USING INDEX "reviews_details_pkey";
ALTER TABLE "swcampus"."social_accounts" ADD CONSTRAINT "social_accounts_pkey" PRIMARY KEY USING INDEX "social_accounts_pkey";
ALTER TABLE "swcampus"."teachers" ADD CONSTRAINT "teachers_pkey" PRIMARY KEY USING INDEX "teachers_pkey";

-- ========================================
-- 6. Unique Constraints
-- ========================================
ALTER TABLE "swcampus"."cart" ADD CONSTRAINT "cart_user_id_lecture_id_key" UNIQUE USING INDEX "cart_user_id_lecture_id_key";
ALTER TABLE "swcampus"."email_verifications" ADD CONSTRAINT "email_verifications_token_key" UNIQUE USING INDEX "email_verifications_token_key";
ALTER TABLE "swcampus"."members" ADD CONSTRAINT "members_email_key" UNIQUE USING INDEX "members_email_key";
ALTER TABLE "swcampus"."refresh_tokens" ADD CONSTRAINT "refresh_tokens_token_key" UNIQUE USING INDEX "refresh_tokens_token_key";
ALTER TABLE "swcampus"."refresh_tokens" ADD CONSTRAINT "refresh_tokens_user_id_key" UNIQUE USING INDEX "refresh_tokens_user_id_key";
ALTER TABLE "swcampus"."social_accounts" ADD CONSTRAINT "social_accounts_provider_provider_id_key" UNIQUE USING INDEX "social_accounts_provider_provider_id_key";

-- ========================================
-- 7. Check Constraints
-- ========================================
ALTER TABLE "swcampus"."certificates" ADD CONSTRAINT "certificates_approval_status_check"
    CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."certificates" VALIDATE CONSTRAINT "certificates_approval_status_check";

ALTER TABLE "swcampus"."lecture_curriculums" ADD CONSTRAINT "lecture_curriculums_level_check"
    CHECK (((level)::text = ANY ((ARRAY['NONE'::character varying, 'BASIC'::character varying, 'ADVANCED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lecture_curriculums" VALIDATE CONSTRAINT "lecture_curriculums_level_check";

ALTER TABLE "swcampus"."lecture_quals" ADD CONSTRAINT "lecture_quals_type_check"
    CHECK (((type)::text = ANY ((ARRAY['REQUIRED'::character varying, 'PREFERRED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lecture_quals" VALIDATE CONSTRAINT "lecture_quals_type_check";

ALTER TABLE "swcampus"."lecture_steps" ADD CONSTRAINT "lecture_steps_step_type_check"
    CHECK (((step_type)::text = ANY ((ARRAY['DOCUMENT'::character varying, 'INTERVIEW'::character varying, 'CODING_TEST'::character varying, 'PRE_TASK'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lecture_steps" VALIDATE CONSTRAINT "lecture_steps_step_type_check";

ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_equip_pc_check"
    CHECK (((equip_pc)::text = ANY ((ARRAY['NONE'::character varying, 'PC'::character varying, 'LAPTOP'::character varying, 'PERSONAL'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lectures" VALIDATE CONSTRAINT "lectures_equip_pc_check";

ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_lecture_auth_status_check"
    CHECK (((lecture_auth_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lectures" VALIDATE CONSTRAINT "lectures_lecture_auth_status_check";

ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_lecture_loc_check"
    CHECK (((lecture_loc)::text = ANY ((ARRAY['ONLINE'::character varying, 'OFFLINE'::character varying, 'MIXED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lectures" VALIDATE CONSTRAINT "lectures_lecture_loc_check";

ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_recruit_type_check"
    CHECK (((recruit_type)::text = ANY ((ARRAY['CARD_REQUIRED'::character varying, 'GENERAL'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lectures" VALIDATE CONSTRAINT "lectures_recruit_type_check";

ALTER TABLE "swcampus"."lectures" ADD CONSTRAINT "lectures_status_check"
    CHECK (((status)::text = ANY ((ARRAY['RECRUITING'::character varying, 'FINISHED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."lectures" VALIDATE CONSTRAINT "lectures_status_check";

ALTER TABLE "swcampus"."members" ADD CONSTRAINT "members_role_check"
    CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ORGANIZATION'::character varying, 'ADMIN'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."members" VALIDATE CONSTRAINT "members_role_check";

ALTER TABLE "swcampus"."reviews" ADD CONSTRAINT "reviews_approval_status_check"
    CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."reviews" VALIDATE CONSTRAINT "reviews_approval_status_check";

ALTER TABLE "swcampus"."reviews_details" ADD CONSTRAINT "reviews_details_category_check"
    CHECK (((category)::text = ANY ((ARRAY['TEACHER'::character varying, 'CURRICULUM'::character varying, 'MANAGEMENT'::character varying, 'FACILITY'::character varying, 'PROJECT'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."reviews_details" VALIDATE CONSTRAINT "reviews_details_category_check";

ALTER TABLE "swcampus"."social_accounts" ADD CONSTRAINT "social_accounts_provider_check"
    CHECK (((provider)::text = ANY ((ARRAY['GOOGLE'::character varying, 'GITHUB'::character varying])::text[]))) NOT VALID;
ALTER TABLE "swcampus"."social_accounts" VALIDATE CONSTRAINT "social_accounts_provider_check";

-- ========================================
-- 8. Foreign Key Constraints
-- ========================================
ALTER TABLE "swcampus"."curriculums" ADD CONSTRAINT "fkidchal9gkoxrwq4cp4nbpa0g1"
    FOREIGN KEY (category_id) REFERENCES categories(category_id) NOT VALID;
ALTER TABLE "swcampus"."curriculums" VALIDATE CONSTRAINT "fkidchal9gkoxrwq4cp4nbpa0g1";

ALTER TABLE "swcampus"."lecture_adds" ADD CONSTRAINT "fkeetadomehs6h7xbuwf7r9ff2d"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_adds" VALIDATE CONSTRAINT "fkeetadomehs6h7xbuwf7r9ff2d";

ALTER TABLE "swcampus"."lecture_curriculums" ADD CONSTRAINT "fkh1vrtjei3pd7gug2la8nrialy"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_curriculums" VALIDATE CONSTRAINT "fkh1vrtjei3pd7gug2la8nrialy";

ALTER TABLE "swcampus"."lecture_curriculums" ADD CONSTRAINT "fkmf5hhd2asjfohf80q55sc16pa"
    FOREIGN KEY (curriculum_id) REFERENCES curriculums(curriculum_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_curriculums" VALIDATE CONSTRAINT "fkmf5hhd2asjfohf80q55sc16pa";

ALTER TABLE "swcampus"."lecture_quals" ADD CONSTRAINT "fk68cht7hkr8ddf8vsd0y71gvxd"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_quals" VALIDATE CONSTRAINT "fk68cht7hkr8ddf8vsd0y71gvxd";

ALTER TABLE "swcampus"."lecture_steps" ADD CONSTRAINT "fk8g2iv31015fuplh4ldslq54l5"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_steps" VALIDATE CONSTRAINT "fk8g2iv31015fuplh4ldslq54l5";

ALTER TABLE "swcampus"."lecture_teachers" ADD CONSTRAINT "fk8i7sy4v5vynio3lcfb85jjiss"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_teachers" VALIDATE CONSTRAINT "fk8i7sy4v5vynio3lcfb85jjiss";

ALTER TABLE "swcampus"."lecture_teachers" ADD CONSTRAINT "fknbr4jhpvpluopycd06s5si50i"
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) NOT VALID;
ALTER TABLE "swcampus"."lecture_teachers" VALIDATE CONSTRAINT "fknbr4jhpvpluopycd06s5si50i";

ALTER TABLE "swcampus"."reviews_details" ADD CONSTRAINT "fk3w60sswre468d996mlinoav3v"
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) NOT VALID;
ALTER TABLE "swcampus"."reviews_details" VALIDATE CONSTRAINT "fk3w60sswre468d996mlinoav3v";

ALTER TABLE "swcampus"."banners" ADD CONSTRAINT "banners_lecture_fk"
    FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) ON DELETE CASCADE NOT VALID;
ALTER TABLE "swcampus"."banners" VALIDATE CONSTRAINT "banners_lecture_fk";

-- ========================================
-- 9. Test Data Registry (테스트 데이터 추적)
-- ========================================
CREATE TABLE swcampus.test_data_registry (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_test_data_table_record UNIQUE (table_name, record_id)
);

CREATE INDEX idx_test_data_batch_id ON swcampus.test_data_registry(batch_id);
CREATE INDEX idx_test_data_table_name ON swcampus.test_data_registry(table_name);
