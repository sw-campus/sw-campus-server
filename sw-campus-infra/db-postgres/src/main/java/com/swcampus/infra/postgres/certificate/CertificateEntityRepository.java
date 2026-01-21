package com.swcampus.infra.postgres.certificate;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.common.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Certificate> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberId(memberId)
            .stream()
            .map(CertificateEntity::toDomain)
            .toList();
    }

    @Override
    public Map<Long, Certificate> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(
                CertificateEntity::getId,
                CertificateEntity::toDomain
            ));
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countByApprovalStatus(ApprovalStatus status) {
        return jpaRepository.countByApprovalStatus(status);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        jpaRepository.deleteByMemberId(memberId);
    }
}
