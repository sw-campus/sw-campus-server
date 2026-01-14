-- Add KAKAO to social_accounts provider check constraint
ALTER TABLE "swcampus"."social_accounts" DROP CONSTRAINT "social_accounts_provider_check";

ALTER TABLE "swcampus"."social_accounts" ADD CONSTRAINT "social_accounts_provider_check"
    CHECK (((provider)::text = ANY ((ARRAY['GOOGLE'::character varying, 'GITHUB'::character varying, 'KAKAO'::character varying])::text[])));
