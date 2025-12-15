package com.swcampus.api.mypage;

import com.swcampus.api.mypage.request.UpsertSurveyRequest;
import com.swcampus.api.mypage.request.UpdateOrganizationRequest;
import com.swcampus.api.mypage.request.UpdateProfileRequest;
import com.swcampus.api.mypage.response.MyLectureListResponse;
import com.swcampus.api.mypage.response.MyReviewListResponse;
import com.swcampus.api.mypage.response.MypageProfileResponse;
import com.swcampus.api.mypage.response.OrganizationInfoResponse;
import com.swcampus.api.mypage.response.SurveyResponse;
import com.swcampus.api.exception.FileProcessingException;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지", description = "마이페이지 관련 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class MypageController {

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final LectureService lectureService;
    private final MemberSurveyService memberSurveyService;
    private final OrganizationService organizationService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MypageProfileResponse> getProfile(@CurrentMember MemberPrincipal member) {
        Member memberEntity = memberService.getMember(member.memberId());
        boolean hasSurvey = false;
        if (member.role() == Role.USER) {
            hasSurvey = memberSurveyService.existsByMemberId(member.memberId());
        }
        return ResponseEntity.ok(MypageProfileResponse.from(memberEntity, hasSurvey));
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateProfile(
        @CurrentMember MemberPrincipal member,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        memberService.updateProfile(member.memberId(), request.nickname(), request.phone(), request.location());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 후기 목록 조회", description = "내가 작성한 후기 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (없을 경우 빈 배열)"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MyReviewListResponse>> getMyReviews(@CurrentMember MemberPrincipal member) {
        List<Review> reviews = reviewService.findAllByMemberId(member.memberId());
        List<Long> lectureIds = reviews.stream().map(Review::getLectureId).toList();
        
        // Note: lectureService.getLectureNames uses 'IN' clause, so N+1 problem is avoided here.
        // However, be careful not to change this to individual queries in the future.
        Map<Long, String> lectureNames = lectureService.getLectureNames(lectureIds);

        List<MyReviewListResponse> response = reviews.stream()
            .map(review -> MyReviewListResponse.from(review, lectureNames.getOrDefault(review.getLectureId(), "Unknown")))
            .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "설문조사 조회", description = "나의 설문조사 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/survey")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SurveyResponse> getSurvey(@CurrentMember MemberPrincipal member) {
        return memberSurveyService.findSurveyByMemberId(member.memberId())
            .map(SurveyResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(SurveyResponse.empty()));
    }

    @Operation(summary = "설문조사 등록/수정", description = "설문조사 정보를 등록하거나 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/survey")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> saveSurvey(
        @CurrentMember MemberPrincipal member,
        @Valid @RequestBody UpsertSurveyRequest request
    ) {
        memberSurveyService.upsertSurvey(
            member.memberId(),
            request.major(),
            request.bootcampCompleted(),
            request.wantedJobs(),
            request.licenses(),
            request.hasGovCard(),
            request.affordableAmount()
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 강의 목록 조회", description = "내가 등록한(기관) 강의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @GetMapping("/lectures")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<List<MyLectureListResponse>> getMyLectures(@CurrentMember MemberPrincipal member) {
        Organization org = organizationService.getOrganizationByUserId(member.memberId());
        List<Lecture> lectures = lectureService.findAllByOrgId(org.getId());

        List<MyLectureListResponse> response = lectures.stream()
            .map(MyLectureListResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기관 정보 조회", description = "나의 기관 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @GetMapping("/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationInfoResponse> getOrganization(@CurrentMember MemberPrincipal member) {
        Organization org = organizationService.getOrganizationByUserId(member.memberId());
        Member memberEntity = memberService.getMember(member.memberId());
        return ResponseEntity.ok(OrganizationInfoResponse.from(org, memberEntity));
    }

    @Operation(summary = "기관 정보 수정", description = "기관 정보를 수정합니다. (재직증명서 파일 포함)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @PatchMapping(value = "/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> updateOrganization(
        @CurrentMember MemberPrincipal member,
        @Valid @ModelAttribute UpdateOrganizationRequest request
    ) {
        // Update Member Info (Phone, Address)
        memberService.updateProfile(member.memberId(), null, request.phone(), request.location());

        // Update Organization Info
        Organization org = organizationService.getOrganizationByUserId(member.memberId());

        byte[] fileContent = null;
        String fileName = null;
        String contentType = null;
        
        try {
            if (request.businessRegistration() != null && !request.businessRegistration().isEmpty()) {
                fileContent = request.businessRegistration().getBytes();
                fileName = request.businessRegistration().getOriginalFilename();
                contentType = request.businessRegistration().getContentType();
            }
        } catch (java.io.IOException e) {
            throw new FileProcessingException("파일 처리 중 오류가 발생했습니다.", e);
        }

        organizationService.updateOrganization(
            org.getId(),
            member.memberId(),
            request.organizationName(),
            null, // Description is not in request
            fileContent,
            fileName,
            contentType
        );

        return ResponseEntity.ok().build();
    }
}
