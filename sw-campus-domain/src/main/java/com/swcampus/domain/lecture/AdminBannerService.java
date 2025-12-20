package com.swcampus.domain.lecture;

import com.swcampus.domain.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerService {

    private final BannerRepository bannerRepository;

    /**
     * 모든 배너 조회 (관리자용 - 활성/비활성 모두 포함)
     */
    public List<Banner> getBannerList() {
        return bannerRepository.findAll();
    }

    /**
     * 배너 상세 조회
     */
    public Banner getBanner(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
    }

    /**
     * 배너 생성
     */
    @Transactional
    public Banner createBanner(Banner banner) {
        return bannerRepository.save(banner);
    }

    /**
     * 배너 수정
     */
    @Transactional
    public Banner updateBanner(Long id, Banner banner) {
        // 존재 여부 확인
        getBanner(id);

        Banner updatedBanner = Banner.builder()
                .id(id)
                .lectureId(banner.getLectureId())
                .type(banner.getType())
                .content(banner.getContent())
                .imageUrl(banner.getImageUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .isActive(banner.getIsActive())
                .build();

        return bannerRepository.save(updatedBanner);
    }

    /**
     * 배너 삭제
     */
    @Transactional
    public void deleteBanner(Long id) {
        // 존재 여부 확인
        getBanner(id);
        bannerRepository.deleteById(id);
    }

    /**
     * 배너 활성화/비활성화 토글
     */
    @Transactional
    public Banner toggleBannerActive(Long id, Boolean isActive) {
        Banner existing = getBanner(id);

        Banner updated = Banner.builder()
                .id(existing.getId())
                .lectureId(existing.getLectureId())
                .type(existing.getType())
                .content(existing.getContent())
                .imageUrl(existing.getImageUrl())
                .startDate(existing.getStartDate())
                .endDate(existing.getEndDate())
                .isActive(isActive)
                .build();

        return bannerRepository.save(updated);
    }
}
