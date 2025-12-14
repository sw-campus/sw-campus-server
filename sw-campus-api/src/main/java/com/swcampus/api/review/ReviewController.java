package com.swcampus.api.review;

import com.swcampus.api.review.request.CreateReviewRequest;
import com.swcampus.api.review.request.UpdateReviewRequest;
import com.swcampus.api.review.response.ReviewEligibilityResponse;
import com.swcampus.api.review.response.ReviewResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.domain.review.ReviewEligibility;
import com.swcampus.domain.review.ReviewService;
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
import java.util.stream.Collectors;

@Tag(name = "Review", description = "후기 API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MemberRepository memberRepository;

    @Operation(summary = "후기 작성 가능 여부 확인", description = "닉네임 설정, 수료증 인증, 기존 후기 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/eligibility")
    public ResponseEntity<ReviewEligibilityResponse> checkEligibility(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", required = true)
            @RequestParam Long lectureId) {

        Long memberId = member.memberId();
        ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

        return ResponseEntity.ok(ReviewEligibilityResponse.from(eligibility));
    }

    @Operation(summary = "후기 작성", description = "수료증 인증이 완료된 강의에 후기를 작성합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "작성 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "수료증 인증 필요"),
        @ApiResponse(responseCode = "409", description = "이미 작성한 후기 존재")
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @CurrentMember MemberPrincipal member,
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

        String nickname = getNickname(memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReviewResponse.from(review, nickname));
    }

    @Operation(summary = "후기 수정", description = "본인이 작성한 후기를 수정합니다. 승인된 후기는 수정할 수 없습니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "본인 후기만 수정 가능 / 승인된 후기 수정 불가"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId,
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

        String nickname = getNickname(memberId);
        return ResponseEntity.ok(ReviewResponse.from(review, nickname));
    }

    @Operation(summary = "후기 상세 조회", description = "후기 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "후기를 찾을 수 없음")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "후기 ID", required = true, name = "reviewId")
            @PathVariable("reviewId") Long reviewId) {

        Review review = reviewService.getReview(reviewId);
        String nickname = getNickname(review.getMemberId());

        return ResponseEntity.ok(ReviewResponse.from(review, nickname));
    }

    private String getNickname(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse(null);
    }
}
