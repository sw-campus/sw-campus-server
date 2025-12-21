package com.swcampus.domain.lecture;

import com.swcampus.domain.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.lecture.dto.BannerDetailsDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerService {

    private final BannerRepository bannerRepository;
    private final LectureRepository lectureRepository;

    private static final String UNKNOWN_LECTURE_NAME = "알 수 없음";

    /**
     * 모든 배너 조회 (관리자용 - 활성/비활성 모두 포함)
     * 강의명을 포함한 BannerDetailsDto 리스트 반환
     */
    public List<BannerDetailsDto> getBannerDetailsDtoList() {
        List<Banner> banners = bannerRepository.findAll();
        List<Long> lectureIds = banners.stream()
                .map(Banner::getLectureId)
                .distinct()
                .toList();
        Map<Long, String> lectureNames = lectureRepository.findLectureNamesByIds(lectureIds);

        return banners.stream()
                .map(banner -> toDetails(banner, lectureNames))
                .toList();
    }

    /**
     * 배너 상세 조회
     */
    public BannerDetailsDto getBannerDetailsDto(Long id) {
        Banner banner = getBanner(id);
        return toDetails(banner);
    }

    /**
     * 배너 생성
     * isActive가 null인 경우 기본값 true 설정
     */
    @Transactional
    public BannerDetailsDto createBanner(Banner banner) {
        Banner bannerToSave = banner.getIsActive() != null ? banner
                : Banner.builder()
                        .lectureId(banner.getLectureId())
                        .type(banner.getType())
                        .url(banner.getUrl())
                        .imageUrl(banner.getImageUrl())
                        .startDate(banner.getStartDate())
                        .endDate(banner.getEndDate())
                        .isActive(true)
                        .build();

        Banner saved = bannerRepository.save(bannerToSave);
        return toDetails(saved);
    }

    /**
     * 배너 수정
     * 존재 여부 확인은 bannerRepository.save() 내부에서 수행됨
     */
    @Transactional
    public BannerDetailsDto updateBanner(Long id, Banner banner) {
        Banner updatedBanner = Banner.builder()
                .id(id)
                .lectureId(banner.getLectureId())
                .type(banner.getType())
                .url(banner.getUrl())
                .imageUrl(banner.getImageUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .isActive(banner.getIsActive())
                .build();

        Banner saved = bannerRepository.save(updatedBanner);
        return toDetails(saved);
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
    public BannerDetailsDto toggleBannerActive(Long id, Boolean isActive) {
        Banner existing = getBanner(id);

        Banner updated = Banner.builder()
                .id(existing.getId())
                .lectureId(existing.getLectureId())
                .type(existing.getType())
                .url(existing.getUrl())
                .imageUrl(existing.getImageUrl())
                .startDate(existing.getStartDate())
                .endDate(existing.getEndDate())
                .isActive(isActive)
                .build();

        Banner saved = bannerRepository.save(updated);
        return toDetails(saved);
    }

    private Banner getBanner(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
    }

    /**
     * 단일 배너를 BannerDetailsDto로 변환 (강의명 단건 조회)
     */
    private BannerDetailsDto toDetails(Banner banner) {
        Map<Long, String> lectureNames = lectureRepository
                .findLectureNamesByIds(List.of(banner.getLectureId()));
        return toDetails(banner, lectureNames);
    }

    /**
     * 배너와 강의명 맵을 사용하여 BannerDetailsDto 생성
     */
    private BannerDetailsDto toDetails(Banner banner, Map<Long, String> lectureNames) {
        String lectureName = lectureNames.getOrDefault(banner.getLectureId(), UNKNOWN_LECTURE_NAME);
        return BannerDetailsDto.from(banner, lectureName);
    }
}
