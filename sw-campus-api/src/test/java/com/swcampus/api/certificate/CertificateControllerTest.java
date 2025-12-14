package com.swcampus.api.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateService;
import com.swcampus.domain.certificate.exception.CertificateAlreadyExistsException;
import com.swcampus.domain.certificate.exception.CertificateLectureMismatchException;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.review.ApprovalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CertificateController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("CertificateController 테스트")
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CertificateService certificateService;

    private void setAuthentication(Long memberId) {
        MemberPrincipal principal = new MemberPrincipal(memberId, "user@example.com", Role.USER);
        UsernamePasswordAuthenticationToken authWithDetails = 
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authWithDetails);
    }

    @Nested
    @DisplayName("GET /api/v1/certificates/check")
    class CheckCertificate {

        @Test
        @DisplayName("수료증 인증됨 - certified: true")
        void checkCertificate_certified() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            setAuthentication(memberId);

            Certificate certificate = Certificate.of(
                    1L, memberId, lectureId,
                    "https://s3.../image.jpg", "SUCCESS",
                    ApprovalStatus.PENDING, LocalDateTime.now()
            );
            given(certificateService.checkCertificate(memberId, lectureId))
                    .willReturn(Optional.of(certificate));

            // when & then
            mockMvc.perform(get("/api/v1/certificates/check")
                            .param("lectureId", String.valueOf(lectureId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certified").value(true))
                    .andExpect(jsonPath("$.certificate_id").value(1))
                    .andExpect(jsonPath("$.image_url").value("https://s3.../image.jpg"))
                    .andExpect(jsonPath("$.approval_status").value("PENDING"));
        }

        @Test
        @DisplayName("수료증 미인증 - certified: false")
        void checkCertificate_notCertified() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            setAuthentication(memberId);

            given(certificateService.checkCertificate(memberId, lectureId))
                    .willReturn(Optional.empty());

            // when & then
            mockMvc.perform(get("/api/v1/certificates/check")
                            .param("lectureId", String.valueOf(lectureId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certified").value(false))
                    .andExpect(jsonPath("$.certificate_id").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/certificates/verify")
    class VerifyCertificate {

        @Test
        @DisplayName("수료증 인증 성공")
        void verifyCertificate_success() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            setAuthentication(memberId);

            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    "1".getBytes()
            );

            Certificate certificate = Certificate.of(
                    1L, memberId, lectureId,
                    "https://s3.../certificates/certificate.jpg", "SUCCESS",
                    ApprovalStatus.PENDING, LocalDateTime.now()
            );

            given(certificateService.verifyCertificate(
                    eq(memberId), eq(lectureId), any(byte[].class), anyString(), anyString()
            )).willReturn(certificate);

            // when & then
            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(image)
                            .file(lectureIdPart))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificate_id").value(1))
                    .andExpect(jsonPath("$.lecture_id").value(1))
                    .andExpect(jsonPath("$.approval_status").value("PENDING"));
        }

        @Test
        @DisplayName("이미 인증된 수료증 - 409 Conflict")
        void verifyCertificate_alreadyExists() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            setAuthentication(memberId);

            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    "1".getBytes()
            );

            given(certificateService.verifyCertificate(
                    eq(memberId), eq(lectureId), any(byte[].class), anyString(), anyString()
            )).willThrow(new CertificateAlreadyExistsException(memberId, lectureId));

            // when & then
            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(image)
                            .file(lectureIdPart))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("강의명 불일치 - 400 Bad Request")
        void verifyCertificate_lectureMismatch() throws Exception {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            setAuthentication(memberId);

            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    "1".getBytes()
            );

            given(certificateService.verifyCertificate(
                    eq(memberId), eq(lectureId), any(byte[].class), anyString(), anyString()
            )).willThrow(new CertificateLectureMismatchException());

            // when & then
            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(image)
                            .file(lectureIdPart))
                    .andExpect(status().isBadRequest());
        }
    }
}
