package com.swcampus.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Analytics API 캐시 설정
 * 
 * Caffeine 캐시를 사용하여 GA4 API 호출 결과를 5분간 캐싱합니다.
 * 이를 통해 동일한 요청에 대해 GA4 API를 반복 호출하지 않아 성능이 크게 향상됩니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
            "analyticsReport",
            "eventStats",
            "topBanners",
            "topLectures",
            "popularLectures",
            "popularSearchTerms"
        );
        
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)  // 5분 TTL
            .maximumSize(100)                       // 최대 100개 엔트리
            .recordStats());                        // 캐시 통계 활성화 (Micrometer 연동)
        
        return manager;
    }
}
