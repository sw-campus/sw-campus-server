-- Banner table creation and seed data
-- Generated at: 2025-12-20

-- Create sequence for banner_id
CREATE SEQUENCE IF NOT EXISTS "swcampus"."banners_banner_id_seq";

-- Create banners table
CREATE TABLE IF NOT EXISTS "swcampus"."banners" (
    "banner_id" bigint NOT NULL DEFAULT nextval('swcampus.banners_banner_id_seq'),
    "lecture_id" bigint NOT NULL,
    "banner_type" character varying(50) NOT NULL,
    "content" text,
    "image_url" text,
    "start_date" timestamp with time zone NOT NULL,
    "end_date" timestamp with time zone NOT NULL,
    "is_active" boolean DEFAULT true,
    "created_at" timestamp(6) without time zone DEFAULT NOW(),
    "updated_at" timestamp(6) without time zone DEFAULT NOW(),
    CONSTRAINT "banners_pkey" PRIMARY KEY ("banner_id"),
    CONSTRAINT "banners_lecture_fk" FOREIGN KEY ("lecture_id") REFERENCES "swcampus"."lectures"("lecture_id") ON DELETE CASCADE,
    CONSTRAINT "banners_banner_type_check" CHECK (("banner_type"::text = ANY (ARRAY['BIG'::text, 'SMALL'::text, 'TEXT'::text])))
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS "idx_banners_lecture_id" ON "swcampus"."banners" ("lecture_id");
CREATE INDEX IF NOT EXISTS "idx_banners_banner_type" ON "swcampus"."banners" ("banner_type");
CREATE INDEX IF NOT EXISTS "idx_banners_is_active" ON "swcampus"."banners" ("is_active");

-- Seed banner test data
-- BIG Banners (메인 페이지 상단 대형 배너)
INSERT INTO swcampus.banners (lecture_id, banner_type, content, image_url, start_date, end_date, is_active) VALUES
(1, 'BIG', '백엔드 개발자를 꿈꾸는 당신에게! 스프링 부트 완벽 마스터 과정', 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=1920&h=600&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(11, 'BIG', '풀스택 개발자 취업 보장! 스프링 & 리액트 통합 부트캠프', 'https://images.unsplash.com/photo-1517180102446-f3ece451e9d8?w=1920&h=600&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(72, 'BIG', 'SW 엔지니어링의 정석! 소프트웨어 엔지니어링 백엔드 부트캠프', 'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=1920&h=600&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(76, 'BIG', 'AI 시대를 선도하라! 인공지능 부트캠프 모집중', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1920&h=600&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(87, 'BIG', '취업률 1위! 멋쟁이사자처럼 백엔드 스쿨 오픈', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=1920&h=600&fit=crop', NOW(), NOW() + INTERVAL '3 months', true);

-- SMALL Banners (서브 페이지, 사이드바 등 소형 배너)
INSERT INTO swcampus.banners (lecture_id, banner_type, content, image_url, start_date, end_date, is_active) VALUES
(15, 'SMALL', '삼성 SSAFY 10기 모집', 'https://images.unsplash.com/photo-1531482615713-2afd69097998?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(49, 'SMALL', 'KT AIVLE School AI 전문가 과정', 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(56, 'SMALL', '네이버클라우드 AIaaS 개발자 과정', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '4 months', true),
(66, 'SMALL', '우아한테크코스 프론트엔드 5기', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '3 months', true),
(67, 'SMALL', '우아한테크코스 백엔드 5기', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '3 months', true),
(70, 'SMALL', '코드스테이츠 프론트엔드 부트캠프', 'https://images.unsplash.com/photo-1547658719-da2b51169166?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '4 months', true),
(103, 'SMALL', '멋쟁이사자처럼 프론트엔드 스쿨', 'https://images.unsplash.com/photo-1593720213428-28a5b9e94613?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(106, 'SMALL', 'AIFFEL 인공지능 혁신학교', 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=400&h=300&fit=crop', NOW(), NOW() + INTERVAL '3 months', true);

-- TEXT Banners (텍스트 기반 간단한 배너, 공지사항 등)
INSERT INTO swcampus.banners (lecture_id, banner_type, content, image_url, start_date, end_date, is_active) VALUES
(118, 'TEXT', '🔥 부스트캠프 AI Tech 6기 모집중! 지금 바로 지원하세요.', NULL, NOW(), NOW() + INTERVAL '1 month', true),
(122, 'TEXT', '💰 블록체인 개발자 부트캠프 - 조기등록 20% 할인!', NULL, NOW(), NOW() + INTERVAL '2 weeks', true),
(123, 'TEXT', '☁️ 클라우드/DevOps 부트캠프 신규 개설! 실무 중심 커리큘럼', NULL, NOW(), NOW() + INTERVAL '3 months', true),
(131, 'TEXT', '⛓️ 블록체인 스쿨 - Web3 개발자로 성장하세요', NULL, NOW(), NOW() + INTERVAL '2 months', true),
(134, 'TEXT', '🤖 프로그래머스 인공지능 데브코스 - AI 엔지니어 취업 준비!', NULL, NOW(), NOW() + INTERVAL '1 month', true);

-- Inactive banners for testing (비활성 배너)
INSERT INTO swcampus.banners (lecture_id, banner_type, content, image_url, start_date, end_date, is_active) VALUES
(2, 'BIG', '[종료] VR/AR 실감형 콘텐츠 개발 과정', 'https://images.unsplash.com/photo-1617802690992-15d93263d3a9?w=1920&h=600&fit=crop', NOW() - INTERVAL '6 months', NOW() - INTERVAL '1 month', false),
(3, 'SMALL', '[종료] 2D VFX 아티스트 양성과정', 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=400&h=300&fit=crop', NOW() - INTERVAL '4 months', NOW() - INTERVAL '1 month', false),
(4, 'TEXT', '[마감] 전자정부 프레임워크 기반 풀스택 개발자 양성과정', NULL, NOW() - INTERVAL '3 months', NOW() - INTERVAL '2 weeks', false);
