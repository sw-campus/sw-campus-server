package com.swcampus.api.mypage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "기관 정보 수정 요청")
public record UpdateOrganizationRequest(
    @Schema(description = "기관명", example = "SW Campus")
    @NotBlank(message = "기관명은 필수입니다.")
    String organizationName,

    @Schema(description = "대표자명", example = "홍길동")
    @NotBlank(message = "대표자명은 필수입니다.")
    String representativeName,

    @Schema(description = "전화번호", example = "02-1234-5678")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    String phone,

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @Schema(description = "재직증명서 파일")
    MultipartFile businessRegistration
) {
}
