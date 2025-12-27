package com.swcampus.api.config;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.swcampus.api.security.JwtAuthenticationFilter;
import com.swcampus.domain.auth.TokenProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            TokenProvider tokenProvider,
            @Value("${cors.allowed-origins}") List<String> allowedOrigins
    ) {
        this.tokenProvider = tokenProvider;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"인증이 필요합니다\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"접근 권한이 없습니다\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth

                // ✅ ALB / Kubernetes Health Check (무조건 허용)
                .requestMatchers(
                    "/health",
                    "/internal/health",
                    "/actuator/health",
                    "/actuator/health/**"
                ).permitAll()

                // Auth
                .requestMatchers("/api/v1/auth/**").permitAll()

                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**"
                ).permitAll()

                // Public GET APIs
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/organizations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/banners/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/members/nickname/check").permitAll()

                // Storage
                .requestMatchers(HttpMethod.GET, "/api/v1/storage/presigned-urls").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/storage/presigned-urls/batch").permitAll()

                // Admin
                .requestMatchers("/api/v1/admin/**").authenticated()

                // Everything else
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
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
