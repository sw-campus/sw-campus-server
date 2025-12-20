package com.swcampus.domain.lecture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    /**
     * 모든 활성화된 배너 조회
     */
    public List<Banner> getActiveBannerList() {
        return bannerRepository.findAllActiveBanners();
    }

    /**
     * 배너 타입별 활성화된 배너 조회
     */
    public List<Banner> getActiveBannerListByType(BannerType type) {
        return bannerRepository.findAllActiveBannersByType(type);
    }

    /**
     * 강의별 배너 조회
     */
    public List<Banner> getBannerListByLectureId(Long lectureId) {
        return bannerRepository.findAllByLectureId(lectureId);
    }
}
