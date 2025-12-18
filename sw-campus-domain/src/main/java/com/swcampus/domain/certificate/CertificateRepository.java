package com.swcampus.domain.certificate;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository {
    Certificate save(Certificate certificate);
    Optional<Certificate> findById(Long id);
    Optional<Certificate> findByMemberIdAndLectureId(Long memberId, Long lectureId);
    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);
    List<Certificate> findAllByMemberId(Long memberId);
}
