package com.swcampus.api.storage.response;

import com.swcampus.domain.storage.PresignedUrlService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned Upload URL 응답")
public record PresignedUploadResponse(
        @Schema(description = "Presigned PUT URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/certificates/...?X-Amz-Algorithm=...")
        String uploadUrl,

        @Schema(description = "생성된 S3 key", example = "certificates/2024/01/01/uuid.jpg")
        String key,

        @Schema(description = "만료 시간 (초)", example = "900")
        int expiresIn
) {
    public static PresignedUploadResponse from(PresignedUrlService.PresignedUploadUrl presignedUploadUrl) {
        return new PresignedUploadResponse(
                presignedUploadUrl.uploadUrl(),
                presignedUploadUrl.key(),
                presignedUploadUrl.expiresIn()
        );
    }
}
