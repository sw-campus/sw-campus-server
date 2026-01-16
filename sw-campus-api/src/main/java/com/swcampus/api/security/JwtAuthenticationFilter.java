package com.swcampus.api.security;

import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    // ✅ ALB 헬스체크 전용 엔드포인트는 JWT 필터 자체를 타지 않음
    // - SecurityConfig의 permitAll()과 반드시 동일한 범위여야 함
    // - 이거 없으면 permitAll 해도 필터가 먼저 실행돼서 막힘
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // /healthz는 유지 (ALB 헬스체크 전용)
        // actuator 관련은 주석 처리 (나중에 필요하면 주석 해제)
        return uri.equals("/healthz");
        // return uri.equals("/healthz") || uri.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 공개 GET API는 JWT 검사 스킵
        if ("GET".equals(method) && isPublicGetApi(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 나머지만 JWT 인증 처리
        String token = resolveToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            Long memberId = tokenProvider.getMemberId(token);
            String email = tokenProvider.getEmail(token);
            Role role = tokenProvider.getRole(token);

            MemberPrincipal principal = new MemberPrincipal(memberId, email, role);
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.name())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicGetApi(String uri) {
        return uri.startsWith("/api/v1/categories")
                || uri.startsWith("/api/v1/banners")
                || uri.startsWith("/api/v1/lectures")
                || uri.startsWith("/api/v1/organizations")
                || uri.startsWith("/api/v1/reviews");
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 (Bearer 토큰) - 헤더 우선
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Cookie에서 토큰 추출 (fallback)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
