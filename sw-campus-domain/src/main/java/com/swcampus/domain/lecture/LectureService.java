package com.swcampus.domain.lecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.category.CategoryRepository;
import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.common.BusinessException;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;
import com.swcampus.domain.lecture.exception.LectureNotModifiableException;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.teacher.Teacher;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

	private static final Long ROOT_CATEGORY_ID = 1L;

	private final LectureRepository lectureRepository;
	private final LectureCacheRepository lectureCacheRepository;
	private final com.swcampus.domain.storage.FileStorageService fileStorageService;
	private final com.swcampus.domain.review.ReviewRepository reviewRepository;
	private final CategoryRepository categoryRepository;
	private final OrganizationService organizationService;

	@Value("${app.default-image.base-url:}")
	private String defaultImageBaseUrl;

	@Transactional
	public Lecture registerLecture(Lecture lecture, Long userId, Role role) {
		return registerLecture(lecture, userId, role, null, null, null);
	}

	@Transactional
	public Lecture registerLecture(Lecture lecture, Long userId, Role role, byte[] imageContent, String imageName,
			String contentType) {
		return registerLecture(lecture, userId, role, imageContent, imageName, contentType, Collections.emptyList());
	}

	@Transactional
	public Lecture registerLecture(Lecture lecture, Long userId, Role role, byte[] imageContent, String imageName,
			String contentType,
			List<ImageContent> teacherImages) {

		Long organizationId = resolveOrganizationId(userId, role, lecture.getOrgId());

		String imageUrl = lecture.getLectureImageUrl();

		if (imageContent != null && imageContent.length > 0) {
			imageUrl = fileStorageService.upload(imageContent, "lectures", imageName, contentType);
		} else if (imageUrl == null || imageUrl.isBlank()) {
			imageUrl = resolveDefaultImageUrl(lecture);
		}

		List<Teacher> updatedTeachers = processNewTeachers(lecture.getTeachers(), teacherImages);

		Lecture newLecture = lecture.toBuilder()
				.orgId(organizationId)
				.lectureImageUrl(imageUrl)
				.lectureAuthStatus(LectureAuthStatus.PENDING)
				.teachers(updatedTeachers)
				.build();
		return lectureRepository.save(newLecture);
	}

	public record ImageContent(byte[] content, String name, String contentType) {
	}

	@Transactional
	public Lecture modifyLecture(Long lectureId, Long userId, Role role, Lecture lecture, byte[] imageContent,
			String imageName,
			String contentType,
			List<ImageContent> teacherImages) {
		Lecture existingLecture = getLecture(lectureId);

		// ADMIN은 모든 강의 수정 가능, 권한 체크 건너뜀
		if (role != Role.ADMIN) {
			// 일반 회원은 자신의 기관 ID로 확인
			Long organizationId = resolveOrganizationId(userId, role, null);
			if (!existingLecture.getOrgId().equals(organizationId)) {
				throw new AccessDeniedException("해당 강의를 수정할 권한이 없습니다.");
			}
		}

		// if (existingLecture.getLectureAuthStatus() == LectureAuthStatus.APPROVED) {
		// throw new LectureNotModifiableException();
		// }

		String imageUrl = existingLecture.getLectureImageUrl();

		if (imageContent != null && imageContent.length > 0) {
			imageUrl = fileStorageService.upload(imageContent, "lectures", imageName, contentType);
		}

		List<Teacher> updatedTeachers = processNewTeachers(lecture.getTeachers(), teacherImages);

		// 반려된 강의는 수정 시 승인 대기 상태로 변경, 그 외(승인됨, 대기중)는 기존 상태 유지
		LectureAuthStatus newAuthStatus = existingLecture.getLectureAuthStatus();
		if (existingLecture.getLectureAuthStatus() == LectureAuthStatus.REJECTED) {
			newAuthStatus = LectureAuthStatus.PENDING;
		}

		Lecture updatedLecture = lecture.toBuilder()
				.lectureId(lectureId)
				.lectureImageUrl(imageUrl)
				.status(existingLecture.getStatus())
				.lectureAuthStatus(newAuthStatus)
				.createdAt(existingLecture.getCreatedAt())
				.teachers(updatedTeachers)
				.build();
		Lecture saved = lectureRepository.save(updatedLecture);

		// 캐시 무효화
		lectureCacheRepository.deleteLecture(lectureId);

		return saved;
	}

	/**
	 * 강의 조회 (Cache-Aside 패턴)
	 * 1. 캐시 조회 -> 2. 캐시 미스 시 DB 조회 -> 3. 캐시 저장
	 */
	public Lecture getLecture(Long lectureId) {
		// 1. 캐시 조회
		Optional<Lecture> cached = lectureCacheRepository.getLecture(lectureId);
		if (cached.isPresent()) {
			return cached.get();
		}

		// 2. DB 조회
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

		// 3. 캐시 저장
		lectureCacheRepository.saveLecture(lecture);

		return lecture;
	}

	public List<Lecture> findAllByOrgId(Long orgId) {
		return lectureRepository.findAllByOrgId(orgId);
	}

	public Lecture getPublishedLecture(Long lectureId) {
		Lecture lecture = getLecture(lectureId);
		if (lecture.getLectureAuthStatus() != LectureAuthStatus.APPROVED) {
			throw new ResourceNotFoundException("Lecture not found with id: " + lectureId);
		}
		return lecture;
	}

	public Page<Lecture> searchLectures(LectureSearchCondition condition) {
		return lectureRepository.searchLectures(condition);
	}

	public List<Lecture> getTopRatedLecturesByCategory(Long categoryId, int limit) {
		LectureSearchCondition condition = LectureSearchCondition.builder()
				.categoryIds(Collections.singletonList(categoryId))
				.status(LectureStatus.RECRUITING)
				.lectureAuthStatus(LectureAuthStatus.APPROVED)
				.sort(com.swcampus.domain.lecture.dto.LectureSortType.SCORE_DESC)
				.pageable(org.springframework.data.domain.PageRequest.of(0, limit))
				.build();

		return searchLectures(condition).getContent();
	}

	/**
	 * 강의 상세 조회 (평점/리뷰 수 포함)
	 */
	public LectureSummaryDto getLectureWithStats(Long lectureId) {
		Lecture lecture = getPublishedLecture(lectureId);
		Double averageScore = getAverageScoresByLectureIds(List.of(lectureId))
				.getOrDefault(lectureId, null);
		Long reviewCount = getReviewCountsByLectureIds(List.of(lectureId))
				.getOrDefault(lectureId, null);
		return LectureSummaryDto.from(lecture, averageScore, reviewCount);
	}

	/**
	 * 강의 검색 (평점/리뷰 통계 포함)
	 */
	public Page<LectureSummaryDto> searchLecturesWithStats(LectureSearchCondition condition) {
		Page<Lecture> lectures = searchLectures(condition);

		List<Long> lectureIds = lectures.getContent().stream()
				.map(Lecture::getLectureId)
				.toList();

		// 2 쿼리 → 1 쿼리 최적화: 평균 점수와 리뷰 수를 한 번에 조회
		Map<Long, Map<String, Number>> reviewStats = reviewRepository.getReviewStatsByLectureIds(lectureIds);

		return lectures.map(lecture -> {
			Map<String, Number> stats = reviewStats.getOrDefault(lecture.getLectureId(), Map.of());
			Double avgScore = stats.getOrDefault("avgScore", 0.0).doubleValue();
			Long reviewCount = stats.getOrDefault("reviewCount", 0L).longValue();
			return LectureSummaryDto.from(lecture, avgScore, reviewCount);
		});
	}

	/**
	 * 카테고리별 평점 높은 강의 조회 (평점/리뷰 통계 포함)
	 */
	public List<LectureSummaryDto> getTopRatedLecturesByCategoryWithStats(Long categoryId, int limit) {
		List<Lecture> lectures = getTopRatedLecturesByCategory(categoryId, limit);

		List<Long> lectureIds = lectures.stream()
				.map(Lecture::getLectureId)
				.toList();

		// 2 쿼리 → 1 쿼리 최적화
		Map<Long, Map<String, Number>> reviewStats = reviewRepository.getReviewStatsByLectureIds(lectureIds);

		return lectures.stream()
				.map(lecture -> {
					Map<String, Number> stats = reviewStats.getOrDefault(lecture.getLectureId(), Map.of());
					Double avgScore = stats.getOrDefault("avgScore", 0.0).doubleValue();
					Long reviewCount = stats.getOrDefault("reviewCount", 0L).longValue();
					return LectureSummaryDto.from(lecture, avgScore, reviewCount);
				})
				.toList();
	}

	public Map<Long, String> getLectureNames(List<Long> lectureIds) {
		return lectureRepository.findLectureNamesByIds(lectureIds);
	}

	public Map<Long, Lecture> getLecturesByIds(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		return lectureRepository.findAllByIds(lectureIds).stream()
				.collect(Collectors.toMap(Lecture::getLectureId, lecture -> lecture));
	}

	@Transactional(readOnly = true)
	public List<Lecture> getPublishedLectureListByOrgId(Long orgId) {
		return lectureRepository.findAllByOrgIdAndLectureAuthStatus(orgId, LectureAuthStatus.APPROVED);
		// return lectureRepository.findAllByOrgIdAndLectureAuthStatus(orgId,
		// LectureAuthStatus.PENDING);
	}

	public Map<Long, Long> getRecruitingLectureCounts(List<Long> orgIds) {
		return lectureRepository.countLecturesByStatusAndAuthStatusAndOrgIdIn(LectureStatus.RECRUITING,
				LectureAuthStatus.APPROVED, orgIds);
	}

	public Map<Long, Double> getAverageScoresByLectureIds(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		return reviewRepository.getAverageScoresByLectureIds(lectureIds);
	}

	public Map<Long, Long> getReviewCountsByLectureIds(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		return reviewRepository.countReviewsByLectureIds(lectureIds);
	}

	private List<Teacher> processNewTeachers(List<Teacher> teachers, List<ImageContent> teacherImages) {
		List<Teacher> updatedTeachers = new ArrayList<>();
		int imageIndex = 0;
		if (teachers != null) {
			for (Teacher teacher : teachers) {
				// 신규 강사 (ID 없음)인 경우 이미지 업로드 처리
				if (teacher.getTeacherId() == null) {
					String teacherImgUrl = null;
					// 사용 가능한 이미지가 있으면 업로드
					if (teacherImages != null && imageIndex < teacherImages.size()) {
						ImageContent img = teacherImages.get(imageIndex++);
						if (img.content() != null && img.content().length > 0) {
							teacherImgUrl = fileStorageService.upload(img.content(), "teachers", img.name(),
									img.contentType());
						}
					}
					updatedTeachers.add(teacher.toBuilder().teacherImageUrl(teacherImgUrl).build());
				} else {
					// 기존 강사 (ID 있음)는 그대로 유지
					updatedTeachers.add(teacher);
				}
			}
		}
		return updatedTeachers;
	}

	private Long resolveOrganizationId(Long userId, Role role, Long requestOrgId) {
		if (role == Role.ADMIN) {
			if (requestOrgId == null) {
				throw new BusinessException("관리자는 강의 등록 시 기관 ID를 필수적으로 입력해야 합니다.");
			}
			return organizationService.getOrganization(requestOrgId).getId();
		} else {
			return organizationService.getApprovedOrganizationByUserId(userId).getId();
		}
	}

	private String resolveDefaultImageUrl(Lecture lecture) {
		if (defaultImageBaseUrl == null || defaultImageBaseUrl.isBlank()) {
			return null;
		}

		Long categoryId = lecture.extractCategoryId();
		if (categoryId == null) {
			return null;
		}

		Long middleCategoryId = findMiddleCategoryId(categoryId);
		if (middleCategoryId == null) {
			return null;
		}

		String fileName = getDefaultImageFileName(middleCategoryId);
		if (fileName == null) {
			return null;
		}

		return defaultImageBaseUrl + "/" + fileName;
	}

	private Long findMiddleCategoryId(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.map(category -> {
					Long pid = category.getPid();
					if (pid == null || pid.equals(ROOT_CATEGORY_ID)) {
						return categoryId;
					}
					return pid;
				})
				.orElse(null);
	}

	private String getDefaultImageFileName(Long middleCategoryId) {
		return switch (middleCategoryId.intValue()) {
			case 2 -> "web-development.png";
			case 6 -> "mobile.png";
			case 8 -> "data-ai.png";
			case 12 -> "cloud.png";
			case 14 -> "security.png";
			case 16 -> "embedded-iot.png";
			case 19 -> "game-blockchain.png";
			case 22 -> "planning-marketing-design.png";
			default -> null;
		};
	}

}