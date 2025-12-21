package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.BannerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface BannerJpaRepository extends JpaRepository<BannerEntity, Long> {
    List<BannerEntity> findAllByLecture_LectureId(Long lectureId);

    List<BannerEntity> findAllByIsActiveTrueAndStartDateBeforeAndEndDateAfter(OffsetDateTime now1, OffsetDateTime now2);

    List<BannerEntity> findAllByBannerTypeAndIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            BannerType bannerType, OffsetDateTime now1, OffsetDateTime now2);
}
