-- 닉네임 unique 제약조건 추가
-- 1. 기존 중복 닉네임에 suffix 추가 (가장 오래된 것만 유지)
WITH duplicates AS (
    SELECT
        user_id,
        nickname,
        ROW_NUMBER() OVER (
            PARTITION BY LOWER(nickname)
            ORDER BY created_at ASC, user_id ASC
        ) as rn
    FROM swcampus.members
    WHERE nickname IS NOT NULL
)
UPDATE swcampus.members m
SET nickname = m.nickname || '_' || m.user_id
FROM duplicates d
WHERE m.user_id = d.user_id
  AND d.rn > 1;

-- 2. 대소문자 무시 unique 인덱스 생성
CREATE UNIQUE INDEX members_nickname_lower_key
ON swcampus.members (LOWER(nickname))
WHERE nickname IS NOT NULL;
