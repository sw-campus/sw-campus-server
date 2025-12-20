package com.swcampus.domain.lecture;

import com.swcampus.domain.lecture.dto.BannerWithLectureDto;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;
    private final LectureRepository lectureRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * 모든 활성화된 배너 조회 (강의 정보 포함)
     */
    public List<BannerWithLectureDto> getActiveBannerWithLectureList() {
        List<Banner> banners = bannerRepository.findAllActiveBanners();
        return toBannerWithLectureList(banners);
    }

    /**
     * 배너 타입별 활성화된 배너 조회 (강의 정보 포함)
     */
    public List<BannerWithLectureDto> getActiveBannerWithLectureListByType(BannerType type) {
        List<Banner> banners = bannerRepository.findAllActiveBannersByType(type);
        return toBannerWithLectureList(banners);
    }

    /**
     * 강의별 배너 조회 (강의 정보 포함)
     */
    public List<BannerWithLectureDto> getBannerWithLectureListByLectureId(Long lectureId) {
        List<Banner> banners = bannerRepository.findAllByLectureId(lectureId);
        return toBannerWithLectureList(banners);
    }

    private List<BannerWithLectureDto> toBannerWithLectureList(List<Banner> banners) {
        if (banners.isEmpty()) {
            return List.of();
        }

        List<Long> lectureIds = banners.stream()
                .map(Banner::getLectureId)
                .distinct()
                .toList();

        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIds);
        Map<Long, Lecture> lectureMap = lectures.stream()
                .collect(Collectors.toMap(Lecture::getLectureId, Function.identity()));

        // Organization 정보 조회하여 orgName 설정
        List<Long> orgIds = lectures.stream()
                .map(Lecture::getOrgId)
                .distinct()
                .toList();
        Map<Long, String> orgNameMap = organizationRepository.findAllByIds(orgIds).stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));

        return banners.stream()
                .map(banner -> {
                    Lecture lecture = lectureMap.get(banner.getLectureId());
                    String orgName = lecture != null ? orgNameMap.get(lecture.getOrgId()) : null;
                    // orgName을 포함한 Lecture 생성
                    Lecture lectureWithOrgName = lecture != null
                            ? lecture.toBuilder().orgName(orgName).build()
                            : null;
                    return BannerWithLectureDto.from(banner, lectureWithOrgName);
                })
                .toList();
    }
}
