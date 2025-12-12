package com.swcampus.api.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

@WebMvcTest(controllers = AdminSurveyController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AdminSurveyController - 관리자 설문조사 API 테스트")
class AdminSurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberSurveyService surveyService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication = mock(
                UsernamePasswordAuthenticationToken.class);
        when(authentication.getDetails()).thenReturn(99L); // Admin userId
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/v1/admin/members/surveys")
    class GetAllSurveys {

        @Test
        @DisplayName("전체 설문조사 목록 조회 성공 (200)")
        void success() throws Exception {
            // given
            MemberSurvey survey1 = MemberSurvey.create(
                    1L, "컴퓨터공학", true,
                    "백엔드 개발자", "정보처리기사",
                    true, BigDecimal.valueOf(500000)
            );
            MemberSurvey survey2 = MemberSurvey.create(
                    2L, "전자공학", false,
                    "프론트엔드 개발자", "SQLD",
                    false, BigDecimal.valueOf(300000)
            );

            Page<MemberSurvey> page = new PageImpl<>(List.of(survey1, survey2));
            when(surveyService.getAllSurveys(any(Pageable.class))).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/v1/admin/members/surveys"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].user_id").value(1))
                    .andExpect(jsonPath("$.content[0].major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.content[1].user_id").value(2))
                    .andExpect(jsonPath("$.content[1].major").value("전자공학"));
        }

        @Test
        @DisplayName("빈 목록 조회 (200)")
        void success_empty() throws Exception {
            // given
            Page<MemberSurvey> emptyPage = Page.empty();
            when(surveyService.getAllSurveys(any(Pageable.class))).thenReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/api/v1/admin/members/surveys"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/members/{userId}/survey")
    class GetSurveyByUserId {

        @Test
        @DisplayName("특정 회원 설문조사 조회 성공 (200)")
        void success() throws Exception {
            // given
            Long targetUserId = 1L;
            MemberSurvey survey = MemberSurvey.create(
                    targetUserId, "컴퓨터공학", true,
                    "백엔드 개발자", "정보처리기사",
                    true, BigDecimal.valueOf(500000)
            );

            when(surveyService.getSurveyByUserId(targetUserId)).thenReturn(survey);

            // when & then
            mockMvc.perform(get("/api/v1/admin/members/{userId}/survey", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user_id").value(targetUserId))
                    .andExpect(jsonPath("$.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.bootcamp_completed").value(true));
        }

        @Test
        @DisplayName("설문조사 없는 회원 조회 시 실패 (404)")
        void fail_notFound() throws Exception {
            // given
            Long targetUserId = 999L;
            when(surveyService.getSurveyByUserId(targetUserId))
                    .thenThrow(new SurveyNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/admin/members/{userId}/survey", targetUserId))
                    .andExpect(status().isNotFound());
        }
    }
}
