-- Seed banner test data
-- Generated at: 2025-12-20

-- BIG Banners (메인 페이지 상단 대형 배너) - 1280x210
INSERT INTO swcampus.banners (lecture_id, banner_type, url, image_url, start_date, end_date, is_active) VALUES
(1, 'BIG', NULL, 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=1280&h=210&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(11, 'BIG', NULL, 'https://images.unsplash.com/photo-1517180102446-f3ece451e9d8?w=1280&h=210&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(72, 'BIG', NULL, 'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=1280&h=210&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(76, 'BIG', 'https://example.com/ai-bootcamp', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1280&h=210&fit=crop', NOW(), NOW() + INTERVAL '6 months', true),
(87, 'BIG', 'https://likelion.net', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=1280&h=210&fit=crop', NOW(), NOW() + INTERVAL '3 months', true);

-- MIDDLE Banners (중형 배너) - 630x190
INSERT INTO swcampus.banners (lecture_id, banner_type, url, image_url, start_date, end_date, is_active) VALUES
(118, 'MIDDLE', 'https://boostcamp.connect.or.kr', 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=630&h=190&fit=crop', NOW(), NOW() + INTERVAL '1 month', true),
(122, 'MIDDLE', NULL, 'https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=630&h=190&fit=crop', NOW(), NOW() + INTERVAL '2 weeks', true),
(123, 'MIDDLE', NULL, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=630&h=190&fit=crop', NOW(), NOW() + INTERVAL '3 months', true),
(131, 'MIDDLE', NULL, 'https://images.unsplash.com/photo-1639322537228-f710d846310a?w=630&h=190&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(134, 'MIDDLE', 'https://programmers.co.kr', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=630&h=190&fit=crop', NOW(), NOW() + INTERVAL '1 month', true);

-- SMALL Banners (소형 배너) - 420x200
INSERT INTO swcampus.banners (lecture_id, banner_type, url, image_url, start_date, end_date, is_active) VALUES
(15, 'SMALL', 'https://www.ssafy.com', 'https://images.unsplash.com/photo-1531482615713-2afd69097998?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(49, 'SMALL', 'https://aivle.kt.co.kr', 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(56, 'SMALL', NULL, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '4 months', true),
(66, 'SMALL', 'https://www.woowacourse.io', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '3 months', true),
(67, 'SMALL', 'https://www.woowacourse.io', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '3 months', true),
(70, 'SMALL', NULL, 'https://images.unsplash.com/photo-1547658719-da2b51169166?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '4 months', true),
(103, 'SMALL', 'https://likelion.net', 'https://images.unsplash.com/photo-1593720213428-28a5b9e94613?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '2 months', true),
(106, 'SMALL', NULL, 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=420&h=200&fit=crop', NOW(), NOW() + INTERVAL '3 months', true);

-- Inactive banners for testing (비활성 배너)
INSERT INTO swcampus.banners (lecture_id, banner_type, url, image_url, start_date, end_date, is_active) VALUES
(2, 'BIG', NULL, 'https://images.unsplash.com/photo-1617802690992-15d93263d3a9?w=1280&h=210&fit=crop', NOW() - INTERVAL '6 months', NOW() - INTERVAL '1 month', false),
(3, 'SMALL', NULL, 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=420&h=200&fit=crop', NOW() - INTERVAL '4 months', NOW() - INTERVAL '1 month', false),
(4, 'MIDDLE', NULL, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=630&h=190&fit=crop', NOW() - INTERVAL '3 months', NOW() - INTERVAL '2 weeks', false);
