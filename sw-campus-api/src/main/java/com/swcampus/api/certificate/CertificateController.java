package com.swcampus.api.certificate;

import com.swcampus.api.certificate.response.CertificateCheckResponse;
import com.swcampus.api.certificate.response.CertificateVerifyResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Certificate", description = "수료증 인증 API")
@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "수료증 인증 여부 확인", description = "해당 강의에 대한 수료증 인증 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @GetMapping("/check")
    public ResponseEntity<CertificateCheckResponse> checkCertificate(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", required = true)
            @RequestParam(name = "lectureId") Long lectureId) {

        Long memberId = member.memberId();

        return certificateService.checkCertificate(memberId, lectureId)
                .map(cert -> ResponseEntity.ok(CertificateCheckResponse.certified(
                        cert.getId(),
                        cert.getImageKey(),
                        cert.getApprovalStatus().name(),
                        cert.getCreatedAt()
                )))
                .orElseGet(() -> ResponseEntity.ok(CertificateCheckResponse.notCertified()));
    }

    @Operation(summary = "수료증 인증", description = "수료증 이미지를 업로드하여 OCR 검증 후 인증합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공"),
        @ApiResponse(responseCode = "400", description = "강의명 불일치",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 400, "message": "강의명이 일치하지 않습니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                    """))),
        @ApiResponse(responseCode = "409", description = "이미 인증된 수료증",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"status": 409, "message": "이미 인증된 수료증입니다", "timestamp": "2025-12-09T12:00:00"}
                    """)))
    })
    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificateVerifyResponse> verifyCertificate(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", required = true)
            @RequestPart(name = "lectureId") String lectureIdStr,
            @Parameter(description = "수료증 이미지", required = true)
            @RequestPart(name = "image") MultipartFile image) throws IOException {

        Long memberId = member.memberId();
        Long lectureId = Long.parseLong(lectureIdStr);

        Certificate certificate = certificateService.verifyCertificate(
                memberId,
                lectureId,
                image.getBytes(),
                image.getOriginalFilename(),
                image.getContentType()
        );

        return ResponseEntity.ok(CertificateVerifyResponse.of(
                certificate.getId(),
                certificate.getLectureId(),
                certificate.getImageKey(),
                certificate.getApprovalStatus().name()
        ));
    }
}
