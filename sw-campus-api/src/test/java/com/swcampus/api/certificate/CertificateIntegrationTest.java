package com.swcampus.api.certificate;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureLocation;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.ocr.OcrClient;
import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Certificate 통합 테스트 (E2E)
 * <p>
 * 실제 서버 컨텍스트에서 수료증 인증 플로우를 검증합니다.
 * - 수료증 인증 여부 확인 → OCR 인증 → 중복 인증 방지
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Certificate 통합 테스트 (E2E)")
class CertificateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @MockitoBean
    private OcrClient ocrClient;

    @MockitoBean
    private FileStorageService fileStorageService;

    private Member testMember;
    private Lecture testLecture;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        testMember = memberRepository.save(
                Member.createUser("test@example.com", "Password1!",
                        "테스트유저", "테스트닉네임", "010-1234-5678", "서울시")
        );

        // 테스트용 강의 생성 (NOT NULL 컬럼들 포함)
        testLecture = lectureRepository.save(
                Lecture.builder()
                        .lectureName("Spring Boot 실전 강의")
                        .orgId(1L)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .lectureLoc(LectureLocation.OFFLINE)
                        .recruitType(RecruitType.CARD_REQUIRED)
                        .subsidy(BigDecimal.ZERO)
                        .lectureFee(BigDecimal.ZERO)
                        .eduSubsidy(BigDecimal.ZERO)
                        .books(true)
                        .resume(true)
                        .mockInterview(true)
                        .employmentHelp(true)
                        .status(LectureStatus.RECRUITING)
                        .startAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusMonths(3))
                        .totalDays(90)
                        .totalTimes(720)
                        .build()
        );

        // 인증 설정
        setAuthentication(testMember.getId());
    }

    private void setAuthentication(Long memberId) {
        MemberPrincipal principal = new MemberPrincipal(memberId, "user@example.com", Role.USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("시나리오 1: 수료증 인증 전체 플로우")
    class FullCertificateFlow {

        @Test
        @DisplayName("인증 전 → OCR 인증 → 인증 후 확인")
        void completeCertificationFlow() throws Exception {
            // === 1단계: 인증 전 - 미인증 상태 확인 ===
            mockMvc.perform(get("/api/v1/certificates/check")
                            .param("lectureId", testLecture.getLectureId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certified").value(false));

            // === 2단계: OCR 인증 요청 ===
            // Mock 설정: OCR이 강의명을 정상적으로 추출
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of("수료증", "Spring Boot 실전 강의", "수료를 증명합니다"));
            
            // Mock 설정: S3 Private Bucket 업로드 성공
            given(fileStorageService.uploadPrivate(any(byte[].class), eq("certificates"), anyString(), anyString()))
                    .willReturn("https://s3.example.com/certificate/test.jpg");

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    testLecture.getLectureId().toString().getBytes()
            );

            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificateId").exists())
                    .andExpect(jsonPath("$.lectureId").value(testLecture.getLectureId()))
                    .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/certificate/test.jpg"))
                    .andExpect(jsonPath("$.approvalStatus").value("PENDING"));

            // === 3단계: 인증 후 - 인증 상태 확인 ===
            mockMvc.perform(get("/api/v1/certificates/check")
                            .param("lectureId", testLecture.getLectureId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certified").value(true))
                    .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/certificate/test.jpg"));
        }
    }

    @Nested
    @DisplayName("시나리오 2: 에러 케이스")
    class ErrorScenarios {

        @Test
        @DisplayName("중복 인증 시도 - 실패")
        void duplicateCertification_fails() throws Exception {
            // 이미 인증된 수료증이 있는 경우
            certificateRepository.save(
                    Certificate.create(testMember.getId(), testLecture.getLectureId(),
                            "https://s3.example.com/old-certificate.jpg", "SUCCESS")
            );

            // 중복 인증 시도
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of("수료증", "Spring Boot 실전 강의"));
            given(fileStorageService.uploadPrivate(any(byte[].class), eq("certificates"), anyString(), anyString()))
                    .willReturn("https://s3.example.com/new-certificate.jpg");

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    testLecture.getLectureId().toString().getBytes()
            );

            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(status().isConflict());
        }

        @Test
        @Disabled("OCR 기능 일시 비활성화 (certificate.ocr.enabled=false)")
        @DisplayName("OCR 강의명 불일치 - 실패")
        void ocrMismatch_fails() throws Exception {
            // OCR이 다른 강의명을 추출
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of("수료증", "완전 다른 강의", "수료를 증명합니다"));

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    testLecture.getLectureId().toString().getBytes()
            );

            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 강의 - 실패")
        void nonExistentLecture_fails() throws Exception {
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of("수료증", "강의명"));

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    "999999".getBytes()  // 존재하지 않는 강의 ID
            );

            // 존재하지 않는 강의 ID로 인증 시도 시 실패 응답 (2xx가 아님)
            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status >= 200 && status < 300) {
                            throw new AssertionError("Expected error status but got " + status);
                        }
                    });
        }
    }

    @Nested
    @DisplayName("시나리오 3: OCR 유연한 매칭")
    class OcrFlexibleMatching {

        @Test
        @Disabled("OCR 기능 일시 비활성화 (certificate.ocr.enabled=false)")
        @DisplayName("공백/대소문자가 달라도 매칭 성공")
        void flexibleMatchingSuccess() throws Exception {
            // 강의명: "Spring Boot 실전 강의"
            // OCR 추출: "SpringBoot실전강의" (공백 없음)
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of("수료증", "SpringBoot실전강의", "2024년 수료"));
            
            given(fileStorageService.uploadPrivate(any(byte[].class), eq("certificates"), anyString(), anyString()))
                    .willReturn("https://s3.example.com/certificate/test.jpg");

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    testLecture.getLectureId().toString().getBytes()
            );

            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificateId").exists());
        }

        @Test
        @Disabled("OCR 기능 일시 비활성화 (certificate.ocr.enabled=false)")
        @DisplayName("강의명이 여러 줄에 걸쳐 추출되어도 매칭 성공")
        void multiLineMatchingSuccess() throws Exception {
            // 강의명이 OCR에서 여러 줄로 분리된 경우
            given(ocrClient.extractText(any(byte[].class), anyString()))
                    .willReturn(List.of(
                            "수료증",
                            "Spring Boot",
                            "실전 강의",
                            "수료를 증명합니다"
                    ));
            
            given(fileStorageService.uploadPrivate(any(byte[].class), eq("certificates"), anyString(), anyString()))
                    .willReturn("https://s3.example.com/certificate/test.jpg");

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "certificate.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "fake-image-content".getBytes()
            );

            MockMultipartFile lectureIdPart = new MockMultipartFile(
                    "lectureId",
                    "",
                    MediaType.TEXT_PLAIN_VALUE,
                    testLecture.getLectureId().toString().getBytes()
            );

            mockMvc.perform(multipart("/api/v1/certificates/verify")
                            .file(imageFile)
                            .file(lectureIdPart))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.certificateId").exists());
        }
    }
}
