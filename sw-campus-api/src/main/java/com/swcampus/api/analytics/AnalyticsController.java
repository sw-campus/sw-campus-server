package com.swcampus.api.analytics;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.analytics.response.AnalyticsReportResponse;
import com.swcampus.api.analytics.response.BannerClickStatsResponse;
import com.swcampus.api.analytics.response.EventStatsResponse;
import com.swcampus.api.analytics.response.LectureClickStatsResponse;
import com.swcampus.api.analytics.response.PopularLectureResponse;
import com.swcampus.api.analytics.response.PopularSearchTermResponse;
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.domain.analytics.AnalyticsReport;
import com.swcampus.domain.analytics.AnalyticsService;
import com.swcampus.domain.analytics.EventStats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Google Analytics 통계 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics", description = "Google Analytics 통계 API")
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Google Analytics 통계 조회", 
               description = "Google Analytics에서 수집된 사이트 통계 데이터를 조회합니다. " +
                            "총 사용자 수, 활성 사용자 수, 페이지뷰, 세션 수와 " +
                            "일별 통계를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AnalyticsReportResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<AnalyticsReportResponse> getAnalytics(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days
    ) {
        AnalyticsReport report = analyticsService.getReport(days);
        return ResponseEntity.ok(AnalyticsReportResponse.from(report));
    }



    @Operation(summary = "이벤트 통계 조회", 
               description = "배너 클릭, 신청 버튼 클릭, 공유하기 등 커스텀 이벤트 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = EventStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/events")
    public ResponseEntity<EventStatsResponse> getEventStats(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days
    ) {
        EventStats stats = analyticsService.getEventStats(days);
        return ResponseEntity.ok(EventStatsResponse.from(stats));
    }

    @Operation(summary = "배너 클릭 순위 조회",
               description = "클릭 수가 높은 순으로 배너 목록을 조회합니다.")
    @GetMapping("/events/top-banners")
    public ResponseEntity<List<BannerClickStatsResponse>> getTopBanners(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "조회할 개수. 기본값: 10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        var stats = analyticsService.getTopBannersByClicks(days, limit);
        return ResponseEntity.ok(BannerClickStatsResponse.fromList(stats));
    }

    @Operation(summary = "강의 클릭 순위 조회",
               description = "수강신청/공유 클릭 수가 높은 순으로 강의 목록을 조회합니다.")
    @GetMapping("/events/top-lectures")
    public ResponseEntity<List<LectureClickStatsResponse>> getTopLectures(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "조회할 개수. 기본값: 10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        var stats = analyticsService.getTopLecturesByClicks(days, limit);
        return ResponseEntity.ok(LectureClickStatsResponse.fromList(stats));
    }
    
    @Operation(summary = "인기 강의 조회",
               description = "페이지 조회수가 높은 순으로 인기 강의 목록을 조회합니다.")
    @GetMapping("/popular-lectures")
    public ResponseEntity<List<PopularLectureResponse>> getPopularLectures(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "조회할 개수. 기본값: 5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        var lectures = analyticsService.getPopularLectures(days, limit);
        return ResponseEntity.ok(PopularLectureResponse.fromList(lectures));
    }
    
    @Operation(summary = "인기 검색어 조회",
               description = "검색 횟수가 높은 순으로 인기 검색어 목록을 조회합니다.")
    @GetMapping("/popular-search-terms")
    public ResponseEntity<List<PopularSearchTermResponse>> getPopularSearchTerms(
            @Parameter(description = "조회할 기간 (일 수). 기본값: 7")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "조회할 개수. 기본값: 10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        var terms = analyticsService.getPopularSearchTerms(days, limit);
        return ResponseEntity.ok(PopularSearchTermResponse.fromList(terms));
    }
}


