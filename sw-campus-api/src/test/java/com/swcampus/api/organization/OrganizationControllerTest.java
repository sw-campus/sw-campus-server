package com.swcampus.api.organization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewSortType;
import com.swcampus.domain.review.ReviewWithNickname;
import com.swcampus.domain.review.dto.ReviewListResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(controllers = OrganizationController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("OrganizationController - 교육기관 테스트")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private LectureService lectureService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("기관 목록 조회 성공")
    void getOrganizationList() throws Exception {
        // given
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

        when(organizationService.getOrganizationList(null)).thenReturn(List.of(organization));
        when(lectureService.getRecruitingLectureCounts(anyList())).thenReturn(Map.of(10L, 5L));

        // when & then
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Test Org"))
                .andExpect(jsonPath("$[0].recruitingLectureCount").value(5));
    }

    @Test
    @DisplayName("기관 상세 조회 성공")
    void getOrganization() throws Exception {
        // given
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

        when(organizationService.getOrganization(10L)).thenReturn(organization);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Org"));
    }

    @Test
    @DisplayName("기관별 강의 목록 조회 성공")
    void getOrganizationLectureList() throws Exception {
        // given
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

        Lecture lecture = Lecture.builder()
                .lectureId(100L)
                .lectureName("Org Lecture")
                .status(LectureStatus.RECRUITING)
                .build();
        LectureSummaryDto dto = LectureSummaryDto.from(lecture, 4.3, 10L);
        Page<LectureSummaryDto> lecturePage = new PageImpl<>(List.of(dto), PageRequest.of(0, 6), 1);

        when(organizationService.getOrganization(10L)).thenReturn(organization);
        when(lectureService.searchLecturesWithStats(any(LectureSearchCondition.class))).thenReturn(lecturePage);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/lectures", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lectures[0].lectureId").value(100))
                .andExpect(jsonPath("$.lectures[0].lectureName").value("Org Lecture"))
                .andExpect(jsonPath("$.lectures[0].averageScore").value(4.3));
    }

    @Test
    @DisplayName("기관별 승인된 후기 페이지네이션 조회 성공")
    void getApprovedReviewsByOrganization_withPagination() throws Exception {
        // given
        Review review1 = Review.of(1L, 1L, 1L, 1L, "좋은 강의였습니다", 4.5,
                ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null);
        Review review2 = Review.of(2L, 2L, 2L, 2L, "만족스러운 수업이었습니다", 4.0,
                ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null);

        List<ReviewWithNickname> reviewsWithNicknames = List.of(
                ReviewWithNickname.of(review1, "사용자1"),
                ReviewWithNickname.of(review2, "사용자2")
        );
        ReviewListResult result = ReviewListResult.of(reviewsWithNicknames, 2, true);

        when(reviewService.getApprovedReviewsByOrganizationWithBlind(
                eq(10L), isNull(), eq(0), eq(10), eq(ReviewSortType.LATEST)))
                .thenReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reviews", 10L)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[0].reviewId").value(1))
                .andExpect(jsonPath("$.reviews[0].comment").value("좋은 강의였습니다"))
                .andExpect(jsonPath("$.reviews[0].score").value(4.5))
                .andExpect(jsonPath("$.reviews[0].nickname").value("사용자1"))
                .andExpect(jsonPath("$.reviews[1].reviewId").value(2));
    }

    @Test
    @DisplayName("기관별 후기 조회 - 기본값으로 조회")
    void getApprovedReviewsByOrganization_withDefaultParams() throws Exception {
        // given
        ReviewListResult result = ReviewListResult.of(List.of(), 0, false);

        when(reviewService.getApprovedReviewsByOrganizationWithBlind(
                eq(10L), isNull(), eq(0), eq(6), eq(ReviewSortType.LATEST)))
                .thenReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reviews", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(0));
    }

    @Test
    @DisplayName("기관별 후기 조회 - 별점 높은순 정렬")
    void getApprovedReviewsByOrganization_sortByScoreDesc() throws Exception {
        // given
        Review review = Review.of(1L, 1L, 1L, 1L, "최고의 강의", 5.0,
                ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null);

        List<ReviewWithNickname> reviewsWithNicknames = List.of(
                ReviewWithNickname.of(review, "베스트리뷰어")
        );
        ReviewListResult result = ReviewListResult.of(reviewsWithNicknames, 1, false);

        when(reviewService.getApprovedReviewsByOrganizationWithBlind(
                eq(10L), isNull(), eq(0), eq(6), eq(ReviewSortType.SCORE_DESC)))
                .thenReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reviews", 10L)
                        .param("sort", "SCORE_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].score").value(5.0));
    }

    @Test
    @DisplayName("기관별 후기 조회 - 두번째 페이지 조회")
    void getApprovedReviewsByOrganization_secondPage() throws Exception {
        // given
        Review review = Review.of(11L, 11L, 11L, 11L, "11번째 후기", 4.0,
                ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null);

        List<ReviewWithNickname> reviewsWithNicknames = List.of(
                ReviewWithNickname.of(review, "사용자11")
        );
        ReviewListResult result = ReviewListResult.of(reviewsWithNicknames, 11, true);

        when(reviewService.getApprovedReviewsByOrganizationWithBlind(
                eq(10L), isNull(), eq(1), eq(10), eq(ReviewSortType.LATEST)))
                .thenReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reviews", 10L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].reviewId").value(11));
    }
}
