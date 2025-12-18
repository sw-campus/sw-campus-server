package com.swcampus.api.mypage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "내 정보 수정 요청")
public record UpdateProfileRequest(
    @Schema(description = "닉네임", example = "dev_master")
    @Size(max = 20, message = "닉네임은 20자 이내여야 합니다.")
    String nickname,

    @Schema(description = "전화번호", example = "01012345678")
    @Pattern(regexp = "^(0\\d{8,10})?$", message = "전화번호 형식이 올바르지 않습니다. (하이픈 없이 9~11자리 숫자)")
    String phone,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    String location
) {
}
