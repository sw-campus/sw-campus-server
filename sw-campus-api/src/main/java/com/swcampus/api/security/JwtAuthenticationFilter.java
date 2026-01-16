package com.swcampus.api.security;

import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final List<RequestMatcher> publicGetMatchers;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, String[] publicGetApis) {
        this.tokenProvider = tokenProvider;
        this.publicGetMatchers = Arrays.stream(publicGetApis)
                .map(path -> new AntPathRequestMatcher(path, "GET"))
                .collect(Collectors.toList());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 공개 GET API는 JWT 검사 스킵
        if (isPublicGet(request)) {
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

    private boolean isPublicGet(HttpServletRequest request) {
        return publicGetMatchers.stream().anyMatch(m -> m.matches(request));
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
