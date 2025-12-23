package com.swcampus.api.mypage.response;

import com.swcampus.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 프로필 응답")
public record MypageProfileResponse(
    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "닉네임", example = "dev_master")
    String nickname,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    String location,

    @Schema(description = "가입 경로", example = "LOCAL")
    String provider,

    @Schema(description = "역할", example = "USER")
    String role,

    @Schema(description = "설문조사 완료 여부")
    Boolean hasSurvey
) {
    public static MypageProfileResponse from(Member member, boolean hasSurvey) {
        return new MypageProfileResponse(
            member.getEmail(),
            member.getName(),
            member.getNickname(),
            member.getPhone(),
            member.getLocation(),
            member.isSocialUser() ? "OAUTH" : "LOCAL",
            member.getRole().name(),
            hasSurvey
        );
    }
}
