package com.swcampus.api.organization;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.lecture.response.LectureSummaryResponse;
import com.swcampus.api.organization.response.OrganizationResponse;
import com.swcampus.api.organization.response.OrganizationSummaryResponse;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;

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

        @GetMapping
        @Operation(summary = "기관 목록 조회", description = "모집 중인 강의 수와 함께 기관 목록을 조회합니다. 키워드로 검색할 수 있습니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        public ResponseEntity<List<OrganizationSummaryResponse>> getOrganizationList(
                        @Parameter(description = "검색 키워드 (기관명)", example = "패스트캠퍼스") @RequestParam(required = false) String keyword) {

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
                        @Parameter(description = "기관 ID", example = "1", required = true) @PathVariable Long organizationId) {
                Organization organization = organizationService.getOrganization(organizationId);
                return ResponseEntity.ok(OrganizationResponse.from(organization));
        }

        @GetMapping("/{organizationId}/lectures")
        @Operation(summary = "기관별 강의 목록 조회", description = "특정 기관의 강의 목록을 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "기관 없음")
        })
        public ResponseEntity<List<LectureSummaryResponse>> getOrganizationLectureList(
                        @Parameter(description = "기관 ID", example = "1", required = true) @PathVariable Long organizationId) {
                List<Lecture> lectures = lectureService
                                .getPublishedLectureListByOrgId(organizationId);

                // 배치로 평균 점수 조회 (N+1 문제 해결)
                List<Long> lectureIds = lectures.stream()
                                .map(Lecture::getLectureId)
                                .toList();
                Map<Long, Double> averageScores = lectureService.getAverageScoresByLectureIds(lectureIds);
                Map<Long, Long> reviewCounts = lectureService.getReviewCountsByLectureIds(lectureIds);

                Organization organization = organizationService.getOrganization(organizationId);
                String orgName = organization.getName();

                List<LectureSummaryResponse> response = lectures.stream()
                                .map(lecture -> {
                                        Double averageScore = averageScores.get(lecture.getLectureId());
                                        Long reviewCount = reviewCounts.get(lecture.getLectureId());
                                        return LectureSummaryResponse.from(lecture, orgName, averageScore, reviewCount);
                                })
                                .toList();
                return ResponseEntity.ok(response);
        }
}
