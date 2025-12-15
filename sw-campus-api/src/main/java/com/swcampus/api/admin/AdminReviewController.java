package com.swcampus.api.admin;

import com.swcampus.api.admin.request.BlindReviewRequest;
import com.swcampus.api.admin.response.*;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.review.AdminReviewService;
import com.swcampus.domain.review.Review;
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
    private final LectureRepository lectureRepository;
    private final MemberRepository memberRepository;
    private final CertificateRepository certificateRepository;

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
        List<Review> reviews = adminReviewService.getPendingReviews();

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

        String lectureName = lectureRepository.findById(certificate.getLectureId())
                .map(Lecture::getLectureName)
                .orElse("알 수 없음");

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

        Review review = adminReviewService.getReview(reviewId);

        return ResponseEntity.ok(toDetailResponse(review));
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

    private AdminReviewListResponse.AdminReviewSummary toSummary(Review review) {
        String lectureName = lectureRepository.findById(review.getLectureId())
                .map(Lecture::getLectureName)
                .orElse("알 수 없음");

        Member member = memberRepository.findById(review.getMemberId()).orElse(null);
        String userName = member != null ? member.getName() : "알 수 없음";
        String nickname = member != null ? member.getNickname() : "알 수 없음";

        // 수료증 상태 조회
        Certificate certificate = certificateRepository.findById(review.getCertificateId())
                .orElse(null);
        String certStatus = certificate != null ? certificate.getApprovalStatus().name() : "PENDING";

        return new AdminReviewListResponse.AdminReviewSummary(
                review.getId(),
                review.getLectureId(),
                lectureName,
                review.getMemberId(),
                userName,
                nickname,
                review.getScore(),
                review.getCertificateId(),
                certStatus,
                review.getApprovalStatus().name(),
                review.getCreatedAt().format(FORMATTER)
        );
    }

    private AdminReviewDetailResponse toDetailResponse(Review review) {
        String lectureName = lectureRepository.findById(review.getLectureId())
                .map(Lecture::getLectureName)
                .orElse("알 수 없음");

        Member member = memberRepository.findById(review.getMemberId()).orElse(null);
        String userName = member != null ? member.getName() : "알 수 없음";
        String nickname = member != null ? member.getNickname() : "알 수 없음";

        // 수료증 상태 조회
        Certificate certificate = certificateRepository.findById(review.getCertificateId())
                .orElse(null);
        String certStatus = certificate != null ? certificate.getApprovalStatus().name() : "PENDING";

        List<AdminReviewDetailResponse.DetailScore> detailScores = review.getDetails().stream()
                .map(d -> new AdminReviewDetailResponse.DetailScore(
                        d.getCategory().name(),
                        d.getScore(),
                        d.getComment()
                ))
                .collect(Collectors.toList());

        return new AdminReviewDetailResponse(
                review.getId(),
                review.getLectureId(),
                lectureName,
                review.getMemberId(),
                userName,
                nickname,
                review.getComment(),
                review.getScore(),
                review.getApprovalStatus().name(),
                review.getCertificateId(),
                certStatus,
                detailScores,
                review.getCreatedAt().format(FORMATTER)
        );
    }
}
