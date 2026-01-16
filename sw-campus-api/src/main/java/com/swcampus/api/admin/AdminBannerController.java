package com.swcampus.api.admin;

import com.swcampus.api.admin.request.BannerActiveRequest;
import com.swcampus.api.admin.request.BannerRequest;
import com.swcampus.api.admin.response.AdminBannerResponse;
import com.swcampus.api.admin.response.BannerStatsResponse;
import com.swcampus.domain.lecture.AdminBannerService;
import com.swcampus.domain.lecture.BannerPeriodStatus;
import com.swcampus.domain.lecture.BannerType;
import com.swcampus.domain.lecture.dto.BannerDetailsDto;
import com.swcampus.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.swcampus.api.exception.FileProcessingException;

import java.io.IOException;

@Tag(name = "Admin Banner", description = "관리자 배너 관리 API")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class AdminBannerController {

        private final AdminBannerService adminBannerService;

        /**
         * 이미지 파일에서 추출한 데이터를 담는 레코드
         */
        private record ImageData(byte[] content, String fileName, String contentType) {
                static ImageData empty() {
                        return new ImageData(null, null, null);
                }
        }

        /**
         * MultipartFile에서 이미지 데이터를 추출합니다.
         * @param image 업로드된 이미지 파일 (nullable)
         * @return 추출된 이미지 데이터
         */
        private ImageData extractImageData(MultipartFile image) {
                if (image == null || image.isEmpty()) {
                        return ImageData.empty();
                }
                try {
                        return new ImageData(
                                        image.getBytes(),
                                        image.getOriginalFilename(),
                                        image.getContentType()
                        );
                } catch (IOException e) {
                        throw new FileProcessingException("배너 이미지 파일 처리 중 오류가 발생했습니다", e);
                }
        }

        @Operation(summary = "배너 상태별 통계 조회", description = "전체/활성/비활성/예정/진행중/종료 배너 수를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "401", description = "인증 필요",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                        examples = @ExampleObject(value = """
                                                {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                                """))),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                        examples = @ExampleObject(value = """
                                                {"status": 403, "message": "관리자 권한이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                                """)))
        })
        @GetMapping("/stats")
        public ResponseEntity<BannerStatsResponse> getStats() {
                var stats = adminBannerService.getStats();
                return ResponseEntity.ok(BannerStatsResponse.of(
                        stats.total(), stats.active(), stats.inactive(), 
                        stats.scheduled(), stats.currentlyActive(), stats.ended()));
        }

        @Operation(summary = "배너 목록 조회", description = "배너를 검색합니다. 키워드, 기간 상태, 배너 타입으로 필터링하고 페이징 처리됩니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "401", description = "인증 필요",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                        examples = @ExampleObject(value = """
                                                {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                                """))),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                        examples = @ExampleObject(value = """
                                                {"status": 403, "message": "관리자 권한이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                                """)))
        })
        @GetMapping
        public ResponseEntity<Page<AdminBannerResponse>> getBanners(
                        @Parameter(description = "강의명 검색어") @RequestParam(name = "keyword", required = false) String keyword,
                        @Parameter(description = "기간 상태 (SCHEDULED, ACTIVE, ENDED)") @RequestParam(name = "periodStatus", required = false) String periodStatus,
                        @Parameter(description = "배너 타입 (BIG, MIDDLE, SMALL)") @RequestParam(name = "type", required = false) BannerType type,
                        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(name = "page", defaultValue = "0") int page,
                        @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                BannerPeriodStatus status = BannerPeriodStatus.fromString(periodStatus);
                Page<BannerDetailsDto> bannerPage = adminBannerService.searchBanners(keyword, status, type, pageable);
                Page<AdminBannerResponse> response = bannerPage.map(AdminBannerResponse::from);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "배너 상세 조회", description = "배너 ID로 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음")
        })
        @GetMapping("/{id}")
        public ResponseEntity<AdminBannerResponse> getBanner(
                        @Parameter(description = "배너 ID", required = true) @PathVariable("id") Long id) {
                BannerDetailsDto details = adminBannerService.getBannerDetailsDto(id);
                return ResponseEntity.ok(AdminBannerResponse.from(details));
        }

        @Operation(summary = "배너 생성", description = "새 배너를 생성합니다. 이미지 파일을 업로드할 수 있습니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "생성 성공"),
                        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
                        @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
        })
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<AdminBannerResponse> createBanner(
                        @Valid @RequestPart("request") BannerRequest request,
                        @RequestPart(value = "image", required = false) MultipartFile image) {
                ImageData imageData = extractImageData(image);

                BannerDetailsDto details = adminBannerService.createBanner(
                                request.toDomain(), imageData.content(), imageData.fileName(), imageData.contentType());
                return ResponseEntity.status(HttpStatus.CREATED).body(AdminBannerResponse.from(details));
        }

        @Operation(summary = "배너 수정", description = "기존 배너를 수정합니다. 새 이미지 파일을 업로드하면 기존 이미지를 대체합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "수정 성공"),
                        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
                        @ApiResponse(responseCode = "404", description = "배너 또는 강의를 찾을 수 없음")
        })
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<AdminBannerResponse> updateBanner(
                        @Parameter(description = "배너 ID", required = true) @PathVariable("id") Long id,
                        @Valid @RequestPart("request") BannerRequest request,
                        @RequestPart(value = "image", required = false) MultipartFile image) {
                ImageData imageData = extractImageData(image);

                BannerDetailsDto details = adminBannerService.updateBanner(
                                id, request.toDomain(), imageData.content(), imageData.fileName(), imageData.contentType());
                return ResponseEntity.ok(AdminBannerResponse.from(details));
        }

        @Operation(summary = "배너 삭제", description = "배너를 삭제합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "삭제 성공"),
                        @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBanner(
                        @Parameter(description = "배너 ID", required = true) @PathVariable("id") Long id) {
                adminBannerService.deleteBanner(id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "배너 활성화/비활성화", description = "배너의 활성화 상태를 변경합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "변경 성공"),
                        @ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음")
        })
        @PatchMapping("/{id}/active")
        public ResponseEntity<AdminBannerResponse> toggleBannerActive(
                        @Parameter(description = "배너 ID", required = true) @PathVariable("id") Long id,
                        @Valid @RequestBody BannerActiveRequest request) {
                BannerDetailsDto details = adminBannerService.toggleBannerActive(id, request.isActive());
                return ResponseEntity.ok(AdminBannerResponse.from(details));
        }
}
