package com.swcampus.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.api.review.request.CreateReviewRequest;
import com.swcampus.api.review.request.DetailScoreRequest;
import com.swcampus.api.review.request.UpdateReviewRequest;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.certificate.exception.CertificateNotVerifiedException;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.review.*;
import com.swcampus.domain.review.exception.ReviewAlreadyExistsException;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.review.exception.ReviewNotModifiableException;
import com.swcampus.domain.review.exception.ReviewNotOwnerException;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ReviewController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("ReviewController 테스트")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private void setAuthentication(Long memberId) {
        MemberPrincipal principal = new MemberPrincipal(memberId, "user@example.com", Role.USER);
        UsernamePasswordAuthenticationToken authWithDetails =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authWithDetails);
    }

    @BeforeEach
    void setUp() {
        setAuthentication(1L);
    }

    private List<DetailScoreRequest> createValidDetailScores() {
        return List.of(
                new DetailScoreRequest("TEACHER", 4.5, "강사님이 정말 친절하고 설명을 잘 해주셨습니다."),
                new DetailScoreRequest("CURRICULUM", 4.0, "커리큘럼이 체계적이고 실무에 많은 도움됩니다."),
                new DetailScoreRequest("MANAGEMENT", 4.5, "취업지원과 행정 서비스가 정말 좋았습니다."),
                new DetailScoreRequest("FACILITY", 3.5, "시설은 보통이었지만 학습에는 문제없었습니다."),
                new DetailScoreRequest("PROJECT", 5.0, "프로젝트 경험이 정말 유익하고 좋았습니다.")
        );
    }

    private Review createMockReview(Long reviewId, Long memberId, Long lectureId) {
        List<ReviewDetail> details = List.of(
                ReviewDetail.create(ReviewCategory.TEACHER, 4.5, "강사님이 좋았습니다"),
                ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "커리큘럼이 좋았습니다"),
                ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.5, "행정이 좋았습니다"),
                ReviewDetail.create(ReviewCategory.FACILITY, 3.5, "시설이 좋았습니다"),
                ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "프로젝트가 좋았습니다")
        );
        return Review.of(
                reviewId, memberId, lectureId, 1L,
                "좋은 강의였습니다", 4.3,
                ApprovalStatus.PENDING, false,
                LocalDateTime.now(), LocalDateTime.now(),
                details
        );
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/eligibility")
    class CheckEligibility {

        @Test
        @DisplayName("모든 조건 충족 - eligible: true")
        void checkEligibility_allMet() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            ReviewEligibility eligibility = ReviewEligibility.of(true, true, true);
            given(reviewService.checkEligibility(memberId, lectureId))
                    .willReturn(eligibility);

            // when & then
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", String.valueOf(lectureId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(true))
                    .andExpect(jsonPath("$.has_nickname").value(true))
                    .andExpect(jsonPath("$.has_certificate").value(true))
                    .andExpect(jsonPath("$.can_write").value(true));
        }

        @Test
        @DisplayName("닉네임 없음 - eligible: false")
        void checkEligibility_noNickname() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            ReviewEligibility eligibility = ReviewEligibility.of(false, true, true);
            given(reviewService.checkEligibility(memberId, lectureId))
                    .willReturn(eligibility);

            // when & then
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", String.valueOf(lectureId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(false))
                    .andExpect(jsonPath("$.has_nickname").value(false));
        }

        @Test
        @DisplayName("수료증 없음 - eligible: false")
        void checkEligibility_noCertificate() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            ReviewEligibility eligibility = ReviewEligibility.of(true, false, true);
            given(reviewService.checkEligibility(memberId, lectureId))
                    .willReturn(eligibility);

            // when & then
            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("lectureId", String.valueOf(lectureId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eligible").value(false))
                    .andExpect(jsonPath("$.has_certificate").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/reviews")
    class CreateReview {

        @Test
        @DisplayName("후기 작성 성공 - 201 Created")
        void createReview_success() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            CreateReviewRequest request = new CreateReviewRequest(
                    lectureId,
                    "전체적으로 만족스러운 강의였습니다.",
                    createValidDetailScores()
            );

            Review review = createMockReview(1L, memberId, lectureId);
            given(reviewService.createReview(eq(memberId), eq(lectureId), anyString(), anyList()))
                    .willReturn(review);

            given(reviewService.getNickname(memberId))
                    .willReturn("길동이");

            // when & then
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.lecture_id").value(1))
                    .andExpect(jsonPath("$.nickname").value("길동이"))
                    .andExpect(jsonPath("$.approval_status").value("PENDING"));
        }

        @Test
        @DisplayName("수료증 미인증 - 403 Forbidden")
        void createReview_noCertificate() throws Exception {
            // given
            CreateReviewRequest request = new CreateReviewRequest(
                    1L,
                    "테스트 후기입니다.",
                    createValidDetailScores()
            );

            given(reviewService.createReview(anyLong(), anyLong(), anyString(), anyList()))
                    .willThrow(new CertificateNotVerifiedException());

            // when & then
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("이미 후기 존재 - 409 Conflict")
        void createReview_alreadyExists() throws Exception {
            // given
            CreateReviewRequest request = new CreateReviewRequest(
                    1L,
                    "테스트 후기입니다.",
                    createValidDetailScores()
            );

            given(reviewService.createReview(anyLong(), anyLong(), anyString(), anyList()))
                    .willThrow(new ReviewAlreadyExistsException());

            // when & then
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("유효성 검증 실패 - 상세 점수 부족 - 400 Bad Request")
        void createReview_validationFail_insufficientDetails() throws Exception {
            // given
            CreateReviewRequest request = new CreateReviewRequest(
                    1L,
                    "테스트",
                    List.of() // 5개 필요한데 0개
            );

            // when & then
            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/reviews/{reviewId}")
    class UpdateReview {

        @Test
        @DisplayName("후기 수정 성공 - 200 OK")
        void updateReview_success() throws Exception {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;

            UpdateReviewRequest request = new UpdateReviewRequest(
                    "수정된 후기입니다.",
                    createValidDetailScores()
            );

            Review review = createMockReview(reviewId, memberId, 1L);
            given(reviewService.updateReview(eq(memberId), eq(reviewId), anyString(), anyList()))
                    .willReturn(review);

            given(reviewService.getNickname(memberId))
                    .willReturn("길동이");

            // when & then
            mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1));
        }

        @Test
        @DisplayName("후기 없음 - 404 Not Found")
        void updateReview_notFound() throws Exception {
            // given
            Long reviewId = 999L;

            UpdateReviewRequest request = new UpdateReviewRequest(
                    "수정된 후기입니다.",
                    createValidDetailScores()
            );

            given(reviewService.updateReview(anyLong(), eq(reviewId), anyString(), anyList()))
                    .willThrow(new ReviewNotFoundException());

            // when & then
            mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("본인 후기 아님 - 403 Forbidden")
        void updateReview_notOwner() throws Exception {
            // given
            Long reviewId = 1L;

            UpdateReviewRequest request = new UpdateReviewRequest(
                    "수정된 후기입니다.",
                    createValidDetailScores()
            );

            given(reviewService.updateReview(anyLong(), eq(reviewId), anyString(), anyList()))
                    .willThrow(new ReviewNotOwnerException());

            // when & then
            mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("승인된 후기 수정 불가 - 403 Forbidden")
        void updateReview_alreadyApproved() throws Exception {
            // given
            Long reviewId = 1L;

            UpdateReviewRequest request = new UpdateReviewRequest(
                    "수정된 후기입니다.",
                    createValidDetailScores()
            );

            given(reviewService.updateReview(anyLong(), eq(reviewId), anyString(), anyList()))
                    .willThrow(new ReviewNotModifiableException());

            // when & then
            mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/{reviewId}")
    class GetReview {

        @Test
        @DisplayName("후기 상세 조회 성공 - 200 OK")
        void getReview_success() throws Exception {
            // given
            Long reviewId = 1L;
            Long memberId = 1L;

            Review review = createMockReview(reviewId, memberId, 1L);
            ReviewWithNickname reviewWithNickname = ReviewWithNickname.of(review, "길동이");
            given(reviewService.getReviewWithNickname(eq(reviewId), any()))
                    .willReturn(reviewWithNickname);

            // when & then
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.nickname").value("길동이"))
                    .andExpect(jsonPath("$.comment").value("좋은 강의였습니다"));
        }

        @Test
        @DisplayName("후기 없음 - 404 Not Found")
        void getReview_notFound() throws Exception {
            // given
            Long reviewId = 999L;

            given(reviewService.getReviewWithNickname(eq(reviewId), any()))
                    .willThrow(new ReviewNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                    .andExpect(status().isNotFound());
        }
    }
}
