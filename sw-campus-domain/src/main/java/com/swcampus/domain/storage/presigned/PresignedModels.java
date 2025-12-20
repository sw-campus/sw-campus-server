package com.swcampus.domain.storage.presigned;

import java.util.List;
import java.util.Map;

/**
 * Presigned upload DTO models used across layers.
 */
public class PresignedModels {

    public record PresignedSingleUpload(
            String uploadUrl,
            String method,
            Map<String, String> headers,
            String key,
            String publicUrl
    ) {}

    public record MultipartInitResponse(
            String uploadId,
            String key,
            long partSize,
            String publicUrl
    ) {}

    public record PresignedPart(
            int partNumber,
            String uploadUrl,
            Map<String, String> headers
    ) {}

    public record CompletedPart(
            int partNumber,
            String eTag
    ) {}

    public record SignPartsRequest(
            String key,
            String uploadId,
            List<Integer> partNumbers
    ) {}
}
