package com.swcampus.api.storage;

import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.storage.request.PresignedUrlBatchRequest;
import com.swcampus.api.storage.request.PresignedUploadRequest;
import com.swcampus.api.storage.response.PresignedUploadResponse;
import com.swcampus.api.storage.response.PresignedUrlResponse;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.storage.PresignedUrlService;
import com.swcampus.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Storage", description = "스토리지 API")
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final PresignedUrlService presignedUrlService;

    @Operation(summary = "Presigned GET URL 발급", description = "S3 객체에 접근하기 위한 Presigned GET URL을 발급합니다. Public 파일은 인증 없이, Private 파일은 관리자만 접근 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공"),
            @ApiResponse(responseCode = "403", description = "Private 파일에 대한 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 403, "message": "접근 권한이 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @GetMapping("/presigned-urls")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Parameter(description = "S3 객체 key", required = true, example = "lectures/2024/01/01/uuid.jpg")
            @RequestParam("key") String key,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MemberPrincipal member) {

        boolean isAdmin = isAdmin(member);
        var presignedUrl = presignedUrlService.getPresignedUrl(key, isAdmin);

        return ResponseEntity.ok(PresignedUrlResponse.from(presignedUrl));
    }

    @Operation(summary = "배치 Presigned GET URL 발급", description = "여러 S3 객체에 대한 Presigned GET URL을 일괄 발급합니다. Private 파일에 대해 권한이 없으면 해당 key의 값은 null로 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공"),
            @ApiResponse(responseCode = "400", description = "요청 개수가 50개 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 400, "message": "요청 개수가 50개를 초과했습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @PostMapping("/presigned-urls/batch")
    public ResponseEntity<Map<String, String>> getPresignedUrlBatch(
            @Valid @RequestBody PresignedUrlBatchRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal MemberPrincipal member) {

        boolean isAdmin = isAdmin(member);
        var urls = presignedUrlService.getPresignedUrls(request.keys(), isAdmin);

        return ResponseEntity.ok(urls);
    }

    @Operation(summary = "Presigned Upload URL 발급", description = "S3에 파일을 업로드하기 위한 Presigned PUT URL을 발급합니다. 카테고리별 권한 체크가 적용됩니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 카테고리",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 400, "message": "지원하지 않는 카테고리입니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "403", description = "해당 카테고리에 대한 업로드 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 403, "message": "접근 권한이 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @PostMapping("/presigned-urls/upload")
    public ResponseEntity<PresignedUploadResponse> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUploadRequest request,
            @CurrentMember MemberPrincipal member) {

        var presignedUploadUrl = presignedUrlService.getPresignedUploadUrl(
                request.category(),
                request.fileName(),
                request.contentType(),
                member.role()
        );

        return ResponseEntity.ok(PresignedUploadResponse.from(presignedUploadUrl));
    }

    private boolean isAdmin(MemberPrincipal member) {
        return member != null && member.role() == Role.ADMIN;
    }
}
