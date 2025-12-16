create schema if not exists "swcampus";

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

create table "swcampus"."cart" (
    "id" bigint not null,
    "lecture_id" bigint not null,
    "user_id" bigint not null
);


create table "swcampus"."categories" (
    "sort" integer not null,
    "category_id" bigint not null,
    "pid" bigint,
    "category_name" character varying(255) not null
);


create table "swcampus"."certificates" (
    "certificate_id" bigint not null,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint not null,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint not null,
    "approval_status" character varying(255) not null,
    "image_url" character varying(255) not null,
    "status" character varying(255) not null
);


create table "swcampus"."curriculums" (
    "category_id" bigint not null,
    "curriculum_id" bigint not null,
    "curriculum_name" character varying(255)
);


create table "swcampus"."email_verifications" (
    "verified" boolean not null,
    "created_at" timestamp(6) without time zone,
    "expires_at" timestamp(6) without time zone not null,
    "id" bigint not null,
    "email" character varying(255) not null,
    "token" character varying(255) not null
);


create table "swcampus"."lecture_adds" (
    "add_id" bigint not null,
    "lecture_id" bigint,
    "add_name" character varying(255) not null
);


create table "swcampus"."lecture_curriculums" (
    "curriculum_id" bigint not null,
    "id" bigint not null,
    "lecture_id" bigint not null,
    "level" character varying(255)
);


create table "swcampus"."lecture_quals" (
    "lecture_id" bigint,
    "qual_id" bigint not null,
    "text" character varying(255) not null,
    "type" character varying(255) not null
);


create table "swcampus"."lecture_steps" (
    "step_order" integer not null,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint,
    "step_id" bigint not null,
    "updated_at" timestamp(6) without time zone,
    "step_type" character varying(255) not null
);


create table "swcampus"."lecture_teachers" (
    "id" bigint not null,
    "lecture_id" bigint not null,
    "teacher_id" bigint not null
);


create table "swcampus"."lectures" (
    "after_completion" boolean,
    "books" boolean not null,
    "edu_subsidy" numeric(38,2) not null,
    "employment_help" boolean not null,
    "end_time" time(6) without time zone not null,
    "lecture_fee" numeric(38,2) not null,
    "max_capacity" integer,
    "mock_interview" boolean not null,
    "project_mentor" boolean,
    "project_num" integer,
    "project_time" integer,
    "resume" boolean not null,
    "start_time" time(6) without time zone not null,
    "subsidy" numeric(38,2) not null,
    "total_days" integer not null,
    "total_times" integer not null,
    "created_at" timestamp(6) without time zone,
    "deadline" timestamp(6) without time zone,
    "end_date" timestamp(6) without time zone not null,
    "lecture_id" bigint not null,
    "org_id" bigint not null,
    "start_date" timestamp(6) without time zone not null,
    "updated_at" timestamp(6) without time zone,
    "days" character varying(255),
    "equip_merit" character varying(255),
    "equip_pc" character varying(255),
    "goal" character varying(255),
    "lecture_auth_status" character varying(255),
    "lecture_image_url" character varying(255),
    "lecture_loc" character varying(255) not null,
    "lecture_name" character varying(255) not null,
    "location" character varying(255),
    "project_team" character varying(255),
    "project_tool" character varying(255),
    "recruit_type" character varying(255) not null,
    "status" character varying(255) not null,
    "url" character varying(255)
);


create table "swcampus"."member_surveys" (
    "affordable_amount" numeric(15,2),
    "bootcamp_completed" boolean,
    "has_gov_card" boolean,
    "created_at" timestamp(6) without time zone,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint not null,
    "major" character varying(100),
    "licenses" character varying(500),
    "wanted_jobs" character varying(255)
);


create table "swcampus"."members" (
    "created_at" timestamp(6) without time zone,
    "org_id" bigint,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint not null,
    "email" character varying(255) not null,
    "location" character varying(255),
    "name" character varying(255) not null,
    "nickname" character varying(255),
    "password" character varying(255),
    "phone" character varying(255),
    "role" character varying(255) not null
);


create table "swcampus"."organizations" (
    "approval_status" integer,
    "created_at" timestamp(6) without time zone,
    "org_id" bigint not null,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint not null,
    "gov_auth" character varying(100),
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


create table "swcampus"."refresh_tokens" (
    "created_at" timestamp(6) without time zone,
    "expires_at" timestamp(6) without time zone not null,
    "id" bigint not null,
    "user_id" bigint not null,
    "token" character varying(500) not null
);


create table "swcampus"."reviews" (
    "blurred" boolean not null,
    "score" double precision,
    "certificate_id" bigint not null,
    "created_at" timestamp(6) without time zone,
    "lecture_id" bigint not null,
    "review_id" bigint not null,
    "updated_at" timestamp(6) without time zone,
    "user_id" bigint not null,
    "approval_status" character varying(255) not null,
    "comment" character varying(255) not null
);


create table "swcampus"."reviews_details" (
    "score" double precision not null,
    "review_detail_id" bigint not null,
    "review_id" bigint not null,
    "category" character varying(255) not null,
    "comment" character varying(255)
);


create table "swcampus"."social_accounts" (
    "created_at" timestamp(6) without time zone not null,
    "id" bigint not null,
    "member_id" bigint not null,
    "provider" character varying(255) not null,
    "provider_id" character varying(255) not null
);


create table "swcampus"."teachers" (
    "teacher_id" bigint not null,
    "teacher_name" character varying(50) not null,
    "teacher_description" character varying(255),
    "teacher_image_url" character varying(255)
);


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

alter table "swcampus"."cart" add constraint "cart_pkey" PRIMARY KEY using index "cart_pkey";

alter table "swcampus"."categories" add constraint "categories_pkey" PRIMARY KEY using index "categories_pkey";

alter table "swcampus"."certificates" add constraint "certificates_pkey" PRIMARY KEY using index "certificates_pkey";

alter table "swcampus"."curriculums" add constraint "curriculums_pkey" PRIMARY KEY using index "curriculums_pkey";

alter table "swcampus"."email_verifications" add constraint "email_verifications_pkey" PRIMARY KEY using index "email_verifications_pkey";

alter table "swcampus"."lecture_adds" add constraint "lecture_adds_pkey" PRIMARY KEY using index "lecture_adds_pkey";

alter table "swcampus"."lecture_curriculums" add constraint "lecture_curriculums_pkey" PRIMARY KEY using index "lecture_curriculums_pkey";

alter table "swcampus"."lecture_quals" add constraint "lecture_quals_pkey" PRIMARY KEY using index "lecture_quals_pkey";

alter table "swcampus"."lecture_steps" add constraint "lecture_steps_pkey" PRIMARY KEY using index "lecture_steps_pkey";

alter table "swcampus"."lecture_teachers" add constraint "lecture_teachers_pkey" PRIMARY KEY using index "lecture_teachers_pkey";

alter table "swcampus"."lectures" add constraint "lectures_pkey" PRIMARY KEY using index "lectures_pkey";

alter table "swcampus"."member_surveys" add constraint "member_surveys_pkey" PRIMARY KEY using index "member_surveys_pkey";

alter table "swcampus"."members" add constraint "members_pkey" PRIMARY KEY using index "members_pkey";

alter table "swcampus"."organizations" add constraint "organizations_pkey" PRIMARY KEY using index "organizations_pkey";

alter table "swcampus"."refresh_tokens" add constraint "refresh_tokens_pkey" PRIMARY KEY using index "refresh_tokens_pkey";

alter table "swcampus"."reviews" add constraint "reviews_pkey" PRIMARY KEY using index "reviews_pkey";

alter table "swcampus"."reviews_details" add constraint "reviews_details_pkey" PRIMARY KEY using index "reviews_details_pkey";

alter table "swcampus"."social_accounts" add constraint "social_accounts_pkey" PRIMARY KEY using index "social_accounts_pkey";

alter table "swcampus"."teachers" add constraint "teachers_pkey" PRIMARY KEY using index "teachers_pkey";

alter table "swcampus"."cart" add constraint "cart_user_id_lecture_id_key" UNIQUE using index "cart_user_id_lecture_id_key";

alter table "swcampus"."certificates" add constraint "certificates_approval_status_check" CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) not valid;

alter table "swcampus"."certificates" validate constraint "certificates_approval_status_check";

alter table "swcampus"."curriculums" add constraint "fkidchal9gkoxrwq4cp4nbpa0g1" FOREIGN KEY (category_id) REFERENCES categories(category_id) not valid;

alter table "swcampus"."curriculums" validate constraint "fkidchal9gkoxrwq4cp4nbpa0g1";

alter table "swcampus"."email_verifications" add constraint "email_verifications_token_key" UNIQUE using index "email_verifications_token_key";

alter table "swcampus"."lecture_adds" add constraint "fkeetadomehs6h7xbuwf7r9ff2d" FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) not valid;

alter table "swcampus"."lecture_adds" validate constraint "fkeetadomehs6h7xbuwf7r9ff2d";

alter table "swcampus"."lecture_curriculums" add constraint "fkh1vrtjei3pd7gug2la8nrialy" FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) not valid;

alter table "swcampus"."lecture_curriculums" validate constraint "fkh1vrtjei3pd7gug2la8nrialy";

alter table "swcampus"."lecture_curriculums" add constraint "fkmf5hhd2asjfohf80q55sc16pa" FOREIGN KEY (curriculum_id) REFERENCES curriculums(curriculum_id) not valid;

alter table "swcampus"."lecture_curriculums" validate constraint "fkmf5hhd2asjfohf80q55sc16pa";

alter table "swcampus"."lecture_curriculums" add constraint "lecture_curriculums_level_check" CHECK (((level)::text = ANY ((ARRAY['NONE'::character varying, 'BASIC'::character varying, 'ADVANCED'::character varying])::text[]))) not valid;

alter table "swcampus"."lecture_curriculums" validate constraint "lecture_curriculums_level_check";

alter table "swcampus"."lecture_quals" add constraint "fk68cht7hkr8ddf8vsd0y71gvxd" FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) not valid;

alter table "swcampus"."lecture_quals" validate constraint "fk68cht7hkr8ddf8vsd0y71gvxd";

alter table "swcampus"."lecture_quals" add constraint "lecture_quals_type_check" CHECK (((type)::text = ANY ((ARRAY['REQUIRED'::character varying, 'PREFERRED'::character varying])::text[]))) not valid;

alter table "swcampus"."lecture_quals" validate constraint "lecture_quals_type_check";

alter table "swcampus"."lecture_steps" add constraint "fk8g2iv31015fuplh4ldslq54l5" FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) not valid;

alter table "swcampus"."lecture_steps" validate constraint "fk8g2iv31015fuplh4ldslq54l5";

alter table "swcampus"."lecture_steps" add constraint "lecture_steps_step_type_check" CHECK (((step_type)::text = ANY ((ARRAY['DOCUMENT'::character varying, 'INTERVIEW'::character varying, 'CODING_TEST'::character varying, 'PRE_TASK'::character varying])::text[]))) not valid;

alter table "swcampus"."lecture_steps" validate constraint "lecture_steps_step_type_check";

alter table "swcampus"."lecture_teachers" add constraint "fk8i7sy4v5vynio3lcfb85jjiss" FOREIGN KEY (lecture_id) REFERENCES lectures(lecture_id) not valid;

alter table "swcampus"."lecture_teachers" validate constraint "fk8i7sy4v5vynio3lcfb85jjiss";

alter table "swcampus"."lecture_teachers" add constraint "fknbr4jhpvpluopycd06s5si50i" FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) not valid;

alter table "swcampus"."lecture_teachers" validate constraint "fknbr4jhpvpluopycd06s5si50i";

alter table "swcampus"."lectures" add constraint "lectures_equip_pc_check" CHECK (((equip_pc)::text = ANY ((ARRAY['NONE'::character varying, 'PC'::character varying, 'LAPTOP'::character varying, 'PERSONAL'::character varying])::text[]))) not valid;

alter table "swcampus"."lectures" validate constraint "lectures_equip_pc_check";

alter table "swcampus"."lectures" add constraint "lectures_lecture_auth_status_check" CHECK (((lecture_auth_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) not valid;

alter table "swcampus"."lectures" validate constraint "lectures_lecture_auth_status_check";

alter table "swcampus"."lectures" add constraint "lectures_lecture_loc_check" CHECK (((lecture_loc)::text = ANY ((ARRAY['ONLINE'::character varying, 'OFFLINE'::character varying, 'MIXED'::character varying])::text[]))) not valid;

alter table "swcampus"."lectures" validate constraint "lectures_lecture_loc_check";

alter table "swcampus"."lectures" add constraint "lectures_recruit_type_check" CHECK (((recruit_type)::text = ANY ((ARRAY['CARD_REQUIRED'::character varying, 'GENERAL'::character varying])::text[]))) not valid;

alter table "swcampus"."lectures" validate constraint "lectures_recruit_type_check";

alter table "swcampus"."lectures" add constraint "lectures_status_check" CHECK (((status)::text = ANY ((ARRAY['RECRUITING'::character varying, 'FINISHED'::character varying])::text[]))) not valid;

alter table "swcampus"."lectures" validate constraint "lectures_status_check";

alter table "swcampus"."members" add constraint "members_email_key" UNIQUE using index "members_email_key";

alter table "swcampus"."members" add constraint "members_role_check" CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ORGANIZATION'::character varying, 'ADMIN'::character varying])::text[]))) not valid;

alter table "swcampus"."members" validate constraint "members_role_check";

alter table "swcampus"."refresh_tokens" add constraint "refresh_tokens_token_key" UNIQUE using index "refresh_tokens_token_key";

alter table "swcampus"."refresh_tokens" add constraint "refresh_tokens_user_id_key" UNIQUE using index "refresh_tokens_user_id_key";

alter table "swcampus"."reviews" add constraint "reviews_approval_status_check" CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))) not valid;

alter table "swcampus"."reviews" validate constraint "reviews_approval_status_check";

alter table "swcampus"."reviews_details" add constraint "fk3w60sswre468d996mlinoav3v" FOREIGN KEY (review_id) REFERENCES reviews(review_id) not valid;

alter table "swcampus"."reviews_details" validate constraint "fk3w60sswre468d996mlinoav3v";

alter table "swcampus"."reviews_details" add constraint "reviews_details_category_check" CHECK (((category)::text = ANY ((ARRAY['TEACHER'::character varying, 'CURRICULUM'::character varying, 'MANAGEMENT'::character varying, 'FACILITY'::character varying, 'PROJECT'::character varying])::text[]))) not valid;

alter table "swcampus"."reviews_details" validate constraint "reviews_details_category_check";

alter table "swcampus"."social_accounts" add constraint "social_accounts_provider_check" CHECK (((provider)::text = ANY ((ARRAY['GOOGLE'::character varying, 'GITHUB'::character varying])::text[]))) not valid;

alter table "swcampus"."social_accounts" validate constraint "social_accounts_provider_check";

alter table "swcampus"."social_accounts" add constraint "social_accounts_provider_provider_id_key" UNIQUE using index "social_accounts_provider_provider_id_key";


