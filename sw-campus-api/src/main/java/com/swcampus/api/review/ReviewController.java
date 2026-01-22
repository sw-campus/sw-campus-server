package com.swcampus.api.review;

import com.swcampus.api.review.request.CreateReviewRequest;
import com.swcampus.api.review.request.UpdateReviewRequest;
import com.swcampus.api.review.response.ReviewEligibilityResponse;
import com.swcampus.api.review.response.ReviewResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.security.OptionalCurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.domain.review.ReviewEligibility;
import com.swcampus.domain.review.ReviewAccessService;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewWithNickname;
import com.swcampus.domain.review.dto.ReviewBlindStatus;
import com.swcampus.api.review.response.ReviewBlindStatusResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Review", description = "후기 API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewAccessService reviewAccessService;

    @Operation(summary = "후기 작성 가능 여부 확인", description = "닉네임 설정, 수료증 인증, 기존 후기 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/eligibility")
    public ResponseEntity<ReviewEligibilityResponse> checkEligibility(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", required = true)
            @RequestParam(name = "lectureId") Long lectureId) {

        Long memberId = member.memberId();
        ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

        return ResponseEntity.ok(ReviewEligibilityResponse.from(eligibility));
    }

    @Operation(summary = "후기 작성", description = "수료증 인증이 완료된 강의에 후기를 작성합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "작성 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "reviewId": 1,
                      "lectureId": 10,
                      "memberId": 5,
                      "nickname": "개발자김철수",
                      "comment": "전체적으로 만족스러운 강의였습니다. 실무에 바로 적용할 수 있는 내용이 많았습니다.",
                      "score": 4.3,
                      "detailScores": [
                        {"category": "TEACHER", "score": 4.5, "comment": "강사님이 친절하고 설명을 잘 해주셔서 이해하기 쉬웠습니다. 질문에도 성심성의껏 답변해주셨습니다."},
                        {"category": "CURRICULUM", "score": 4.0, "comment": "커리큘럼이 체계적이고 단계별로 잘 구성되어 있었습니다. 다만 후반부가 조금 빠르게 진행되었습니다."},
                        {"category": "MANAGEMENT", "score": 4.5, "comment": "출결 관리와 학습 지원이 잘 되었습니다. 담당자분이 친절하게 안내해주셨습니다."},
                        {"category": "FACILITY", "score": 4.0, "comment": "강의실 시설이 깨끗하고 쾌적했습니다. 개인 모니터와 책상 공간도 충분했습니다."},
                        {"category": "PROJECT", "score": 4.5, "comment": "팀 프로젝트를 통해 실무 경험을 쌓을 수 있었습니다. 포트폴리오로 활용하기 좋았습니다."}
                      ],
                      "approvalStatus": "PENDING",
                      "blurred": false,
                      "createdAt": "2025-12-10T10:30:00",
                      "updatedAt": "2025-12-10T10:30:00"
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "잘못된 요청입니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "수료증 인증 필요",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "수료증 인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "409", description = "이미 작성한 후기 존재",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 409, "message": "이미 작성한 후기가 존재합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @CurrentMember MemberPrincipal member,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "후기 작성 요청",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateReviewRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "lectureId": 10,
                          "comment": "전체적으로 만족스러운 강의였습니다. 실무에 바로 적용할 수 있는 내용이 많았습니다.",
                          "detailScores": [
                            {"category": "TEACHER", "score": 4.5, "comment": "강사님이 친절하고 설명을 잘 해주셔서 이해하기 쉬웠습니다. 질문에도 성심성의껏 답변해주셨습니다."},
                            {"category": "CURRICULUM", "score": 4.0, "comment": "커리큘럼이 체계적이고 단계별로 잘 구성되어 있었습니다. 다만 후반부가 조금 빠르게 진행되었습니다."},
                            {"category": "MANAGEMENT", "score": 4.5, "comment": "출결 관리와 학습 지원이 잘 되었습니다. 담당자분이 친절하게 안내해주셨습니다."},
                            {"category": "FACILITY", "score": 4.0, "comment": "강의실 시설이 깨끗하고 쾌적했습니다. 개인 모니터와 책상 공간도 충분했습니다."},
                            {"category": "PROJECT", "score": 4.5, "comment": "팀 프로젝트를 통해 실무 경험을 쌓을 수 있었습니다. 포트폴리오로 활용하기 좋았습니다."}
                          ]
                        }
                        """)))
            @Valid @RequestBody CreateReviewRequest request) {

        Long memberId = member.memberId();

        List<ReviewDetail> details = request.detailScores().stream()
                .map(d -> ReviewDetail.create(
                        ReviewCategory.valueOf(d.category()),
                        d.score(),
                        d.comment()
                ))
                .collect(Collectors.toList());

        Review review = reviewService.createReview(
                memberId,
                request.lectureId(),
                request.comment(),
                details
        );

        String nickname = reviewService.getNickname(memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReviewResponse.from(review, nickname));
    }

    @Operation(summary = "후기 수정", description = "본인이 작성한 후기를 수정합니다. 승인된 후기는 수정할 수 없습니다. PENDING 또는 REJECTED 상태의 후기만 수정 가능합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "reviewId": 1,
                      "lectureId": 10,
                      "memberId": 5,
                      "nickname": "개발자김철수",
                      "comment": "수정된 후기입니다. 추가로 느낀 점을 보완했습니다.",
                      "score": 4.5,
                      "detailScores": [
                        {"category": "TEACHER", "score": 5.0, "comment": "강사님이 정말 훌륭하셨습니다. 어려운 개념도 쉽게 설명해주셔서 완벽히 이해할 수 있었습니다."},
                        {"category": "CURRICULUM", "score": 4.5, "comment": "커리큘럼 구성이 탄탄했습니다. 기초부터 심화까지 체계적으로 배울 수 있었습니다."},
                        {"category": "MANAGEMENT", "score": 4.5, "comment": "학습 관리가 철저했습니다. 출결 및 과제 관리가 잘 되어 집중할 수 있었습니다."},
                        {"category": "FACILITY", "score": 4.0, "comment": "시설이 깨끗하고 쾌적했습니다. 냉난방도 적절했습니다."},
                        {"category": "PROJECT", "score": 4.5, "comment": "실전 프로젝트 경험이 매우 유익했습니다. 팀원들과 협업하는 방법도 배웠습니다."}
                      ],
                      "approvalStatus": "PENDING",
                      "blurred": false,
                      "createdAt": "2025-12-10T10:30:00",
                      "updatedAt": "2025-12-15T14:20:00"
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "잘못된 요청입니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "본인 후기만 수정 가능 / 승인된 후기 수정 불가",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "접근 권한이 없습니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 404, "message": "후기를 찾을 수 없습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "후기 수정 요청",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateReviewRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "comment": "수정된 후기입니다. 추가로 느낀 점을 보완했습니다.",
                          "detailScores": [
                            {"category": "TEACHER", "score": 5.0, "comment": "강사님이 정말 훌륭하셨습니다. 어려운 개념도 쉽게 설명해주셔서 완벽히 이해할 수 있었습니다."},
                            {"category": "CURRICULUM", "score": 4.5, "comment": "커리큘럼 구성이 탄탄했습니다. 기초부터 심화까지 체계적으로 배울 수 있었습니다."},
                            {"category": "MANAGEMENT", "score": 4.5, "comment": "학습 관리가 철저했습니다. 출결 및 과제 관리가 잘 되어 집중할 수 있었습니다."},
                            {"category": "FACILITY", "score": 4.0, "comment": "시설이 깨끗하고 쾌적했습니다. 냉난방도 적절했습니다."},
                            {"category": "PROJECT", "score": 4.5, "comment": "실전 프로젝트 경험이 매우 유익했습니다. 팀원들과 협업하는 방법도 배웠습니다."}
                          ]
                        }
                        """)))
            @Valid @RequestBody UpdateReviewRequest request) {

        Long memberId = member.memberId();

        List<ReviewDetail> details = request.detailScores().stream()
                .map(d -> ReviewDetail.create(
                        ReviewCategory.valueOf(d.category()),
                        d.score(),
                        d.comment()
                ))
                .collect(Collectors.toList());

        Review review = reviewService.updateReview(
                memberId,
                reviewId,
                request.comment(),
                details
        );

        String nickname = reviewService.getNickname(memberId);
        return ResponseEntity.ok(ReviewResponse.from(review, nickname));
    }

    @Operation(summary = "후기 상세 조회", description = "후기 ID로 상세 정보를 조회합니다. 승인된 후기만 일반 사용자에게 노출되며, 본인이 작성한 후기는 승인 상태와 관계없이 조회할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "reviewId": 1,
                      "lectureId": 10,
                      "memberId": 5,
                      "nickname": "개발자김철수",
                      "comment": "전체적으로 만족스러운 강의였습니다. 실무에 바로 적용할 수 있는 내용이 많았습니다.",
                      "score": 4.3,
                      "detailScores": [
                        {"category": "TEACHER", "score": 4.5, "comment": "강사님이 친절하고 설명을 잘 해주셔서 이해하기 쉬웠습니다. 질문에도 성심성의껏 답변해주셨습니다."},
                        {"category": "CURRICULUM", "score": 4.0, "comment": "커리큘럼이 체계적이고 단계별로 잘 구성되어 있었습니다. 다만 후반부가 조금 빠르게 진행되었습니다."},
                        {"category": "MANAGEMENT", "score": 4.5, "comment": "출결 관리와 학습 지원이 잘 되었습니다. 담당자분이 친절하게 안내해주셨습니다."},
                        {"category": "FACILITY", "score": 4.0, "comment": "강의실 시설이 깨끗하고 쾌적했습니다. 개인 모니터와 책상 공간도 충분했습니다."},
                        {"category": "PROJECT", "score": 4.5, "comment": "팀 프로젝트를 통해 실무 경험을 쌓을 수 있었습니다. 포트폴리오로 활용하기 좋았습니다."}
                      ],
                      "approvalStatus": "APPROVED",
                      "blurred": false,
                      "createdAt": "2025-12-10T10:30:00",
                      "updatedAt": "2025-12-12T09:15:00"
                    }
                    """))),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음 (미승인 후기를 타인이 조회 시도한 경우 포함)")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @OptionalCurrentMember MemberPrincipal member,
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId) {

        Long requesterId = member != null ? member.memberId() : null;
        ReviewWithNickname reviewWithNickname = reviewService.getReviewWithNickname(reviewId, requesterId);

        return ResponseEntity.ok(ReviewResponse.from(
                reviewWithNickname.review(),
                reviewWithNickname.nickname()
        ));
    }


    @Operation(summary = "블라인드 해제 상태 조회", description = "현재 로그인한 사용자의 리뷰 블라인드 해제 상태를 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReviewBlindStatusResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "isUnblinded": false,
                      "hasApprovedReview": false,
                      "hasSurveyCompleted": false
                    }
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = com.swcampus.api.exception.ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/blind-status")
    public ResponseEntity<ReviewBlindStatusResponse> getBlindStatus(@CurrentMember MemberPrincipal member) {
        ReviewBlindStatus status = reviewAccessService.getBlindStatus(member.memberId());
        return ResponseEntity.ok(ReviewBlindStatusResponse.from(status));
    }
}
