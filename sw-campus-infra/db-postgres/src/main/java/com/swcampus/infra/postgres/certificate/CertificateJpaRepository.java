package com.swcampus.infra.postgres.certificate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateJpaRepository extends JpaRepository<CertificateEntity, Long> {

    Optional<CertificateEntity> findByMemberIdAndLectureId(Long memberId, Long lectureId);

    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);
}
