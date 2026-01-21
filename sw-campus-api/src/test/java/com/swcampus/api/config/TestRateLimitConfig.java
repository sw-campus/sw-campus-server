package com.swcampus.api.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.swcampus.domain.ratelimit.RateLimitRepository;

/**
 * 테스트용 RateLimitRepository Mock 설정
 * WebMvcTest에서 RateLimitInterceptor가 의존하는 RateLimitRepository를 제공
 */
@TestConfiguration
public class TestRateLimitConfig {

    @Bean
    @Primary
    public RateLimitRepository rateLimitRepository() {
        RateLimitRepository mock = Mockito.mock(RateLimitRepository.class);
        // 기본적으로 rate limit을 통과하도록 설정 (1회 요청)
        Mockito.when(mock.incrementAndGet(Mockito.anyString(), Mockito.anyLong())).thenReturn(1L);
        return mock;
    }
}
