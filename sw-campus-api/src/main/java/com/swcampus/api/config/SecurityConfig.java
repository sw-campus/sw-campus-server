package com.swcampus.api.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.swcampus.api.security.JwtAuthenticationFilter;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.auth.TokenValidationResult;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

    // 단일 진실 소스: 공개 GET API 패턴 정의
    public static final String[] PUBLIC_GET_APIS = {
            "/api/v1/categories/**",
            "/api/v1/banners/**",
            "/api/v1/lectures/**",
            "/api/v1/organizations/**",
            "/api/v1/reviews/**"
    };

    private final TokenProvider tokenProvider;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            TokenProvider tokenProvider,
            CorsProperties corsProperties
    ) {
        this.tokenProvider = tokenProvider;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // SSE 요청인 경우 JSON 응답 대신 연결만 종료
                            String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("text/event-stream")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                return;
                            }

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            // JWT 필터에서 저장한 validation result 확인
                            Object validationResult = request.getAttribute(
                                    JwtAuthenticationFilter.TOKEN_VALIDATION_RESULT_ATTRIBUTE);

                            String responseBody;
                            if (validationResult == TokenValidationResult.EXPIRED) {
                                responseBody = "{\"code\": \"A002\", \"message\": \"토큰이 만료되었습니다\"}";
                            } else if (validationResult == TokenValidationResult.INVALID) {
                                responseBody = "{\"code\": \"A001\", \"message\": \"유효하지 않은 토큰입니다\"}";
                            } else {
                                responseBody = "{\"message\": \"인증이 필요합니다\"}";
                            }
                            response.getWriter().write(responseBody);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // SSE 요청인 경우 JSON 응답 대신 연결만 종료
                            String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("text/event-stream")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                return;
                            }

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"접근 권한이 없습니다\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        // Async/Error dispatch는 원래 요청에서 이미 인증됨 (SSE 등)
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.INCLUDE).permitAll()
                        // 인증 없이 접근 가능
                        .requestMatchers(
                                "/api/v1/auth/**",
                                // error page
                                "/error",
                                // health check
                                "/healthz", // ALB 헬스체크 전용 엔드포인트
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/prometheus",
                                // Swagger UI
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**")
                        .permitAll()
                        // 공개 API (조회) - PUBLIC_GET_APIS 상수 사용
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_APIS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/nickname/check").permitAll()
                        // 커뮤니티 API (조회) - 목록/상세/인접글 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/adjacent").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board-categories/**").permitAll()
                        // 사용자 프로필 API (조회) - 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*/profile").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*/commented-posts").permitAll()
                        // Storage API (인증 선택적)
                        .requestMatchers(HttpMethod.GET, "/api/v1/storage/presigned-urls").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/storage/presigned-urls/batch").permitAll()
                        // 관리자 API (ADMIN 역할 필요)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 나머지는 인증 필요
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new JwtAuthenticationFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
            )
        );
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
