package com.swcampus.api.config;

import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.swcampus.api.ratelimit.RateLimitInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Optional<RateLimitInterceptor> rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        rateLimitInterceptor.ifPresent(interceptor ->
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/api/**")
        );
    }
}
