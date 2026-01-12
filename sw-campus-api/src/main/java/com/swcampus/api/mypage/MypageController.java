package com.swcampus.api.mypage;

import com.swcampus.api.mypage.request.UpsertSurveyRequest;
import com.swcampus.api.mypage.request.UpdateProfileRequest;
import com.swcampus.api.mypage.request.VerifyPasswordRequest;
import com.swcampus.api.mypage.response.MyCompletedLectureResponse;
import com.swcampus.api.mypage.response.MyLectureListResponse;
import com.swcampus.api.mypage.response.MypageProfileResponse;
import com.swcampus.api.mypage.response.OrganizationInfoResponse;
import com.swcampus.api.mypage.response.SurveyResponse;
import com.swcampus.api.mypage.response.VerifyPasswordResponse;
import com.swcampus.api.review.response.ReviewResponse;
import com.swcampus.api.exception.FileProcessingException;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.mypage.MypageService;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewWithNickname;
import com.swcampus.domain.survey.MemberSurveyService;
import com.swcampus.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "마이페이지", description = "마이페이지 관련 API - [공통]: 일반 사용자/기관 모두 사용 가능, [일반 사용자 전용]: 일반 수강생만 사용 가능, [기관 전용]: 교육 기관만 사용 가능")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class MypageController {

    private final MemberService memberService;
    private final LectureService lectureService;
    private final MemberSurveyService memberSurveyService;
    private final OrganizationService organizationService;
    private final MypageService mypageService;
    private final ReviewService reviewService;

    @Operation(summary = "[공통] 비밀번호 확인", description = "[공통] 회원정보 수정 화면 진입 전 비밀번호를 확인합니다. 소셜 로그인 사용자는 비밀번호 검증 없이 항상 true를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @PostMapping("/verify-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(
        @CurrentMember MemberPrincipal member,
        @Valid @RequestBody VerifyPasswordRequest request
    ) {
        boolean isValid = memberService.validatePassword(member.memberId(), request.password());
        return ResponseEntity.ok(VerifyPasswordResponse.from(isValid));
    }

    @Operation(summary = "[공통] 내 정보 조회", description = "[공통] 로그인한 사용자의 프로필 정보를 조회합니다. 일반 사용자와 기관 모두 사용 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
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

    @Operation(summary = "[공통] 내 정보 수정", description = "[공통] 로그인한 사용자의 프로필 정보(닉네임, 연락처, 주소)를 수정합니다. 일반 사용자와 기관 모두 사용 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "잘못된 요청입니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
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

    @Operation(summary = "[일반 사용자 전용] 내 수강 완료 강의 조회", description = "[일반 사용자 전용] 수료증을 등록한 강의 목록을 조회합니다. 수료증 승인 상태(PENDING/APPROVED/REJECTED)와 관계없이 모든 등록된 수료증을 반환합니다. 각 강의에 대해 후기 작성 가능 여부(canWriteReview)를 함께 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (없을 경우 빈 배열)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "일반 사용자가 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "일반 사용자만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/completed-lectures")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MyCompletedLectureResponse>> getMyCompletedLectures(@CurrentMember MemberPrincipal member) {
        var completedLectures = mypageService.getCompletedLectures(member.memberId());
        var response = completedLectures.stream()
            .map(MyCompletedLectureResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[일반 사용자 전용] 수강 완료 강의 후기 상세 조회", description = "[일반 사용자 전용] 수강 완료한 강의에 대해 본인이 작성한 후기 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "일반 사용자가 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "일반 사용자만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "404", description = "해당 강의에 대한 후기가 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 404, "message": "해당 강의에 대한 후기가 없습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/completed-lectures/{lectureId}/review")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> getMyCompletedLectureReview(
        @CurrentMember MemberPrincipal member,
        @Parameter(description = "강의 ID", required = true)
        @PathVariable("lectureId") Long lectureId
    ) {
        ReviewWithNickname reviewWithNickname = reviewService.getMyReviewWithNicknameByLecture(
            member.memberId(), lectureId);
        return ResponseEntity.ok(ReviewResponse.from(
            reviewWithNickname.review(), reviewWithNickname.nickname()));
    }

    @Operation(summary = "[일반 사용자 전용] 설문조사 조회", description = "[일반 사용자 전용] 강의 추천을 위한 나의 설문조사 정보를 조회합니다. 전공, 부트캠프 수료 여부, 희망 직무 등의 정보를 확인할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "일반 사용자가 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "일반 사용자만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/survey")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SurveyResponse> getSurvey(@CurrentMember MemberPrincipal member) {
        return memberSurveyService.findSurveyByMemberId(member.memberId())
            .map(SurveyResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(SurveyResponse.empty()));
    }

    @Operation(summary = "[일반 사용자 전용] 설문조사 등록/수정", description = "[일반 사용자 전용] 강의 추천을 위한 설문조사 정보를 등록하거나 수정합니다. 이미 등록된 경우 덮어씁니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "잘못된 요청입니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "일반 사용자가 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "일반 사용자만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
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

    @Operation(summary = "[기관 전용] 등록 강의 목록 조회", description = "[기관 전용] 우리 기관에서 등록한 강의 목록을 조회합니다. 강의 상태, 수강생 수 등을 확인할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "기관 회원만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
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

    @Operation(summary = "[기관 전용] 등록 강의 상세 조회", description = "[기관 전용] 우리 기관에서 등록한 강의의 상세 정보를 조회합니다. 승인 상태와 관계없이 조회할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아니거나 본인 강의가 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "본인 기관의 강의만 조회할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 404, "message": "강의를 찾을 수 없습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<LectureResponse> getMyLectureDetail(
        @CurrentMember MemberPrincipal member,
        @Parameter(description = "강의 ID", required = true)
        @PathVariable("lectureId") Long lectureId
    ) {
        Organization org = organizationService.getOrganizationByUserId(member.memberId());
        Lecture lecture = lectureService.getLecture(lectureId);

        // 본인 기관의 강의인지 확인
        if (!org.getId().equals(lecture.getOrgId())) {
            throw new org.springframework.security.access.AccessDeniedException("본인 기관의 강의만 조회할 수 있습니다.");
        }

        return ResponseEntity.ok(LectureResponse.from(lecture, org));
    }

    @Operation(summary = "[기관 전용] 기관 정보 조회", description = "[기관 전용] 우리 기관의 상세 정보를 조회합니다. 기관명, 설명, 로고, 시설 이미지, 정부 인증 정보 등을 확인할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "기관 회원만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationInfoResponse> getOrganization(@CurrentMember MemberPrincipal member) {
        Organization org = organizationService.getOrganizationByUserId(member.memberId());
        Member memberEntity = memberService.getMember(member.memberId());
        return ResponseEntity.ok(OrganizationInfoResponse.from(org, memberEntity));
    }

    @Operation(summary = "[기관 전용] 기관 정보 수정", description = "[기관 전용] 우리 기관의 정보를 수정합니다. 기관명, 설명, 로고, 시설 이미지, 사업자등록증 등을 업로드할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "잘못된 요청입니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 403, "message": "기관 회원만 접근할 수 있습니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @PatchMapping(value = "/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> updateOrganization(
        @CurrentMember MemberPrincipal member,
        @Parameter(description = "기관명", example = "SW Campus")
        @RequestPart(name = "organizationName") String organizationName,
        @Parameter(description = "기관 설명")
        @RequestPart(name = "description", required = false) String description,
        @Parameter(description = "전화번호", example = "02-1234-5678")
        @RequestPart(name = "phone", required = false) String phone,
        @Parameter(description = "주소", example = "서울시 강남구 테헤란로 123")
        @RequestPart(name = "location", required = false) String location,
        @Parameter(description = "홈페이지 URL", example = "https://example.com")
        @RequestPart(name = "homepage", required = false) String homepage,
        @Parameter(description = "정부 인증 정보", example = "HRD-Net 인증")
        @RequestPart(name = "govAuth", required = false) String govAuth,
        @Parameter(description = "사업자등록증 파일")
        @RequestPart(name = "businessRegistration", required = false) MultipartFile businessRegistration,
        @Parameter(description = "기관 로고 이미지")
        @RequestPart(name = "logo", required = false) MultipartFile logo,
        @Parameter(description = "시설 이미지 1")
        @RequestPart(name = "facilityImage1", required = false) MultipartFile facilityImage1,
        @Parameter(description = "시설 이미지 2")
        @RequestPart(name = "facilityImage2", required = false) MultipartFile facilityImage2,
        @Parameter(description = "시설 이미지 3")
        @RequestPart(name = "facilityImage3", required = false) MultipartFile facilityImage3,
        @Parameter(description = "시설 이미지 4")
        @RequestPart(name = "facilityImage4", required = false) MultipartFile facilityImage4
    ) {
        // Update Organization Info (APPROVED 또는 REJECTED 상태의 기관 수정 가능, REJECTED는 수정 시 PENDING으로 변경됨)
        Organization org = organizationService.getOrganizationByUserId(member.memberId());

        // Update Member Info (Phone, Address)
        memberService.updateProfile(member.memberId(), null, phone, location);

        try {
            var params = new com.swcampus.domain.organization.dto.UpdateOrganizationParams(
                organizationName,
                description,
                homepage,
                govAuth,
                toFileUploadData(businessRegistration),
                toFileUploadData(logo),
                toFileUploadData(facilityImage1),
                toFileUploadData(facilityImage2),
                toFileUploadData(facilityImage3),
                toFileUploadData(facilityImage4)
            );
            organizationService.updateOrganization(org.getId(), member.memberId(), params);
        } catch (java.io.IOException e) {
            throw new FileProcessingException("파일 처리 중 오류가 발생했습니다.", e);
        }

        return ResponseEntity.ok().build();
    }

    private com.swcampus.domain.organization.dto.UpdateOrganizationParams.FileUploadData toFileUploadData(
            MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return new com.swcampus.domain.organization.dto.UpdateOrganizationParams.FileUploadData(
            file.getBytes(), file.getOriginalFilename(), file.getContentType());
    }
}
