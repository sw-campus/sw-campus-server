package com.swcampus.api.storage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Presigned Upload URL 요청")
public record PresignedUploadRequest(
        @Schema(description = "파일 카테고리", example = "certificates")
        @NotBlank(message = "category는 필수입니다")
        String category,

        @Schema(description = "원본 파일명", example = "certificate.jpg")
        @NotBlank(message = "fileName은 필수입니다")
        String fileName,

        @Schema(description = "MIME 타입", example = "image/jpeg")
        @NotBlank(message = "contentType은 필수입니다")
        String contentType
) {
}
