package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.BannerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface BannerJpaRepository extends JpaRepository<BannerEntity, Long> {
    List<BannerEntity> findAllByLecture_LectureId(Long lectureId);

    List<BannerEntity> findAllByIsActiveTrueAndStartDateBeforeAndEndDateAfter(OffsetDateTime now1, OffsetDateTime now2);

    List<BannerEntity> findAllByBannerTypeAndIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            BannerType bannerType, OffsetDateTime now1, OffsetDateTime now2);

    @Query(value = """
            SELECT b.* FROM swcampus.banners b
            LEFT JOIN swcampus.lectures l ON l.lecture_id = b.lecture_id
            WHERE (:keyword IS NULL OR l.lecture_name ILIKE '%' || CAST(:keyword AS TEXT) || '%')
            AND (
                :periodStatus IS NULL
                OR (:periodStatus = 'SCHEDULED' AND b.start_date > :now)
                OR (:periodStatus = 'ACTIVE' AND b.start_date <= :now AND b.end_date >= :now)
                OR (:periodStatus = 'ENDED' AND b.end_date < :now)
            )
            ORDER BY b.banner_id DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM swcampus.banners b
            LEFT JOIN swcampus.lectures l ON l.lecture_id = b.lecture_id
            WHERE (:keyword IS NULL OR l.lecture_name ILIKE '%' || CAST(:keyword AS TEXT) || '%')
            AND (
                :periodStatus IS NULL
                OR (:periodStatus = 'SCHEDULED' AND b.start_date > :now)
                OR (:periodStatus = 'ACTIVE' AND b.start_date <= :now AND b.end_date >= :now)
                OR (:periodStatus = 'ENDED' AND b.end_date < :now)
            )
            """,
            nativeQuery = true)
    Page<BannerEntity> searchBanners(
            @Param("keyword") String keyword,
            @Param("periodStatus") String periodStatus,
            @Param("now") OffsetDateTime now,
            Pageable pageable);
}

