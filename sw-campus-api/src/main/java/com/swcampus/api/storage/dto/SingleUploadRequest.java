package com.swcampus.api.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "SingleUploadRequest", description = "단일 Presigned 업로드 URL 발급 요청")
public class SingleUploadRequest {

    @NotBlank
    @Schema(description = "상위 디렉터리(prefix)", example = "certificates")
    private String directory; // e.g., certificates, lectures, teachers, organizations, banners, thumbnails

    @NotBlank
    @Schema(description = "원본 파일명", example = "avatar.webp")
    private String fileName;

    @NotBlank
    @Schema(description = "MIME 타입", example = "image/webp")
    private String contentType;

    @NotNull
    @Schema(description = "파일 크기(바이트)", example = "123456")
    private Long contentLength;
}
