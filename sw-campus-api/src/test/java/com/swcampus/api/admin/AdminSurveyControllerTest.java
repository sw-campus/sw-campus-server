package com.swcampus.api.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.survey.*;
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

import java.util.List;
import java.util.Map;

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
        MemberPrincipal principal = new MemberPrincipal(99L, "admin@example.com", Role.ADMIN);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private BasicSurvey createTestBasicSurvey(String major) {
        return BasicSurvey.builder()
                .major(major)
                .programmingExperience(ProgrammingExperience.withExperience("삼성 SW 아카데미"))
                .preferredLearningMethod(LearningMethod.OFFLINE)
                .desiredJobs(List.of(DesiredJob.BACKEND, DesiredJob.DATA))
                .desiredJobOther(null)
                .affordableBudgetRange(BudgetRange.RANGE_100_200)
                .build();
    }

    private MemberSurvey createTestMemberSurvey(Long memberId, String major) {
        return MemberSurvey.createWithBasicSurvey(memberId, createTestBasicSurvey(major));
    }

    private MemberSurvey createCompletedSurvey(Long memberId, String major) {
        MemberSurvey survey = createTestMemberSurvey(memberId, major);
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
    @DisplayName("GET /api/v1/admin/surveys")
    class GetAllSurveys {

        @Test
        @DisplayName("전체 설문조사 목록 조회 성공 (200)")
        void success() throws Exception {
            // given
            MemberSurvey survey1 = createCompletedSurvey(1L, "컴퓨터공학");
            MemberSurvey survey2 = createTestMemberSurvey(2L, "전자공학");

            Page<MemberSurvey> page = new PageImpl<>(List.of(survey1, survey2));
            when(surveyService.getAllSurveys(any(Pageable.class))).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/v1/admin/surveys"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].memberId").value(1))
                    .andExpect(jsonPath("$.content[0].basicSurvey.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.content[0].status.hasBasicSurvey").value(true))
                    .andExpect(jsonPath("$.content[0].status.hasAptitudeTest").value(true))
                    .andExpect(jsonPath("$.content[1].memberId").value(2))
                    .andExpect(jsonPath("$.content[1].basicSurvey.major").value("전자공학"))
                    .andExpect(jsonPath("$.content[1].status.hasAptitudeTest").value(false));
        }

        @Test
        @DisplayName("빈 목록 조회 (200)")
        void success_empty() throws Exception {
            // given
            Page<MemberSurvey> emptyPage = Page.empty();
            when(surveyService.getAllSurveys(any(Pageable.class))).thenReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/api/v1/admin/surveys"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/surveys/members/{memberId}")
    class GetSurveyByUserId {

        @Test
        @DisplayName("특정 회원 설문조사 조회 성공 (200)")
        void success() throws Exception {
            // given
            Long targetUserId = 1L;
            MemberSurvey survey = createCompletedSurvey(targetUserId, "컴퓨터공학");

            when(surveyService.getSurveyByMemberId(targetUserId)).thenReturn(survey);

            // when & then
            mockMvc.perform(get("/api/v1/admin/surveys/members/{memberId}", targetUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(targetUserId))
                    .andExpect(jsonPath("$.basicSurvey.major").value("컴퓨터공학"))
                    .andExpect(jsonPath("$.basicSurvey.programmingExperience.hasExperience").value(true))
                    .andExpect(jsonPath("$.status.hasBasicSurvey").value(true))
                    .andExpect(jsonPath("$.status.hasAptitudeTest").value(true))
                    .andExpect(jsonPath("$.results.recommendedJob").value("BACKEND"));
        }

        @Test
        @DisplayName("설문조사 없는 회원 조회 시 실패 (404)")
        void fail_notFound() throws Exception {
            // given
            Long targetUserId = 999L;
            when(surveyService.getSurveyByMemberId(targetUserId))
                    .thenThrow(new SurveyNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/admin/surveys/members/{memberId}", targetUserId))
                    .andExpect(status().isNotFound());
        }
    }
}
