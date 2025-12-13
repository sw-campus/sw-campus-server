package com.swcampus.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.admin.request.BlindReviewRequest;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotFoundException;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.review.*;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminReviewController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AdminReviewController 테스트")
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminReviewService adminReviewService;

    @MockitoBean
    private LectureRepository lectureRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private CertificateRepository certificateRepository;

    private void setAdminAuthentication(Long memberId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        auth.setDetails(memberId);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        setAdminAuthentication(1L);
    }

    private Review createMockReview(Long reviewId, Long memberId, Long lectureId, ApprovalStatus status) {
        return Review.of(
                reviewId, memberId, lectureId, 1L,
                "좋은 강의였습니다", 4.3,
                status, false,
                LocalDateTime.now(), LocalDateTime.now(),
                List.of()
        );
    }

    private Certificate createMockCertificate(Long certificateId, Long memberId, Long lectureId, ApprovalStatus status) {
        return Certificate.of(
                certificateId, memberId, lectureId,
                "https://s3.../certificate.jpg", "SUCCESS",
                status, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/reviews")
    class GetPendingReviews {

        @Test
        @DisplayName("대기 중인 후기 목록 조회 성공")
        void getPendingReviews_success() throws Exception {
            // given
            List<Review> reviews = List.of(
                    createMockReview(1L, 1L, 1L, ApprovalStatus.PENDING),
                    createMockReview(2L, 2L, 1L, ApprovalStatus.PENDING)
            );
            given(adminReviewService.getPendingReviews()).willReturn(reviews);

            Lecture lecture = Lecture.builder()
                    .lectureId(1L)
                    .lectureName("Java 풀스택 과정")
                    .build();
            given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));

            Member member1 = Member.of(1L, "user1@example.com", "password", "홍길동", "길동이",
                    "010-1234-5678", Role.USER, null, "서울", LocalDateTime.now(), LocalDateTime.now());
            Member member2 = Member.of(2L, "user2@example.com", "password", "김철수", "철수",
                    "010-1234-5678", Role.USER, null, "서울", LocalDateTime.now(), LocalDateTime.now());
            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));
            given(memberRepository.findById(2L)).willReturn(Optional.of(member2));

            Certificate cert1 = createMockCertificate(1L, 1L, 1L, ApprovalStatus.PENDING);
            given(certificateRepository.findById(1L)).willReturn(Optional.of(cert1));

            // when & then
            mockMvc.perform(get("/api/v1/admin/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_count").value(2))
                    .andExpect(jsonPath("$.reviews").isArray());
        }

        @Test
        @DisplayName("대기 중인 후기가 없으면 빈 리스트 반환")
        void getPendingReviews_empty() throws Exception {
            // given
            given(adminReviewService.getPendingReviews()).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/v1/admin/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_count").value(0))
                    .andExpect(jsonPath("$.reviews").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/certificates/{certificateId}")
    class GetCertificate {

        @Test
        @DisplayName("수료증 조회 성공")
        void getCertificate_success() throws Exception {
            // given
            Long certificateId = 1L;
            Certificate certificate = createMockCertificate(certificateId, 1L, 1L, ApprovalStatus.PENDING);
            given(adminReviewService.getCertificate(certificateId)).willReturn(certificate);

            Lecture lecture = Lecture.builder()
                    .lectureId(1L)
                    .lectureName("Java 풀스택 과정")
                    .build();
            given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));

            // when & then
            mockMvc.perform(get("/api/v1/admin/certificates/{certificateId}", certificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificate_id").value(1))
                    .andExpect(jsonPath("$.lecture_name").value("Java 풀스택 과정"))
                    .andExpect(jsonPath("$.image_url").value("https://s3.../certificate.jpg"));
        }

        @Test
        @DisplayName("수료증 없음 - 404 Not Found")
        void getCertificate_notFound() throws Exception {
            // given
            Long certificateId = 999L;
            given(adminReviewService.getCertificate(certificateId))
                    .willThrow(new CertificateNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/admin/certificates/{certificateId}", certificateId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/certificates/{certificateId}/approve")
    class ApproveCertificate {

        @Test
        @DisplayName("수료증 승인 성공")
        void approveCertificate_success() throws Exception {
            // given
            Long certificateId = 1L;
            Certificate certificate = createMockCertificate(certificateId, 1L, 1L, ApprovalStatus.APPROVED);
            given(adminReviewService.approveCertificate(certificateId)).willReturn(certificate);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/certificates/{certificateId}/approve", certificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificate_id").value(1))
                    .andExpect(jsonPath("$.approval_status").value("APPROVED"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/certificates/{certificateId}/reject")
    class RejectCertificate {

        @Test
        @DisplayName("수료증 반려 성공")
        void rejectCertificate_success() throws Exception {
            // given
            Long certificateId = 1L;
            Certificate certificate = createMockCertificate(certificateId, 1L, 1L, ApprovalStatus.REJECTED);
            given(adminReviewService.rejectCertificate(certificateId)).willReturn(certificate);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/certificates/{certificateId}/reject", certificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificate_id").value(1))
                    .andExpect(jsonPath("$.approval_status").value("REJECTED"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/reviews/{reviewId}")
    class GetReviewDetail {

        @Test
        @DisplayName("후기 상세 조회 성공")
        void getReviewDetail_success() throws Exception {
            // given
            Long reviewId = 1L;
            Review review = createMockReview(reviewId, 1L, 1L, ApprovalStatus.PENDING);
            given(adminReviewService.getReview(reviewId)).willReturn(review);

            Lecture lecture = Lecture.builder()
                    .lectureId(1L)
                    .lectureName("Java 풀스택 과정")
                    .build();
            given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));

            Member member = Member.of(1L, "user@example.com", "password", "홍길동", "길동이",
                    "010-1234-5678", Role.USER, null, "서울", LocalDateTime.now(), LocalDateTime.now());
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            mockMvc.perform(get("/api/v1/admin/reviews/{reviewId}", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.comment").value("좋은 강의였습니다"));
        }

        @Test
        @DisplayName("후기 없음 - 404 Not Found")
        void getReviewDetail_notFound() throws Exception {
            // given
            Long reviewId = 999L;
            given(adminReviewService.getReview(reviewId))
                    .willThrow(new ReviewNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/admin/reviews/{reviewId}", reviewId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/reviews/{reviewId}/approve")
    class ApproveReview {

        @Test
        @DisplayName("후기 승인 성공")
        void approveReview_success() throws Exception {
            // given
            Long reviewId = 1L;
            Review review = createMockReview(reviewId, 1L, 1L, ApprovalStatus.APPROVED);
            given(adminReviewService.approveReview(reviewId)).willReturn(review);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/approve", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.approval_status").value("APPROVED"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/reviews/{reviewId}/reject")
    class RejectReview {

        @Test
        @DisplayName("후기 반려 성공")
        void rejectReview_success() throws Exception {
            // given
            Long reviewId = 1L;
            Review review = createMockReview(reviewId, 1L, 1L, ApprovalStatus.REJECTED);
            given(adminReviewService.rejectReview(reviewId)).willReturn(review);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/reject", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.approval_status").value("REJECTED"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/reviews/{reviewId}/blind")
    class BlindReview {

        @Test
        @DisplayName("후기 블라인드 처리 성공")
        void blindReview_success() throws Exception {
            // given
            Long reviewId = 1L;
            Review review = Review.of(
                    reviewId, 1L, 1L, 1L,
                    "좋은 강의였습니다", 4.3,
                    ApprovalStatus.APPROVED, true, // blurred = true
                    LocalDateTime.now(), LocalDateTime.now(),
                    List.of()
            );
            given(adminReviewService.blindReview(reviewId, true)).willReturn(review);

            BlindReviewRequest request = new BlindReviewRequest(true);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/blind", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.message").value("후기가 블라인드 처리되었습니다"));
        }

        @Test
        @DisplayName("후기 블라인드 해제 성공")
        void unblindReview_success() throws Exception {
            // given
            Long reviewId = 1L;
            Review review = Review.of(
                    reviewId, 1L, 1L, 1L,
                    "좋은 강의였습니다", 4.3,
                    ApprovalStatus.APPROVED, false, // blurred = false
                    LocalDateTime.now(), LocalDateTime.now(),
                    List.of()
            );
            given(adminReviewService.blindReview(reviewId, false)).willReturn(review);

            BlindReviewRequest request = new BlindReviewRequest(false);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/blind", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review_id").value(1))
                    .andExpect(jsonPath("$.message").value("블라인드가 해제되었습니다"));
        }
    }
}
