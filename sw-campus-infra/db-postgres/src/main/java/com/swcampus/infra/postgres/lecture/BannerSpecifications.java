package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

/**
 * BannerEntity에 대한 JPA Specification 빌더 클래스입니다.
 * 동적 쿼리 조건을 코드로 작성하여 중복을 제거하고 타입 안전성을 보장합니다.
 */
public class BannerSpecifications {

    private BannerSpecifications() {
        // Utility class
    }

    /**
     * 강의명(lecture_name)으로 검색하는 Specification
     * @param keyword 검색 키워드 (nullable)
     * @return Specification
     */
    public static Specification<BannerEntity> hasLectureNameLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction(); // 항상 true (조건 없음)
            }
            Join<BannerEntity, LectureEntity> lectureJoin = root.join("lecture", JoinType.LEFT);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(lectureJoin.get("lectureName")), pattern);
        };
    }

    /**
     * Full Text Search를 사용하는 강의명 검색 Specification
     * Note: Criteria API에서 FTS를 직접 사용하기 어려우므로 ILIKE 폴백을 사용합니다.
     * 복잡한 FTS 쿼리는 별도의 CustomRepository에서 Native Query로 처리할 수 있습니다.
     * 
     * @param keyword 검색 키워드 (nullable)
     * @return Specification
     */
    public static Specification<BannerEntity> hasLectureNameContaining(String keyword) {
        return hasLectureNameLike(keyword);
    }

    /**
     * 기간 상태로 필터링하는 Specification
     * @param periodStatus 기간 상태 (SCHEDULED, ACTIVE, ENDED)
     * @param now 현재 시간
     * @return Specification
     */
    public static Specification<BannerEntity> hasPeriodStatus(String periodStatus, OffsetDateTime now) {
        return (root, query, cb) -> {
            if (periodStatus == null || periodStatus.isBlank()) {
                return cb.conjunction(); // 항상 true (조건 없음)
            }

            Path<OffsetDateTime> startDate = root.get("startDate");
            Path<OffsetDateTime> endDate = root.get("endDate");

            return switch (periodStatus.toUpperCase()) {
                case "SCHEDULED" -> cb.greaterThan(startDate, now);
                case "ACTIVE" -> cb.and(
                        cb.lessThanOrEqualTo(startDate, now),
                        cb.greaterThanOrEqualTo(endDate, now)
                );
                case "ENDED" -> cb.lessThan(endDate, now);
                default -> cb.conjunction();
            };
        };
    }

    /**
     * 검색 조건을 결합하는 메서드
     * @param keyword 검색 키워드
     * @param periodStatus 기간 상태
     * @param now 현재 시간
     * @return 결합된 Specification
     */
    public static Specification<BannerEntity> searchBanners(String keyword, String periodStatus, OffsetDateTime now) {
        return Specification.allOf(
                hasLectureNameContaining(keyword),
                hasPeriodStatus(periodStatus, now)
        );
    }
}
