package com.swcampus.domain.certificate;

import com.swcampus.domain.common.ApprovalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CertificateTest {

    @Nested
    @DisplayName("Certificate 생성 테스트")
    class CreateCertificateTest {

        @Test
        @DisplayName("수료증 생성 시 PENDING 상태로 생성된다")
        void createCertificate_statusIsPending() {
            // when
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");

            // then
            assertThat(certificate.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
            assertThat(certificate.isPending()).isTrue();
            assertThat(certificate.isApproved()).isFalse();
        }

        @Test
        @DisplayName("수료증 생성 시 필수 정보가 올바르게 설정된다")
        void createCertificate_setsRequiredFields() {
            // given
            Long memberId = 1L;
            Long lectureId = 2L;
            String imageKey = "certificates/2024/01/01/image.jpg";

            // when
            Certificate certificate = Certificate.create(memberId, lectureId, imageKey);

            // then
            assertThat(certificate.getMemberId()).isEqualTo(memberId);
            assertThat(certificate.getLectureId()).isEqualTo(lectureId);
            assertThat(certificate.getImageKey()).isEqualTo(imageKey);
            assertThat(certificate.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Certificate 승인 테스트")
    class ApproveCertificateTest {

        @Test
        @DisplayName("수료증 승인 시 APPROVED로 변경된다")
        void approveCertificate_changesStatusToApproved() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");

            // when
            certificate.approve();

            // then
            assertThat(certificate.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(certificate.isApproved()).isTrue();
            assertThat(certificate.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Certificate 반려 테스트")
    class RejectCertificateTest {

        @Test
        @DisplayName("수료증 반려 시 REJECTED로 변경된다")
        void rejectCertificate_changesStatusToRejected() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");

            // when
            certificate.reject();

            // then
            assertThat(certificate.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
            assertThat(certificate.isApproved()).isFalse();
            assertThat(certificate.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Certificate 상태 확인 테스트")
    class StatusCheckTest {

        @Test
        @DisplayName("isPending - PENDING 상태일 때 true 반환")
        void isPending_whenPending_returnsTrue() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");

            // then
            assertThat(certificate.isPending()).isTrue();
        }

        @Test
        @DisplayName("isPending - APPROVED 상태일 때 false 반환")
        void isPending_whenApproved_returnsFalse() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");
            certificate.approve();

            // then
            assertThat(certificate.isPending()).isFalse();
        }

        @Test
        @DisplayName("isApproved - APPROVED 상태일 때 true 반환")
        void isApproved_whenApproved_returnsTrue() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");
            certificate.approve();

            // then
            assertThat(certificate.isApproved()).isTrue();
        }

        @Test
        @DisplayName("isApproved - REJECTED 상태일 때 false 반환")
        void isApproved_whenRejected_returnsFalse() {
            // given
            Certificate certificate = Certificate.create(1L, 1L, "certificates/2024/01/01/image.jpg");
            certificate.reject();

            // then
            assertThat(certificate.isApproved()).isFalse();
        }
    }

    @Nested
    @DisplayName("Certificate.of 팩토리 메서드 테스트")
    class OfMethodTest {

        @Test
        @DisplayName("of 메서드로 모든 필드를 가진 Certificate 생성")
        void ofMethod_createsWithAllFields() {
            // given
            Long id = 1L;
            Long memberId = 2L;
            Long lectureId = 3L;
            String imageKey = "certificates/2024/01/01/image.jpg";
            ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

            // when
            Certificate certificate = Certificate.of(
                    id, memberId, lectureId, imageKey, approvalStatus, null
            );

            // then
            assertThat(certificate.getId()).isEqualTo(id);
            assertThat(certificate.getMemberId()).isEqualTo(memberId);
            assertThat(certificate.getLectureId()).isEqualTo(lectureId);
            assertThat(certificate.getImageKey()).isEqualTo(imageKey);
            assertThat(certificate.getApprovalStatus()).isEqualTo(approvalStatus);
        }
    }
}
