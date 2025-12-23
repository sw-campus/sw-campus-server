package com.swcampus.domain.lecture;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BannerRepository {
    Banner save(Banner banner);

    Optional<Banner> findById(Long id);

    List<Banner> findAll();

    List<Banner> findAllByLectureId(Long lectureId);

    List<Banner> findAllActiveBanners();

    List<Banner> findAllActiveBannersByType(BannerType type);

    void deleteById(Long id);

    /**
     * 검색 조건에 따른 배너 조회 (페이징)
     * @param keyword 강의명 검색어 (null이면 전체)
     * @param periodStatus 기간 상태 (SCHEDULED, ACTIVE, ENDED, null이면 전체)
     * @param pageable 페이징 정보
     * @return 배너 페이지
     */
    Page<Banner> searchBanners(String keyword, String periodStatus, Pageable pageable);

    // Statistics methods
    long countAll();
    long countByIsActive(boolean isActive);
    long countByPeriodStatus(String periodStatus);
}

