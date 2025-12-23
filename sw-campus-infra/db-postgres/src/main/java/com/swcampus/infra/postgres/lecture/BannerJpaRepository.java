package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.BannerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Banner 엔티티에 대한 JPA Repository입니다.
 * <p>
 * JpaSpecificationExecutor를 상속하여 동적 쿼리를 Specification으로 처리할 수 있습니다.
 * 복잡한 검색 쿼리는 {@link BannerSpecifications}를 사용하세요.
 * </p>
 */
public interface BannerJpaRepository extends JpaRepository<BannerEntity, Long>, JpaSpecificationExecutor<BannerEntity> {
    List<BannerEntity> findAllByLecture_LectureId(Long lectureId);

    List<BannerEntity> findAllByIsActiveTrueAndStartDateBeforeAndEndDateAfter(OffsetDateTime now1, OffsetDateTime now2);

    List<BannerEntity> findAllByBannerTypeAndIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            BannerType bannerType, OffsetDateTime now1, OffsetDateTime now2);

    long countByIsActive(boolean isActive);

    // SCHEDULED: startDate > now
    @Query("SELECT COUNT(b) FROM BannerEntity b WHERE b.startDate > :now")
    long countScheduled(@Param("now") OffsetDateTime now);

    // ACTIVE: startDate <= now AND endDate > now
    @Query("SELECT COUNT(b) FROM BannerEntity b WHERE b.startDate <= :now AND b.endDate > :now")
    long countActive(@Param("now") OffsetDateTime now);

    // ENDED: endDate <= now
    @Query("SELECT COUNT(b) FROM BannerEntity b WHERE b.endDate <= :now")
    long countEnded(@Param("now") OffsetDateTime now);
}

