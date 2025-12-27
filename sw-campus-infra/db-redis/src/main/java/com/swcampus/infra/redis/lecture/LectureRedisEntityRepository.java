package com.swcampus.infra.redis.lecture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis를 사용한 강의 캐시 저장소 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LectureRedisEntityRepository implements LectureCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "lecture:";
    private static final long TTL_MINUTES = 30;  // 캐시 히트율 향상을 위해 10분 → 30분

    @Override
    public Optional<Lecture> getLecture(Long lectureId) {
        try {
            String key = getKey(lectureId);
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof Lecture lecture) {
                log.debug("Cache hit for lecture: {}", lectureId);
                return Optional.of(lecture);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get lecture from cache: {}", lectureId, e);
            return Optional.empty();
        }
    }

    @Override
    public void saveLecture(Lecture lecture) {
        try {
            String key = getKey(lecture.getLectureId());
            redisTemplate.opsForValue().set(key, lecture, TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Cached lecture: {}", lecture.getLectureId());
        } catch (Exception e) {
            log.error("Failed to cache lecture: {}", lecture.getLectureId(), e);
        }
    }

    @Override
    public void deleteLecture(Long lectureId) {
        try {
            String key = getKey(lectureId);
            redisTemplate.delete(key);
            log.debug("Deleted lecture cache: {}", lectureId);
        } catch (Exception e) {
            log.error("Failed to delete lecture cache: {}", lectureId, e);
        }
    }

    @Override
    public Map<Long, Lecture> getLectures(List<Long> lectureIds) {
        Map<Long, Lecture> result = new HashMap<>();
        try {
            for (Long id : lectureIds) {
                getLecture(id).ifPresent(lecture -> result.put(id, lecture));
            }
        } catch (Exception e) {
            log.error("Failed to get lectures from cache", e);
        }
        return result;
    }

    @Override
    public void saveLectures(List<Lecture> lectures) {
        try {
            for (Lecture lecture : lectures) {
                saveLecture(lecture);
            }
        } catch (Exception e) {
            log.error("Failed to cache lectures", e);
        }
    }

    private String getKey(Long lectureId) {
        return KEY_PREFIX + lectureId;
    }
}
