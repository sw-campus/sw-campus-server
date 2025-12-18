package com.swcampus.infra.postgres.certificate;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CertificateEntityRepository implements CertificateRepository {

    private final CertificateJpaRepository jpaRepository;

    @Override
    public Certificate save(Certificate certificate) {
        CertificateEntity entity = CertificateEntity.from(certificate);
        CertificateEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Certificate> findById(Long id) {
        return jpaRepository.findById(id).map(CertificateEntity::toDomain);
    }

    @Override
    public Optional<Certificate> findByMemberIdAndLectureId(Long memberId, Long lectureId) {
        return jpaRepository.findByMemberIdAndLectureId(memberId, lectureId)
            .map(CertificateEntity::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId) {
        return jpaRepository.existsByMemberIdAndLectureId(memberId, lectureId);
    }

    @Override
    public java.util.List<com.swcampus.domain.certificate.Certificate> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberIdAndApprovalStatus(memberId, com.swcampus.domain.review.ApprovalStatus.APPROVED)
            .stream()
            .map(CertificateEntity::toDomain)
            .toList();
    }
}
