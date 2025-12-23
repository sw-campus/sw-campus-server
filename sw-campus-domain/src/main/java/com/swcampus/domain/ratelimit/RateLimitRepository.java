package com.swcampus.domain.ratelimit;

/**
 * Rate Limiting을 위한 저장소 인터페이스
 */
public interface RateLimitRepository {

    /**
     * 주어진 키에 대한 요청 횟수를 증가시키고 현재 횟수를 반환
     *
     * @param key 요청 식별 키 (예: "nickname-check:192.168.1.1")
     * @param windowSeconds 윈도우 크기 (초 단위)
     * @return 현재 윈도우 내 요청 횟수
     */
    long incrementAndGet(String key, long windowSeconds);
}
