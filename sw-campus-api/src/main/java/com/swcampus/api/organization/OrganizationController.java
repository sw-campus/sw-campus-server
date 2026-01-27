package com.swcampus.api.organization;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.lecture.response.LectureSummaryResponse;
import com.swcampus.api.organization.response.OrganizationLectureListResponse;
import com.swcampus.api.organization.response.OrganizationResponse;
import com.swcampus.api.organization.response.OrganizationSummaryResponse;
import com.swcampus.api.review.response.ReviewListResponse;
import com.swcampus.api.security.OptionalCurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSortType;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewSortType;
import com.swcampus.domain.review.dto.ReviewListResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization", description = "기관 관리 API")
public class OrganizationController {

        private final OrganizationService organizationService;
        private final LectureService lectureService;
        private final ReviewService reviewService;

        @GetMapping
        @Operation(summary = "기관 목록 조회", description = "모집 중인 강의 수와 함께 기관 목록을 조회합니다. 키워드로 검색할 수 있습니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        public ResponseEntity<List<OrganizationSummaryResponse>> getOrganizationList(
                        @Parameter(description = "검색 키워드 (기관명)", example = "패스트캠퍼스") @RequestParam(name = "keyword", required = false) String keyword) {

                List<Organization> result = organizationService.getOrganizationList(keyword);

                List<Long> orgIds = result.stream().map(Organization::getId).toList();
                Map<Long, Long> counts = lectureService.getRecruitingLectureCounts(orgIds);

                List<OrganizationSummaryResponse> organizations = result.stream()
                                .map(org -> OrganizationSummaryResponse.from(org, counts.getOrDefault(org.getId(), 0L)))
                                .toList();
                return ResponseEntity.ok(organizations);
        }

        @GetMapping("/{organizationId}")
        @Operation(summary = "기관 상세 조회", description = "기관의 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "기관 없음")
        })
        public ResponseEntity<OrganizationResponse> getOrganization(
                        @Parameter(description = "기관 ID", example = "1", required = true) @PathVariable("organizationId") Long organizationId) {
                Organization organization = organizationService.getOrganization(organizationId);
                return ResponseEntity.ok(OrganizationResponse.from(organization));
        }

        @GetMapping("/{organizationId}/lectures")
        @Operation(summary = "기관별 강의 목록 조회", description = "특정 기관의 강의 목록을 조회합니다. 정렬 옵션: LATEST(최신순), FEE_ASC(자부담금 낮은순), FEE_DESC(자부담금 높은순), START_SOON(개강 빠른순), DURATION_ASC(교육기간 짧은순), DURATION_DESC(교육기간 긴순), REVIEW_COUNT_DESC(리뷰 많은순), SCORE_DESC(별점 높은순)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "기관 없음")
        })
        public ResponseEntity<OrganizationLectureListResponse> getOrganizationLectureList(
                        @Parameter(description = "기관 ID", example = "1", required = true)
                        @PathVariable("organizationId") Long organizationId,
                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기", example = "6")
                        @RequestParam(name = "size", defaultValue = "6") int size,
                        @Parameter(description = "정렬 기준", example = "LATEST")
                        @RequestParam(name = "sort", defaultValue = "LATEST") LectureSortType sortType) {

                // 기관 존재 확인
                Organization organization = organizationService.getOrganization(organizationId);

                // 검색 조건 생성
                LectureSearchCondition condition = LectureSearchCondition.builder()
                                .orgId(organizationId)
                                .lectureAuthStatus(LectureAuthStatus.APPROVED)
                                .sort(sortType)
                                .pageable(PageRequest.of(page, size))
                                .build();

                // 검색 실행 (리뷰 통계 포함)
                Page<LectureSummaryDto> lecturePage = lectureService.searchLecturesWithStats(condition);

                // 응답 변환
                List<LectureSummaryResponse> lectures = lecturePage.getContent().stream()
                                .map(dto -> LectureSummaryResponse.from(dto, organization.getName()))
                                .toList();

                return ResponseEntity.ok(OrganizationLectureListResponse.of(lectures, lecturePage));
        }

        @GetMapping("/{organizationId}/reviews")
        @Operation(summary = "기관별 승인된 후기 조회", description = "기관 ID로 승인된 후기 목록을 조회합니다. 블라인드 미해제 사용자는 1개만 조회됩니다. 정렬 옵션: LATEST(최신순), OLDEST(오래된순), SCORE_DESC(별점 높은순), SCORE_ASC(별점 낮은순)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        public ResponseEntity<ReviewListResponse> getApprovedReviewsByOrganization(
                        @OptionalCurrentMember MemberPrincipal member,
                        @Parameter(description = "기관 ID", example = "1", required = true)
                        @PathVariable("organizationId") Long organizationId,
                        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기", example = "6")
                        @RequestParam(name = "size", defaultValue = "6") int size,
                        @Parameter(description = "정렬 기준 (LATEST, OLDEST, SCORE_DESC, SCORE_ASC)", example = "LATEST")
                        @RequestParam(name = "sort", defaultValue = "LATEST") ReviewSortType sortType) {
                Long requesterId = member != null ? member.memberId() : null;
                ReviewListResult result = reviewService.getApprovedReviewsByOrganizationWithBlind(
                                organizationId, requesterId, page, size, sortType);
                return ResponseEntity.ok(ReviewListResponse.from(result));
        }
}

