package com.swcampus.api.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.config.TestRateLimitConfig;
import com.swcampus.api.config.WebMvcConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.api.ratelimit.RateLimitInterceptor;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.ratelimit.RateLimitRepository;

@WebMvcTest(
        controllers = MemberController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, WebMvcConfig.class, RateLimitInterceptor.class, TestRateLimitConfig.class})
@ActiveProfiles("test")
@DisplayName("MemberController - 회원 API 테스트")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private RateLimitRepository rateLimitRepository;

    @Test
    @DisplayName("닉네임 중복 검사 - 사용 가능한 닉네임")
    void checkNicknameAvailable_available() throws Exception {
        // given
        String nickname = "테스트닉네임";
        when(memberService.isNicknameAvailable(nickname, null)).thenReturn(true);
        when(rateLimitRepository.incrementAndGet(anyString(), anyLong())).thenReturn(1L);

        // when & then
        mockMvc.perform(get("/api/v1/members/nickname/check")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("닉네임 중복 검사 - 이미 사용 중인 닉네임")
    void checkNicknameAvailable_notAvailable() throws Exception {
        // given
        String nickname = "중복닉네임";
        when(memberService.isNicknameAvailable(nickname, null)).thenReturn(false);
        when(rateLimitRepository.incrementAndGet(anyString(), anyLong())).thenReturn(1L);

        // when & then
        mockMvc.perform(get("/api/v1/members/nickname/check")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("닉네임 중복 검사 - Rate Limit 초과 시 429 반환")
    void checkNicknameAvailable_rateLimitExceeded() throws Exception {
        // given
        String nickname = "테스트닉네임";
        when(rateLimitRepository.incrementAndGet(anyString(), anyLong())).thenReturn(21L);

        // when & then
        mockMvc.perform(get("/api/v1/members/nickname/check")
                        .param("nickname", nickname))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));

        // MemberService가 호출되지 않았는지 확인
        verify(memberService, never()).isNicknameAvailable(anyString(), any());
    }

    @Test
    @DisplayName("닉네임 중복 검사 - Rate Limit 경계값 (20회)는 허용")
    void checkNicknameAvailable_atRateLimit() throws Exception {
        // given
        String nickname = "테스트닉네임";
        when(memberService.isNicknameAvailable(nickname, null)).thenReturn(true);
        when(rateLimitRepository.incrementAndGet(anyString(), anyLong())).thenReturn(20L);

        // when & then
        mockMvc.perform(get("/api/v1/members/nickname/check")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
