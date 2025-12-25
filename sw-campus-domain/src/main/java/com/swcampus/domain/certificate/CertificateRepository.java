package com.swcampus.domain.certificate;

import com.swcampus.domain.common.ApprovalStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CertificateRepository {
    Certificate save(Certificate certificate);
    Optional<Certificate> findById(Long id);
    Optional<Certificate> findByMemberIdAndLectureId(Long memberId, Long lectureId);
    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);
    List<Certificate> findAllByMemberId(Long memberId);
    Map<Long, Certificate> findAllByIds(List<Long> ids);

    // Statistics methods
    long countAll();
    long countByApprovalStatus(ApprovalStatus status);

    void deleteById(Long id);
}
