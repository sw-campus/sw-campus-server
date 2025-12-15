package com.swcampus.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.review.request.CreateReviewRequest;
import com.swcampus.api.review.request.DetailScoreRequest;
import com.swcampus.api.review.request.UpdateReviewRequest;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.lecture.LectureLocation;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Review 통합 테스트 (E2E)
 * <p>
 * 실제 서버 컨텍스트에서 전체 후기 플로우를 검증합니다.
 * - 수료증 인증 → 후기 작성 가능 여부 확인 → 후기 작성 → 후기 수정 → 관리자 승인
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Review 통합 테스트 (E2E)")
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    private Member testMember;
    private Lecture testLecture;

    private void setAuthentication(Long memberId) {
        MemberPrincipal principal = new MemberPrincipal(memberId, "user@example.com", Role.USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        // SecurityContext에 인증 정보 설정
        setAuthentication(1L);  // 임시 ID, 실제 저장 후 업데이트

        // 테스트용 회원 생성
        testMember = memberRepository.save(
                Member.createUser("test@example.com", "Password1!",
                        "테스트유저", "테스트닉네임", "010-1234-5678", "서울시")
        );

        // 실제 memberId로 SecurityContext 업데이트
        setAuthentication(testMember.getId());

        // 테스트용 강의 생성 (NOT NULL 컬럼들 포함)
        testLecture = lectureRepository.save(
                Lecture.builder()
                        .lectureName("Spring Boot 실전 강의")
                        .orgId(1L)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .lectureLoc(LectureLocation.OFFLINE)
                        .recruitType(RecruitType.CARD_REQUIRED)
                        .subsidy(BigDecimal.ZERO)
                        .lectureFee(BigDecimal.ZERO)
                        .eduSubsidy(BigDecimal.ZERO)
                        .books(true)
                        .resume(true)
                        .mockInterview(true)
                        .employmentHelp(true)
                        .status(LectureStatus.RECRUITING)
                        .startAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusMonths(3))
                        .totalDays(90)
                        .totalTimes(720)
                        .build()
        );
    }

    private List<DetailScoreRequest> createValidDetailScores() {
        return List.of(
                new DetailScoreRequest("TEACHER", 4.5, "강사님의 설명이 정말 상세하고 이해하기 쉬웠습니다."),
                new DetailScoreRequest("CURRICULUM", 4.0, "커리큘럼 구성이 체계적이고 실무에 도움이 되었습니다."),
                new DetailScoreRequest("MANAGEMENT", 4.5, "취업지원 서비스와 행정 서비스가 매우 좋았습니다."),
                new DetailScoreRequest("FACILITY", 3.5, "시설이 전반적으로 깨끗하고 쾌적했습니다."),
                new DetailScoreRequest("PROJECT", 5.0, "프로젝트 경험을 통해 실무 역량을 기를 수 있었습니다.")
        );
    }

    @Nested
    @DisplayName("시나리오 1: 전체 후기 작성 플로우")
    class FullReviewFlow {

        @Test
        @DisplayName("수료증 인증 → 작성 가능 확인 → 후기 작성 → 후기 조회")
        void completeReviewCreationFlow() throws Exception {
            // === 1단계: 수료증 인증 전 - 작성 불가 ===
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", testLecture.getLectureId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(false))
                    .andExpect(jsonPath("$.has_certificate").value(false));

            // === 2단계: 수료증 인증 ===
            certificateRepository.save(
                    Certificate.create(testMember.getId(), testLecture.getLectureId(),
                            "https://s3.example.com/certificate.jpg", "SUCCESS")
            );

            // === 3단계: 수료증 인증 후 - 작성 가능 ===
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", testLecture.getLectureId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(true))
                    .andExpect(jsonPath("$.has_certificate").value(true))
                    .andExpect(jsonPath("$.has_nickname").value(true))
                    .andExpect(jsonPath("$.can_write").value(true));

            // === 4단계: 후기 작성 ===
            CreateReviewRequest createRequest = new CreateReviewRequest(
                    testLecture.getLectureId(),
                    "전반적으로 매우 만족스러운 강의였습니다. 실무에 바로 적용할 수 있는 내용이 많았습니다.",
                    createValidDetailScores()
            );

            MvcResult createResult = mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.review_id").exists())
                    .andExpect(jsonPath("$.member_id").value(testMember.getId()))
                    .andExpect(jsonPath("$.lecture_id").value(testLecture.getLectureId()))
                    .andExpect(jsonPath("$.approval_status").value("PENDING"))
                    .andReturn();

            // 생성된 reviewId 추출
            String responseBody = createResult.getResponse().getContentAsString();
            Long reviewId = objectMapper.readTree(responseBody).get("review_id").asLong();

            // === 5단계: 작성한 후기 조회 ===
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(reviewId))
                    .andExpect(jsonPath("$.approval_status").value("PENDING"));

            // === 6단계: 작성 후 - 중복 작성 불가 확인 ===
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", testLecture.getLectureId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(false))
                    .andExpect(jsonPath("$.can_write").value(false));
        }

        @Test
        @DisplayName("후기 작성 후 조회 가능")
        void reviewCreationAndQueryFlow() throws Exception {
            // === 사전 준비: 수료증 인증 ===
            certificateRepository.save(
                    Certificate.create(testMember.getId(), testLecture.getLectureId(),
                            "https://s3.example.com/certificate.jpg", "SUCCESS")
            );

            // === 1단계: 후기 작성 ===
            CreateReviewRequest createRequest = new CreateReviewRequest(
                    testLecture.getLectureId(),
                    "처음 작성한 후기 내용입니다.",
                    createValidDetailScores()
            );

            MvcResult createResult = mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.review_id").exists())
                    .andExpect(jsonPath("$.approval_status").value("PENDING"))
                    .andReturn();

            Long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("review_id").asLong();

            // === 2단계: 작성한 후기 조회 ===
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(reviewId))
                    .andExpect(jsonPath("$.comment").value(createRequest.comment()));
        }
    }

    @Nested
    @DisplayName("시나리오 2: 에러 케이스")
    class ErrorScenarios {

        @Test
        @DisplayName("수료증 없이 후기 작성 시도 - 실패")
        void createReviewWithoutCertificate_fails() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest(
                    testLecture.getLectureId(),
                    "수료증 없이 작성하려는 후기입니다. 이 요청은 실패해야 합니다.",
                    createValidDetailScores()
            );

            // 수료증 없이 후기 작성 시도 시 403 (수료증 인증 필요) 응답
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("중복 후기 작성 시도 - 실패")
        void createDuplicateReview_fails() throws Exception {
            // 수료증 생성
            certificateRepository.save(
                    Certificate.create(testMember.getId(), testLecture.getLectureId(),
                            "https://s3.example.com/certificate.jpg", "SUCCESS")
            );

            // 첫 번째 후기 작성
            CreateReviewRequest request = new CreateReviewRequest(
                    testLecture.getLectureId(),
                    "첫 번째 후기입니다. 매우 만족스러운 강의였습니다.",
                    createValidDetailScores()
            );

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // 두 번째 후기 작성 시도 - 실패
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("타인의 후기 수정 시도 - 실패")
        void updateOthersReview_fails() throws Exception {
            // 다른 회원 생성
            Member otherMember = memberRepository.save(
                    Member.createUser("other@example.com", "Password1!",
                            "다른유저", "다른닉네임", "010-5555-5555", "부산시")
            );

            // otherMember의 수료증 생성
            certificateRepository.save(
                    Certificate.create(otherMember.getId(), testLecture.getLectureId(),
                            "https://s3.example.com/certificate.jpg", "SUCCESS")
            );

            // SecurityContext를 otherMember로 변경 후 후기 생성
            setAuthentication(otherMember.getId());

            CreateReviewRequest createRequest = new CreateReviewRequest(
                    testLecture.getLectureId(),
                    "다른 회원이 작성한 후기입니다. 매우 좋은 강의였습니다.",
                    createValidDetailScores()
            );

            MvcResult result = mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long reviewId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("review_id").asLong();

            // SecurityContext를 testMember로 다시 변경
            setAuthentication(testMember.getId());

            // testMember가 otherMember의 후기 수정 시도 - 실패
            UpdateReviewRequest updateRequest = new UpdateReviewRequest(
                    "해킹 시도! 타인의 후기를 수정하려고 합니다.",
                    createValidDetailScores()
            );

            mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 후기 조회 - 실패")
        void getNotExistingReview_fails() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", 999999L))
                    .andExpect(status().isNotFound());
        }
    }

}
