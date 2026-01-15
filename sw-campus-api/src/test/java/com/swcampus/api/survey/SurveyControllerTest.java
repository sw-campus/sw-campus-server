package com.swcampus.api.survey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.api.survey.request.SaveBasicSurveyRequest;
import com.swcampus.api.survey.request.SubmitAptitudeTestRequest;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.survey.*;
import com.swcampus.domain.survey.exception.AptitudeTestRequiredException;
import com.swcampus.domain.survey.exception.BasicSurveyRequiredException;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private BasicSurvey createTestBasicSurvey() {
        return BasicSurvey.builder()
                .major("컴퓨터공학")
                .programmingExperience(ProgrammingExperience.withExperience("삼성 SW 아카데미"))
                .preferredLearningMethod(LearningMethod.OFFLINE)
                .desiredJobs(List.of(DesiredJob.BACKEND, DesiredJob.DATA))
                .desiredJobOther(null)
                .affordableBudgetRange(BudgetRange.RANGE_100_200)
                .build();
    }

    private MemberSurvey createTestMemberSurvey() {
        return MemberSurvey.createWithBasicSurvey(MEMBER_ID, createTestBasicSurvey());
    }

    private MemberSurvey createCompletedSurvey() {
        MemberSurvey survey = createTestMemberSurvey();
        AptitudeTest aptitudeTest = AptitudeTest.builder()
                .part1Answers(Map.of("q1", 2, "q2", 1, "q3", 2, "q4", 3))
                .part2Answers(Map.of("q5", 3, "q6", 2, "q7", 2, "q8", 3))
                .part3Answers(Map.of(
                        "q9", "B", "q10", "B", "q11", "D",
                        "q12", "B", "q13", "B", "q14", "B", "q15", "B"
                ))
                .build();
        SurveyResults results = SurveyResults.builder()
                .aptitudeScore(65)
                .aptitudeGrade(AptitudeGrade.TALENTED)
                .jobTypeScores(Map.of(JobTypeCode.B, 5, JobTypeCode.F, 1, JobTypeCode.D, 1))
                .recommendedJob(RecommendedJob.BACKEND)
                .build();
        survey.completeAptitudeTest(aptitudeTest, results, 1);
        return survey;
    }

    @Nested
    @DisplayName("GET /api/v1/members/me/survey")
    class GetMySurvey {

        @Test
        @DisplayName("내 설문조사 조회 성공 (200)")
        void success() throws Exception {
            // given
            MemberSurvey survey = createTestMemberSurvey();
            when(surveyService.getSurveyByMemberId(MEMBER_ID)).thenReturn(survey);

            // when & then
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
                    .andExpect(jsonPath("$.basicSurvey.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.basicSurvey.programmingExperience.hasExperience").value(true))
                    .andExpect(jsonPath("$.basicSurvey.preferredLearningMethod").value("OFFLINE"))
                    .andExpect(jsonPath("$.status.hasBasicSurvey").value(true))
                    .andExpect(jsonPath("$.status.hasAptitudeTest").value(false));
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
    @DisplayName("POST /api/v1/members/me/survey/basic")
    class SaveBasicSurvey {

        @Test
        @DisplayName("기초 설문 저장 성공 (200)")
        void success() throws Exception {
            // given
            SaveBasicSurveyRequest request = new SaveBasicSurveyRequest(
                    "컴퓨터공학",
                    new SaveBasicSurveyRequest.ProgrammingExperienceRequest(true, "삼성 SW 아카데미"),
                    LearningMethod.OFFLINE,
                    List.of(DesiredJob.BACKEND, DesiredJob.DATA),
                    null,
                    BudgetRange.RANGE_100_200
            );

            MemberSurvey survey = createTestMemberSurvey();
            when(surveyService.saveBasicSurvey(eq(MEMBER_ID), any(BasicSurvey.class)))
                    .thenReturn(survey);

            // when & then
            mockMvc.perform(post(BASE_URL + "/basic")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
                    .andExpect(jsonPath("$.basicSurvey.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.status.hasBasicSurvey").value(true));
        }

        @Test
        @DisplayName("필수 필드 누락 시 실패 (400)")
        void fail_missingRequiredFields() throws Exception {
            // given
            SaveBasicSurveyRequest request = new SaveBasicSurveyRequest(
                    null, // major is required
                    new SaveBasicSurveyRequest.ProgrammingExperienceRequest(true, null),
                    LearningMethod.OFFLINE,
                    List.of(DesiredJob.BACKEND),
                    null,
                    BudgetRange.RANGE_100_200
            );

            // when & then
            mockMvc.perform(post(BASE_URL + "/basic")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/members/me/survey/aptitude-test")
    class SubmitAptitudeTest {

        @Test
        @DisplayName("성향 테스트 제출 성공 (200)")
        void success() throws Exception {
            // given
            SubmitAptitudeTestRequest request = new SubmitAptitudeTestRequest(
                    Map.of("q1", 2, "q2", 1, "q3", 2, "q4", 3),
                    Map.of("q5", 3, "q6", 2, "q7", 2, "q8", 3),
                    Map.of("q9", "B", "q10", "B", "q11", "D",
                            "q12", "B", "q13", "B", "q14", "B", "q15", "B")
            );

            MemberSurvey survey = createCompletedSurvey();

            when(surveyService.submitAptitudeTest(eq(MEMBER_ID), any(AptitudeTest.class)))
                    .thenReturn(survey);

            // when & then
            mockMvc.perform(post(BASE_URL + "/aptitude-test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status.hasAptitudeTest").value(true))
                    .andExpect(jsonPath("$.results.recommendedJob").value("BACKEND"));
        }

        @Test
        @DisplayName("기초 설문 미완료 시 실패 (400)")
        void fail_noBasicSurvey() throws Exception {
            // given
            SubmitAptitudeTestRequest request = new SubmitAptitudeTestRequest(
                    Map.of("q1", 2, "q2", 1, "q3", 2, "q4", 3),
                    Map.of("q5", 3, "q6", 2, "q7", 2, "q8", 3),
                    Map.of("q9", "B", "q10", "B", "q11", "D",
                            "q12", "B", "q13", "B", "q14", "B", "q15", "B")
            );

            when(surveyService.submitAptitudeTest(eq(MEMBER_ID), any(AptitudeTest.class)))
                    .thenThrow(new BasicSurveyRequiredException());

            // when & then
            mockMvc.perform(post(BASE_URL + "/aptitude-test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/me/survey/results")
    class GetResults {

        @Test
        @DisplayName("설문 결과 조회 성공 (200)")
        void success() throws Exception {
            // given
            SurveyResults results = SurveyResults.builder()
                    .aptitudeScore(65)
                    .aptitudeGrade(AptitudeGrade.TALENTED)
                    .jobTypeScores(Map.of(JobTypeCode.B, 5, JobTypeCode.F, 1, JobTypeCode.D, 1))
                    .recommendedJob(RecommendedJob.BACKEND)
                    .build();

            when(surveyService.getResultsByMemberId(MEMBER_ID)).thenReturn(results);

            // when & then
            mockMvc.perform(get(BASE_URL + "/results"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedJob").value("BACKEND"));
        }

        @Test
        @DisplayName("성향 테스트 미완료 시 실패 (400)")
        void fail_noAptitudeTest() throws Exception {
            // given
            when(surveyService.getResultsByMemberId(MEMBER_ID))
                    .thenThrow(new AptitudeTestRequiredException());

            // when & then
            mockMvc.perform(get(BASE_URL + "/results"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/me/survey/status")
    class GetStatus {

        @Test
        @DisplayName("설문 상태 조회 성공 - 기초 설문만 완료 (200)")
        void success_basicOnly() throws Exception {
            // given
            MemberSurvey survey = createTestMemberSurvey();
            when(surveyService.findSurveyByMemberId(MEMBER_ID))
                    .thenReturn(Optional.of(survey));

            // when & then
            mockMvc.perform(get(BASE_URL + "/status"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasBasicSurvey").value(true))
                    .andExpect(jsonPath("$.hasAptitudeTest").value(false))
                    .andExpect(jsonPath("$.canUseBasicRecommendation").value(true))
                    .andExpect(jsonPath("$.canUsePreciseRecommendation").value(false));
        }

        @Test
        @DisplayName("설문 상태 조회 성공 - 설문 없음 (200)")
        void success_noSurvey() throws Exception {
            // given
            when(surveyService.findSurveyByMemberId(MEMBER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            mockMvc.perform(get(BASE_URL + "/status"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasBasicSurvey").value(false))
                    .andExpect(jsonPath("$.hasAptitudeTest").value(false))
                    .andExpect(jsonPath("$.canUseBasicRecommendation").value(false))
                    .andExpect(jsonPath("$.canUsePreciseRecommendation").value(false));
        }
    }
}
