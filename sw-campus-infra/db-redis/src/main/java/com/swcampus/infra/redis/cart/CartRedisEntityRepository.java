package com.swcampus.infra.redis.cart;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.domain.cart.CartCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CartRedisEntityRepository implements CartCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "cart:";
    private static final long TTL_DAYS = 7;

    @Override
    public List<Long> getCartLectureIds(Long userId) {
        String key = getKey(userId);
        String json = redisTemplate.opsForValue().get(key);

        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<Long>>() {
                });
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void saveCartLectureIds(Long userId, List<Long> lectureIds) {
        try {
            String json = objectMapper.writeValueAsString(lectureIds);
            redisTemplate.opsForValue().set(getKey(userId), json, TTL_DAYS, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart lecture ids for userId: {}", userId, e);
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
