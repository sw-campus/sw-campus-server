package com.swcampus.api.admin;

import com.swcampus.api.admin.request.BannerActiveRequest;
import com.swcampus.api.admin.request.BannerRequest;
import com.swcampus.api.admin.response.AdminBannerResponse;
import com.swcampus.domain.lecture.AdminBannerService;
import com.swcampus.domain.lecture.dto.BannerDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Banner", description = "관리자 배너 관리 API")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class AdminBannerController {

        private final AdminBannerService adminBannerService;

        @Operation(summary = "배너 목록 조회", description = "모든 배너를 조회합니다 (활성/비활성 모두 포함).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "401", description = "인증 필요"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
        })
        @GetMapping
        public ResponseEntity<List<AdminBannerResponse>> getBanners() {
                List<BannerDetailsDto> bannerDetails = adminBannerService.getBannerDetailsDtoList();
                List<AdminBannerResponse> response = bannerDetails.stream()
                                .map(AdminBannerResponse::from)
                                .toList();
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

        @Operation(summary = "배너 생성", description = "새 배너를 생성합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "생성 성공"),
                        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
                        @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
        })
        @PostMapping
        public ResponseEntity<AdminBannerResponse> createBanner(
                        @Valid @RequestBody BannerRequest request) {
                BannerDetailsDto details = adminBannerService.createBanner(request.toDomain());
                return ResponseEntity.status(HttpStatus.CREATED).body(AdminBannerResponse.from(details));
        }

        @Operation(summary = "배너 수정", description = "기존 배너를 수정합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "수정 성공"),
                        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
                        @ApiResponse(responseCode = "404", description = "배너 또는 강의를 찾을 수 없음")
        })
        @PutMapping("/{id}")
        public ResponseEntity<AdminBannerResponse> updateBanner(
                        @Parameter(description = "배너 ID", required = true) @PathVariable("id") Long id,
                        @Valid @RequestBody BannerRequest request) {
                BannerDetailsDto details = adminBannerService.updateBanner(id, request.toDomain());
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
