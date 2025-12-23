package com.swcampus.api.storage.response;

import com.swcampus.domain.storage.PresignedUrlService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 응답")
public record PresignedUrlResponse(
        @Schema(description = "Presigned URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/lectures/...?X-Amz-Algorithm=...")
        String url,

        @Schema(description = "만료 시간 (초)", example = "900")
        int expiresIn
) {
    public static PresignedUrlResponse from(PresignedUrlService.PresignedUrl presignedUrl) {
        return new PresignedUrlResponse(presignedUrl.url(), presignedUrl.expiresIn());
    }
}
