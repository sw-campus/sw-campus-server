package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerRepository;
import com.swcampus.domain.lecture.BannerType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BannerEntityRepository implements BannerRepository {

    private final BannerJpaRepository jpaRepository;
    private final LectureJpaRepository lectureJpaRepository;

    @Override
    public Banner save(Banner banner) {
        LectureEntity lectureEntity = lectureJpaRepository.findById(banner.getLectureId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Lecture not found with id: " + banner.getLectureId()));

        BannerEntity entity;
        if (banner.getId() != null) {
            entity = jpaRepository.findById(banner.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + banner.getId()));
            entity.updateFields(banner, lectureEntity);
        } else {
            entity = BannerEntity.from(banner, lectureEntity);
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Banner> findById(Long id) {
        return jpaRepository.findById(id).map(BannerEntity::toDomain);
    }

    @Override
    public List<Banner> findAll() {
        return jpaRepository.findAll().stream()
                .map(BannerEntity::toDomain)
                .toList();
    }

    @Override
    public List<Banner> findAllByLectureId(Long lectureId) {
        return jpaRepository.findAllByLecture_LectureId(lectureId).stream()
                .map(BannerEntity::toDomain)
                .toList();
    }

    @Override
    public List<Banner> findAllActiveBanners() {
        OffsetDateTime now = OffsetDateTime.now();
        return jpaRepository.findAllByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now).stream()
                .map(BannerEntity::toDomain)
                .toList();
    }

    @Override
    public List<Banner> findAllActiveBannersByType(BannerType type) {
        OffsetDateTime now = OffsetDateTime.now();
        return jpaRepository.findAllByBannerTypeAndIsActiveTrueAndStartDateBeforeAndEndDateAfter(type, now, now)
                .stream()
                .map(BannerEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<Banner> searchBanners(String keyword, String periodStatus, Pageable pageable) {
        OffsetDateTime now = OffsetDateTime.now();
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        String status = (periodStatus != null && !periodStatus.isBlank() && !"ALL".equals(periodStatus)) 
                ? periodStatus : null;
        
        // Specification 기반 동적 쿼리 사용 (메인 쿼리와 count 쿼리의 중복 제거)
        var spec = BannerSpecifications.searchBanners(searchKeyword, status, now);
        
        return jpaRepository.findAll(spec, pageable)
                .map(BannerEntity::toDomain);
    }
}

