package com.swcampus.api.admin;

import com.swcampus.api.admin.request.BlindReviewRequest;
import com.swcampus.api.admin.response.*;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.review.AdminReviewService;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.dto.PendingReviewInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Review", description = "관리자 후기 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;
    private final LectureService lectureService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Operation(summary = "대기 중인 후기 목록 조회", description = "수료증 또는 후기가 PENDING 상태인 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @GetMapping("/reviews")
    public ResponseEntity<AdminReviewListResponse> getPendingReviews() {
        List<PendingReviewInfo> reviews = adminReviewService.getPendingReviewsWithDetails();

        List<AdminReviewListResponse.AdminReviewSummary> summaries = reviews.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AdminReviewListResponse(summaries, summaries.size()));
    }

    @Operation(summary = "[1단계] 수료증 조회", description = "수료증 이미지 및 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "수료증을 찾을 수 없음")
    })
    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<AdminCertificateResponse> getCertificate(
            @Parameter(description = "수료증 ID", required = true, name = "certificateId")
            @PathVariable("certificateId") Long certificateId) {

        Certificate certificate = adminReviewService.getCertificate(certificateId);

        String lectureName = lectureService.getLectureNames(List.of(certificate.getLectureId()))
                .getOrDefault(certificate.getLectureId(), "알 수 없음");

        return ResponseEntity.ok(new AdminCertificateResponse(
                certificate.getId(),
                certificate.getLectureId(),
                lectureName,
                certificate.getImageUrl(),
                certificate.getApprovalStatus().name(),
                certificate.getCreatedAt().format(FORMATTER)
        ));
    }

    @Operation(summary = "[1단계] 수료증 승인", description = "수료증을 승인하고 2단계(후기 확인)로 진행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공"),
        @ApiResponse(responseCode = "404", description = "수료증을 찾을 수 없음")
    })
    @PatchMapping("/certificates/{certificateId}/approve")
    public ResponseEntity<CertificateApprovalResponse> approveCertificate(
            @Parameter(description = "수료증 ID", required = true, name = "certificateId")
            @PathVariable("certificateId") Long certificateId) {

        Certificate certificate = adminReviewService.approveCertificate(certificateId);

        return ResponseEntity.ok(new CertificateApprovalResponse(
                certificate.getId(),
                certificate.getApprovalStatus().name(),
                "수료증이 승인되었습니다. 후기 내용을 확인해주세요."
        ));
    }

    @Operation(summary = "[1단계] 수료증 반려", description = "수료증을 반려하고 반려 이메일을 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "반려 성공"),
        @ApiResponse(responseCode = "404", description = "수료증을 찾을 수 없음")
    })
    @PatchMapping("/certificates/{certificateId}/reject")
    public ResponseEntity<CertificateApprovalResponse> rejectCertificate(
            @Parameter(description = "수료증 ID", required = true, name = "certificateId")
            @PathVariable("certificateId") Long certificateId) {

        Certificate certificate = adminReviewService.rejectCertificate(certificateId);

        return ResponseEntity.ok(new CertificateApprovalResponse(
                certificate.getId(),
                certificate.getApprovalStatus().name(),
                "수료증이 반려되었습니다. 반려 이메일이 발송됩니다."
        ));
    }

    @Operation(summary = "[2단계] 후기 상세 조회", description = "후기 내용 및 상세 점수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<AdminReviewDetailResponse> getReviewDetail(
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId) {

        PendingReviewInfo reviewInfo = adminReviewService.getReviewWithDetails(reviewId);

        return ResponseEntity.ok(toDetailResponse(reviewInfo));
    }

    @Operation(summary = "[2단계] 후기 승인", description = "후기를 승인하여 일반 사용자에게 노출합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @PatchMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<ReviewApprovalResponse> approveReview(
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId) {

        Review review = adminReviewService.approveReview(reviewId);

        return ResponseEntity.ok(new ReviewApprovalResponse(
                review.getId(),
                review.getApprovalStatus().name(),
                "후기가 승인되었습니다. 일반 사용자에게 노출됩니다."
        ));
    }

    @Operation(summary = "[2단계] 후기 반려", description = "후기를 반려하고 반려 이메일을 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "반려 성공"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @PatchMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<ReviewApprovalResponse> rejectReview(
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId) {

        Review review = adminReviewService.rejectReview(reviewId);

        return ResponseEntity.ok(new ReviewApprovalResponse(
                review.getId(),
                review.getApprovalStatus().name(),
                "후기가 반려되었습니다. 반려 이메일이 발송됩니다."
        ));
    }

    @Operation(summary = "후기 블라인드 처리", description = "후기를 블라인드 처리하거나 해제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @PatchMapping("/reviews/{reviewId}/blind")
    public ResponseEntity<ReviewApprovalResponse> blindReview(
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody BlindReviewRequest request) {

        Review review = adminReviewService.blindReview(reviewId, request.blurred());

        String message = request.blurred()
                ? "후기가 블라인드 처리되었습니다"
                : "블라인드가 해제되었습니다";

        return ResponseEntity.ok(new ReviewApprovalResponse(
                review.getId(),
                review.getApprovalStatus().name(),
                message
        ));
    }

    private AdminReviewListResponse.AdminReviewSummary toSummary(PendingReviewInfo info) {
        return new AdminReviewListResponse.AdminReviewSummary(
                info.reviewId(),
                info.lectureId(),
                info.lectureName(),
                info.memberId(),
                info.userName(),
                info.nickname(),
                info.score(),
                info.certificateId(),
                info.certificateApprovalStatus().name(),
                info.reviewApprovalStatus().name(),
                info.createdAt().format(FORMATTER)
        );
    }

    private AdminReviewDetailResponse toDetailResponse(PendingReviewInfo info) {
        List<AdminReviewDetailResponse.DetailScore> detailScores = info.details().stream()
                .map(d -> new AdminReviewDetailResponse.DetailScore(
                        d.getCategory().name(),
                        d.getScore(),
                        d.getComment()
                ))
                .collect(Collectors.toList());

        return new AdminReviewDetailResponse(
                info.reviewId(),
                info.lectureId(),
                info.lectureName(),
                info.memberId(),
                info.userName(),
                info.nickname(),
                info.comment(),
                info.score(),
                info.reviewApprovalStatus().name(),
                info.certificateId(),
                info.certificateApprovalStatus().name(),
                detailScores,
                info.createdAt().format(FORMATTER)
        );
    }
}
