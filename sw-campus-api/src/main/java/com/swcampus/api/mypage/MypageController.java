package com.swcampus.api.mypage;

import com.swcampus.api.mypage.request.SurveyRequest;
import com.swcampus.api.mypage.request.UpdateOrganizationRequest;
import com.swcampus.api.mypage.request.UpdateProfileRequest;
import com.swcampus.api.mypage.response.MyLectureListResponse;
import com.swcampus.api.mypage.response.MyReviewListResponse;
import com.swcampus.api.mypage.response.MypageProfileResponse;
import com.swcampus.api.mypage.response.OrganizationInfoResponse;
import com.swcampus.api.mypage.response.SurveyResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@SecurityRequirement(name = "cookieAuth")
public class MypageController {

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/profile")
    public ResponseEntity<MypageProfileResponse> getProfile(@CurrentMember MemberPrincipal member) {
        return null;
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
        @CurrentMember MemberPrincipal member,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return null;
    }

    @Operation(summary = "내 후기 목록 조회", description = "내가 작성한 후기 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/reviews")
    public ResponseEntity<List<MyReviewListResponse>> getMyReviews(@CurrentMember MemberPrincipal member) {
        return null;
    }

    @Operation(summary = "설문조사 조회", description = "나의 설문조사 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/survey")
    public ResponseEntity<SurveyResponse> getSurvey(@CurrentMember MemberPrincipal member) {
        return null;
    }

    @Operation(summary = "설문조사 등록/수정", description = "설문조사 정보를 등록하거나 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/survey")
    public ResponseEntity<Void> saveSurvey(
        @CurrentMember MemberPrincipal member,
        @Valid @RequestBody SurveyRequest request
    ) {
        return null;
    }

    @Operation(summary = "내 강의 목록 조회", description = "내가 등록한(기관) 강의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @GetMapping("/lectures")
    public ResponseEntity<List<MyLectureListResponse>> getMyLectures(@CurrentMember MemberPrincipal member) {
        return null;
    }

    @Operation(summary = "기관 정보 조회", description = "나의 기관 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @GetMapping("/organization")
    public ResponseEntity<OrganizationInfoResponse> getOrganization(@CurrentMember MemberPrincipal member) {
        return null;
    }

    @Operation(summary = "기관 정보 수정", description = "기관 정보를 수정합니다. (재직증명서 파일 포함)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "기관 회원이 아님")
    })
    @PatchMapping(value = "/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateOrganization(
        @CurrentMember MemberPrincipal member,
        @Valid @ModelAttribute UpdateOrganizationRequest request
    ) {
        return null;
    }
}
