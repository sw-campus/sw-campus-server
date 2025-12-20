package com.swcampus.api.storage.dto;

import com.swcampus.domain.storage.presigned.PresignedModels.PresignedSingleUpload;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "SingleUploadResponse", description = "단일 Presigned 업로드 URL 발급 응답")
public class SingleUploadResponse {

    @Schema(description = "S3 Presigned 업로드 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/key?X-Amz-Algorithm=...")
    private String uploadUrl;

    @Schema(description = "HTTP 메서드", example = "PUT")
    private String method;

    @Schema(description = "업로드 시 추가로 포함할 헤더")
    private Map<String, String> headers;

    @Schema(description = "저장될 S3 키", example = "certificates/2025/12/21/uuid.jpg")
    private String key;

    @Schema(description = "객체 접근 URL (private의 경우 형식상 URL)")
    private String publicUrl;

    public static SingleUploadResponse from(PresignedSingleUpload src) {
        return new SingleUploadResponse(
                src.uploadUrl(),
                src.method(),
                src.headers(),
                src.key(),
                src.publicUrl()
        );
    }
}
