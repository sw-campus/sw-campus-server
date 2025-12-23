package com.swcampus.api.ratelimit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.swcampus.domain.ratelimit.RateLimitRepository;
import com.swcampus.domain.ratelimit.exception.RateLimitExceededException;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitInterceptor - Rate Limiting 테스트")
class RateLimitInterceptorTest {

    @Mock
    private RateLimitRepository rateLimitRepository;

    @Mock
    private HandlerMethod handlerMethod;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("192.168.1.1");
    }

    @Test
    @DisplayName("Rate limit 미초과 시 요청 허용")
    void preHandle_withinLimit_allowsRequest() throws Exception {
        // given
        RateLimited rateLimited = createRateLimitedAnnotation("test-api", 20, 60);
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(rateLimited);
        when(rateLimitRepository.incrementAndGet(eq("test-api:192.168.1.1"), eq(60L))).thenReturn(10L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Rate limit 초과 시 RateLimitExceededException 발생")
    void preHandle_exceedsLimit_throwsException() {
        // given
        RateLimited rateLimited = createRateLimitedAnnotation("test-api", 20, 60);
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(rateLimited);
        when(rateLimitRepository.incrementAndGet(eq("test-api:192.168.1.1"), eq(60L))).thenReturn(21L);

        // when & then
        assertThatThrownBy(() -> rateLimitInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("Rate limit 경계값 - 정확히 limit 횟수는 허용")
    void preHandle_exactlyAtLimit_allowsRequest() throws Exception {
        // given
        RateLimited rateLimited = createRateLimitedAnnotation("test-api", 20, 60);
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(rateLimited);
        when(rateLimitRepository.incrementAndGet(eq("test-api:192.168.1.1"), eq(60L))).thenReturn(20L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("@RateLimited 어노테이션이 없는 메서드는 통과")
    void preHandle_noAnnotation_allowsRequest() throws Exception {
        // given
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(null);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        verify(rateLimitRepository, never()).incrementAndGet(anyString(), anyLong());
    }

    @Test
    @DisplayName("HandlerMethod가 아닌 경우 통과")
    void preHandle_notHandlerMethod_allowsRequest() throws Exception {
        // given
        Object notHandlerMethod = new Object();

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, notHandlerMethod);

        // then
        assertThat(result).isTrue();
        verify(rateLimitRepository, never()).incrementAndGet(anyString(), anyLong());
    }

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP 사용")
    void preHandle_withXForwardedFor_usesForwardedIp() throws Exception {
        // given
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        RateLimited rateLimited = createRateLimitedAnnotation("test-api", 20, 60);
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(rateLimited);
        when(rateLimitRepository.incrementAndGet(eq("test-api:10.0.0.1"), eq(60L))).thenReturn(1L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        verify(rateLimitRepository).incrementAndGet(eq("test-api:10.0.0.1"), eq(60L));
    }

    @Test
    @DisplayName("X-Real-IP 헤더가 있으면 해당 IP 사용")
    void preHandle_withXRealIp_usesRealIp() throws Exception {
        // given
        request.addHeader("X-Real-IP", "10.0.0.5");
        RateLimited rateLimited = createRateLimitedAnnotation("test-api", 20, 60);
        when(handlerMethod.getMethodAnnotation(RateLimited.class)).thenReturn(rateLimited);
        when(rateLimitRepository.incrementAndGet(eq("test-api:10.0.0.5"), eq(60L))).thenReturn(1L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        verify(rateLimitRepository).incrementAndGet(eq("test-api:10.0.0.5"), eq(60L));
    }

    private RateLimited createRateLimitedAnnotation(String key, int limit, int windowSeconds) {
        return new RateLimited() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RateLimited.class;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public int limit() {
                return limit;
            }

            @Override
            public int windowSeconds() {
                return windowSeconds;
            }
        };
    }
}
