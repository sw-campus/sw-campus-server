package com.swcampus.infra.redis.ratelimit;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.ratelimit.RateLimitRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RateLimitRedisRepository implements RateLimitRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "rate-limit:";

    @Override
    public long incrementAndGet(String key, long windowSeconds) {
        String redisKey = KEY_PREFIX + key;

        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                log.warn("Redis increment returned null for key: {}. Assuming fail-open.", redisKey);
                return 0L;
            }

            // 첫 번째 요청인 경우 TTL 설정
            if (count == 1) {
                redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
            }

            return count;
        } catch (Exception e) {
            log.error("Failed to increment rate limit counter for key: {}", key, e);
            // Redis 장애 시 요청 허용 (fail-open)
            return 0L;
        }
    }
}
