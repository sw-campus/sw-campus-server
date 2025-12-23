package com.swcampus.domain.lecture;

import com.swcampus.domain.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.swcampus.domain.lecture.dto.BannerDetailsDto;
import com.swcampus.domain.storage.FileStorageService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerService {

    private final BannerRepository bannerRepository;
    private final LectureRepository lectureRepository;
    private final FileStorageService fileStorageService;

    private static final String UNKNOWN_LECTURE_NAME = "알 수 없음";
    private static final String BANNER_UPLOAD_DIRECTORY = "banners";

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
     * 배너 검색 (페이징, 키워드, 기간 상태)
     */
    public Page<BannerDetailsDto> searchBanners(String keyword, BannerPeriodStatus periodStatus, Pageable pageable) {
        Page<Banner> bannerPage = bannerRepository.searchBanners(keyword, periodStatus, pageable);
        
        List<Long> lectureIds = bannerPage.getContent().stream()
                .map(Banner::getLectureId)
                .distinct()
                .toList();
        Map<Long, String> lectureNames = lectureRepository.findLectureNamesByIds(lectureIds);

        return bannerPage.map(banner -> toDetails(banner, lectureNames));
    }

    /**
     * 배너 상세 조회
     */
    public BannerDetailsDto getBannerDetailsDto(Long id) {
        Banner banner = getBanner(id);
        return toDetails(banner);
    }

    /**
     * 배너 생성 (기존 호환성 유지)
     * isActive가 null인 경우 기본값 true 설정
     */
    @Transactional
    public BannerDetailsDto createBanner(Banner banner) {
        return createBanner(banner, null, null, null);
    }

    /**
     * 배너 생성 (이미지 파일 업로드 포함)
     * isActive가 null인 경우 기본값 true 설정
     */
    @Transactional
    public BannerDetailsDto createBanner(Banner banner, byte[] imageContent, String imageName, String contentType) {
        String imageUrl = banner.getImageUrl();

        // 이미지 파일이 업로드된 경우 S3에 저장하고 URL 획득
        if (imageContent != null && imageContent.length > 0) {
            imageUrl = fileStorageService.upload(imageContent, BANNER_UPLOAD_DIRECTORY, imageName, contentType);
        }

        Banner bannerToSave = banner.toBuilder()
                .imageUrl(imageUrl)
                .isActive(banner.getIsActive() != null ? banner.getIsActive() : true)
                .build();

        Banner saved = bannerRepository.save(bannerToSave);
        return toDetails(saved);
    }

    /**
     * 배너 수정 (기존 호환성 유지)
     * 존재 여부 확인은 bannerRepository.save() 내부에서 수행됨
     */
    @Transactional
    public BannerDetailsDto updateBanner(Long id, Banner banner) {
        return updateBanner(id, banner, null, null, null);
    }

    /**
     * 배너 수정 (이미지 파일 업로드 포함)
     * 새 이미지가 업로드되면 기존 이미지 URL을 대체
     */
    @Transactional
    public BannerDetailsDto updateBanner(Long id, Banner banner, byte[] imageContent, String imageName, String contentType) {
        Banner existing = getBanner(id);
        String imageUrl = existing.getImageUrl(); // 기존 이미지 URL 유지

        // 새 이미지 파일이 업로드된 경우 S3에 저장
        if (imageContent != null && imageContent.length > 0) {
            imageUrl = fileStorageService.upload(imageContent, BANNER_UPLOAD_DIRECTORY, imageName, contentType);
        } else if (banner.getImageUrl() != null && !banner.getImageUrl().isBlank()) {
            // 새 이미지 파일 없지만 URL이 직접 제공된 경우
            imageUrl = banner.getImageUrl();
        }

        Banner updatedBanner = banner.toBuilder()
                .id(id)
                .imageUrl(imageUrl)
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

    /**
     * 배너 상태별 통계를 조회합니다.
     */
    public BannerStats getStats() {
        long total = bannerRepository.countAll();
        long active = bannerRepository.countByIsActive(true);
        long inactive = bannerRepository.countByIsActive(false);
        long scheduled = bannerRepository.countByPeriodStatus(BannerPeriodStatus.SCHEDULED);
        long currentlyActive = bannerRepository.countByPeriodStatus(BannerPeriodStatus.ACTIVE);
        long ended = bannerRepository.countByPeriodStatus(BannerPeriodStatus.ENDED);
        return new BannerStats(total, active, inactive, scheduled, currentlyActive, ended);
    }

    public record BannerStats(long total, long active, long inactive, long scheduled, long currentlyActive, long ended) {}
}
