package com.swcampus.infra.redis.post;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.post.PostViewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostViewRedisRepository implements PostViewRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "post:view:";

    @Override
    public boolean hasViewed(Long postId, Long userId, long ttlSeconds) {
        String redisKey = KEY_PREFIX + postId + ":" + userId;

        try {
            Boolean exists = redisTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }

            // 조회 기록 저장 (TTL 설정)
            redisTemplate.opsForValue().set(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);
            return false;
        } catch (Exception e) {
            log.error("Failed to check/set post view for key: {}", redisKey, e);
            // Redis 장애 시 조회수 증가 허용 (fail-open)
            return false;
        }
    }
}
