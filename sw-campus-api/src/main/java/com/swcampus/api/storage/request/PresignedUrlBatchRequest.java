package com.swcampus.api.storage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "배치 Presigned URL 요청")
public record PresignedUrlBatchRequest(
        @Schema(description = "S3 key 목록", example = "[\"lectures/2024/01/01/uuid.jpg\", \"organizations/2024/01/02/uuid.jpg\"]")
        @NotEmpty(message = "keys는 비어있을 수 없습니다")
        @Size(max = 50, message = "최대 50개까지 요청 가능합니다")
        List<String> keys
) {
}
