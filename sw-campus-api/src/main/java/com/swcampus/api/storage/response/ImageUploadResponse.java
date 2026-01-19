package com.swcampus.api.storage.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드 응답")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/posts/2024/01/01/uuid.jpg")
        String url,

        @Schema(description = "S3 key", example = "posts/2024/01/01/uuid.jpg")
        String key
) {
}
