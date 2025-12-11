package com.swcampus.infra.redis.cart;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.cart.CartCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CartRedisEntityRepository implements CartCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "cart:";
    private static final long TTL_DAYS = 7;

    @Override
    public List<Long> getCartLectureIds(Long userId) {
        try {
            String key = getKey(userId);
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                return list.stream()
                        .map(item -> ((Number) item).longValue())
                        .toList();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get cart lecture ids for userId: {}", userId, e);
            return null;
        }
    }

    @Override
    public void saveCartLectureIds(Long userId, List<Long> lectureIds) {
        try {
            redisTemplate.opsForValue().set(getKey(userId), lectureIds, TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Failed to save cart lecture ids for userId: {}", userId, e);
        }
    }

    @Override
    public void deleteCart(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    private String getKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
