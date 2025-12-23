package com.swcampus.domain.certificate;

import com.swcampus.domain.certificate.exception.CertificateAlreadyExistsException;
import com.swcampus.domain.certificate.exception.CertificateLectureMismatchException;
import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.ocr.OcrClient;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @InjectMocks
    private CertificateService certificateService;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private OcrClient ocrClient;

    @Nested
    @DisplayName("수료증 인증 여부 확인")
    class CheckCertificateTest {

        @Test
        @DisplayName("수료증이 존재하면 Optional에 담겨 반환")
        void checkCertificate_exists_returnsOptionalWithCertificate() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Certificate certificate = Certificate.create(memberId, lectureId, "certificates/image.jpg", "SUCCESS");

            given(certificateRepository.findByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(Optional.of(certificate));

            // when
            Optional<Certificate> result = certificateService.checkCertificate(memberId, lectureId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getMemberId()).isEqualTo(memberId);
            assertThat(result.get().getLectureId()).isEqualTo(lectureId);
        }

        @Test
        @DisplayName("수료증이 없으면 빈 Optional 반환")
        void checkCertificate_notExists_returnsEmptyOptional() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            given(certificateRepository.findByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(Optional.empty());

            // when
            Optional<Certificate> result = certificateService.checkCertificate(memberId, lectureId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("수료증 인증 처리")
    class VerifyCertificateTest {

        @Test
        @DisplayName("이미 인증된 수료증이 있으면 예외 발생")
        void verifyCertificate_alreadyVerified_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            )).isInstanceOf(CertificateAlreadyExistsException.class);
        }

        @Test
        @DisplayName("강의를 찾을 수 없으면 예외 발생")
        void verifyCertificate_lectureNotFound_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 999L;

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            )).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("강의를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("OCR 결과에 강의명이 포함되지 않으면 예외 발생")
        void verifyCertificate_lectureMismatch_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Lecture lecture = createLecture("Java 풀스택 개발자 과정");

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(lecture));
            given(ocrClient.extractText(any(), anyString()))
                    .willReturn(List.of("Python", "백엔드", "과정"));

            // when & then
            assertThatThrownBy(() -> certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            )).isInstanceOf(CertificateLectureMismatchException.class);
        }

        @Test
        @DisplayName("OCR 결과가 빈 리스트면 예외 발생")
        void verifyCertificate_emptyOcrResult_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Lecture lecture = createLecture("Java 풀스택 개발자 과정");

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(lecture));
            given(ocrClient.extractText(any(), anyString()))
                    .willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            )).isInstanceOf(CertificateLectureMismatchException.class);
        }

        @Test
        @DisplayName("수료증 인증 성공 - 정확히 일치")
        void verifyCertificate_exactMatch_success() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Lecture lecture = createLecture("Java 풀스택 개발자 과정");

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(lecture));
            given(ocrClient.extractText(any(), anyString()))
                    .willReturn(List.of("수료증", "Java 풀스택 개발자 과정", "홍길동"));
            given(fileStorageService.uploadPrivate(any(), eq("certificates"), anyString(), anyString()))
                    .willReturn("certificates/test.jpg");

            Certificate savedCertificate = Certificate.create(memberId, lectureId, "certificates/test.jpg", "SUCCESS");
            given(certificateRepository.save(any(Certificate.class)))
                    .willReturn(savedCertificate);

            // when
            Certificate result = certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getLectureId()).isEqualTo(lectureId);
            assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("유연한 매칭: 공백이 다르더라도 강의명 인식 성공")
        void verifyCertificate_flexibleMatching_success() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Lecture lecture = createLecture("Java 풀스택 개발자 과정");

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(lecture));
            // OCR 결과: 공백이 다름
            given(ocrClient.extractText(any(), anyString()))
                    .willReturn(List.of("수료증", "Java풀스택개발자과정", "홍길동"));
            given(fileStorageService.uploadPrivate(any(), eq("certificates"), anyString(), anyString()))
                    .willReturn("certificates/test.jpg");

            Certificate savedCertificate = Certificate.create(memberId, lectureId, "certificates/test.jpg", "SUCCESS");
            given(certificateRepository.save(any(Certificate.class)))
                    .willReturn(savedCertificate);

            // when
            Certificate result = certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("유연한 매칭: 대소문자가 다르더라도 강의명 인식 성공")
        void verifyCertificate_caseInsensitiveMatching_success() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Lecture lecture = createLecture("Java 풀스택 개발자 과정");

            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(lecture));
            // OCR 결과: 대소문자가 다름
            given(ocrClient.extractText(any(), anyString()))
                    .willReturn(List.of("수료증", "JAVA 풀스택 개발자 과정", "홍길동"));
            given(fileStorageService.uploadPrivate(any(), eq("certificates"), anyString(), anyString()))
                    .willReturn("certificates/test.jpg");

            Certificate savedCertificate = Certificate.create(memberId, lectureId, "certificates/test.jpg", "SUCCESS");
            given(certificateRepository.save(any(Certificate.class)))
                    .willReturn(savedCertificate);

            // when
            Certificate result = certificateService.verifyCertificate(
                    memberId, lectureId, new byte[0], "test.jpg", "image/jpeg"
            );

            // then
            assertThat(result).isNotNull();
        }
    }

    // 헬퍼 메서드
    private Lecture createLecture(String lectureName) {
        return Lecture.builder()
                .lectureId(1L)
                .lectureName(lectureName)
                .build();
    }
}
