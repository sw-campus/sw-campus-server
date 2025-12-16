package com.swcampus.infra.postgres.certificate;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certificates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CertificateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "certificates_seq")
    @SequenceGenerator(name = "certificates_seq", sequenceName = "certificates_certificate_id_seq", allocationSize = 1)
    @Column(name = "certificate_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long memberId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    public static CertificateEntity from(Certificate certificate) {
        CertificateEntity entity = new CertificateEntity();
        entity.id = certificate.getId();
        entity.memberId = certificate.getMemberId();
        entity.lectureId = certificate.getLectureId();
        entity.imageUrl = certificate.getImageUrl();
        entity.status = certificate.getStatus();
        entity.approvalStatus = certificate.getApprovalStatus();
        return entity;
    }

    public Certificate toDomain() {
        return Certificate.of(
            this.id,
            this.memberId,
            this.lectureId,
            this.imageUrl,
            this.status,
            this.approvalStatus,
            this.getCreatedAt()
        );
    }
}
