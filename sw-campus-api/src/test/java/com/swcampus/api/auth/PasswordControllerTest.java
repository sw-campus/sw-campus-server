package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.PasswordChangeRequest;
import com.swcampus.api.auth.request.TemporaryPasswordRequest;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.PasswordService;
import com.swcampus.domain.auth.TokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("PasswordController 테스트")
class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordService passwordService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("PATCH /api/v1/auth/password - 비밀번호 변경 성공")
    void changePassword() throws Exception {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("OldPassword1!", "NewPassword1!");

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getMemberId("valid-token")).thenReturn(1L);

        // when & then
        mockMvc.perform(patch("/api/v1/auth/password")
                        .cookie(new Cookie("accessToken", "valid-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(passwordService).changePassword(1L, "OldPassword1!", "NewPassword1!");
    }

    @Test
    @DisplayName("POST /api/v1/auth/password/temporary - 임시 비밀번호 발급")
    void issueTemporaryPassword() throws Exception {
        // given
        TemporaryPasswordRequest request = new TemporaryPasswordRequest("user@example.com");

        // when & then
        mockMvc.perform(post("/api/v1/auth/password/temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("임시 비밀번호가 이메일로 발송되었습니다"));

        verify(passwordService).issueTemporaryPassword("user@example.com");
    }

    @Test
    @DisplayName("임시 비밀번호 발급 - 이메일 형식 오류")
    void issueTemporaryPassword_invalidEmail() throws Exception {
        // given
        TemporaryPasswordRequest request = new TemporaryPasswordRequest("invalid-email");

        // when & then
        mockMvc.perform(post("/api/v1/auth/password/temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("임시 비밀번호 발급 - 이메일 누락")
    void issueTemporaryPassword_missingEmail() throws Exception {
        // given
        String requestBody = "{}";

        // when & then
        mockMvc.perform(post("/api/v1/auth/password/temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
