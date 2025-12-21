package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.BannerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * @deprecated Native query를 사용한 검색 메서드입니다.
     * 메인 쿼리와 countQuery의 중복 문제가 있으므로
     * {@link BannerSpecifications#searchBanners}를 사용하는 것을 권장합니다.
     * 이 메서드는 향후 버전에서 제거될 예정입니다.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
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
    Page<BannerEntity> searchBannersNative(
            @Param("keyword") String keyword,
            @Param("periodStatus") String periodStatus,
            @Param("now") OffsetDateTime now,
            Pageable pageable);
}

