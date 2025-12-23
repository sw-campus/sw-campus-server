package com.swcampus.api.survey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.api.survey.request.CreateSurveyRequest;
import com.swcampus.api.survey.request.UpdateSurveyRequest;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import com.swcampus.domain.survey.exception.SurveyAlreadyExistsException;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

@WebMvcTest(controllers = SurveyController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("SurveyController - 설문조사 API 테스트")
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberSurveyService surveyService;

    @MockitoBean
    private TokenProvider tokenProvider;

    private static final Long MEMBER_ID = 1L;
    private static final String BASE_URL = "/api/v1/members/me/survey";

    @BeforeEach
    void setUp() {
        MemberPrincipal principal = new MemberPrincipal(MEMBER_ID, "user@example.com", Role.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/v1/members/me/survey")
    class CreateSurvey {

        @Test
        @DisplayName("설문조사 작성 성공 (201)")
        void success() throws Exception {
            // given
            CreateSurveyRequest request = new CreateSurveyRequest(
                    "컴퓨터공학", true, "백엔드 개발자",
                    "정보처리기사", true, BigDecimal.valueOf(500000)
            );

            MemberSurvey survey = MemberSurvey.create(
                    MEMBER_ID, "컴퓨터공학", true,
                    "백엔드 개발자", "정보처리기사",
                    true, BigDecimal.valueOf(500000)
            );

            when(surveyService.createSurvey(
                    eq(MEMBER_ID), any(), any(), any(), any(), any(), any()
            )).thenReturn(survey);

            // when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.member_id").value(MEMBER_ID))
                    .andExpect(jsonPath("$.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.bootcamp_completed").value(true))
                    .andExpect(jsonPath("$.wanted_jobs").value("백엔드 개발자"))
                    .andExpect(jsonPath("$.licenses").value("정보처리기사"))
                    .andExpect(jsonPath("$.has_gov_card").value(true))
                    .andExpect(jsonPath("$.affordable_amount").value(500000));
        }

        @Test
        @DisplayName("이미 설문조사 존재 시 실패 (409)")
        void fail_alreadyExists() throws Exception {
            // given
            CreateSurveyRequest request = new CreateSurveyRequest(
                    "컴퓨터공학", true, "백엔드 개발자",
                    "정보처리기사", true, BigDecimal.valueOf(500000)
            );

            when(surveyService.createSurvey(
                    eq(MEMBER_ID), any(), any(), any(), any(), any(), any()
            )).thenThrow(new SurveyAlreadyExistsException());

            // when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/me/survey")
    class GetMySurvey {

        @Test
        @DisplayName("내 설문조사 조회 성공 (200)")
        void success() throws Exception {
            // given
            MemberSurvey survey = MemberSurvey.create(
                    MEMBER_ID, "컴퓨터공학", true,
                    "백엔드 개발자", "정보처리기사",
                    true, BigDecimal.valueOf(500000)
            );

            when(surveyService.getSurveyByMemberId(MEMBER_ID)).thenReturn(survey);

            // when & then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.member_id").value(MEMBER_ID))
                    .andExpect(jsonPath("$.major").value("컴퓨터공학"));
        }

        @Test
        @DisplayName("설문조사 없을 때 (404)")
        void fail_notFound() throws Exception {
            // given
            when(surveyService.getSurveyByMemberId(MEMBER_ID))
                    .thenThrow(new SurveyNotFoundException());

            // when & then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/members/me/survey")
    class UpdateMySurvey {

        @Test
        @DisplayName("설문조사 수정 성공 (200)")
        void success() throws Exception {
            // given
            UpdateSurveyRequest request = new UpdateSurveyRequest(
                    "소프트웨어공학", false, "풀스택 개발자",
                    "정보처리기사, SQLD", false, BigDecimal.valueOf(1000000)
            );

            MemberSurvey updatedSurvey = MemberSurvey.create(
                    MEMBER_ID, "소프트웨어공학", false,
                    "풀스택 개발자", "정보처리기사, SQLD",
                    false, BigDecimal.valueOf(1000000)
            );

            when(surveyService.updateSurvey(
                    eq(MEMBER_ID), any(), any(), any(), any(), any(), any()
            )).thenReturn(updatedSurvey);

            // when & then
            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.major").value("소프트웨어공학"))
                    .andExpect(jsonPath("$.wanted_jobs").value("풀스택 개발자"));
        }

        @Test
        @DisplayName("설문조사 없을 때 수정 실패 (404)")
        void fail_notFound() throws Exception {
            // given
            UpdateSurveyRequest request = new UpdateSurveyRequest(
                    "소프트웨어공학", false, "풀스택 개발자",
                    "정보처리기사, SQLD", false, BigDecimal.valueOf(1000000)
            );

            when(surveyService.updateSurvey(
                    eq(MEMBER_ID), any(), any(), any(), any(), any(), any()
            )).thenThrow(new SurveyNotFoundException());

            // when & then
            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
