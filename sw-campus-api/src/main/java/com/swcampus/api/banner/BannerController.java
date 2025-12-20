package com.swcampus.api.banner;

import com.swcampus.api.banner.response.BannerResponse;
import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerService;
import com.swcampus.domain.lecture.BannerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "배너 관리 API")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "활성화된 배너 전체 조회", description = "현재 활성화된 모든 배너를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBannerList();
        List<BannerResponse> response = banners.stream()
                .map(BannerResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "타입별 활성화된 배너 조회", description = "배너 타입(BIG, SMALL, TEXT)별로 활성화된 배너를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<BannerResponse>> getActiveBannersByType(
            @Parameter(description = "배너 타입", example = "BIG", required = true) @PathVariable("type") BannerType type) {
        List<Banner> banners = bannerService.getActiveBannerListByType(type);
        List<BannerResponse> response = banners.stream()
                .map(BannerResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lecture/{lectureId}")
    @Operation(summary = "강의별 배너 조회", description = "특정 강의에 연결된 배너 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<BannerResponse>> getBannersByLecture(
            @Parameter(description = "강의 ID", example = "1", required = true) @PathVariable("lectureId") Long lectureId) {
        List<Banner> banners = bannerService.getBannerListByLectureId(lectureId);
        List<BannerResponse> response = banners.stream()
                .map(BannerResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
