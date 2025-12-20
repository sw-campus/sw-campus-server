package com.swcampus.domain.lecture;

import java.util.List;
import java.util.Optional;

public interface BannerRepository {
    Banner save(Banner banner);

    Optional<Banner> findById(Long id);

    List<Banner> findAll();

    List<Banner> findAllByLectureId(Long lectureId);

    List<Banner> findAllActiveBanners();

    List<Banner> findAllActiveBannersByType(BannerType type);

    void deleteById(Long id);
}
