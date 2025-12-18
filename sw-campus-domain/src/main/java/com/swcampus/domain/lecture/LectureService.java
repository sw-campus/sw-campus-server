package com.swcampus.domain.lecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;
import com.swcampus.domain.lecture.exception.LectureNotModifiableException;
import com.swcampus.domain.teacher.Teacher;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

	private final LectureRepository lectureRepository;
	private final com.swcampus.domain.storage.FileStorageService fileStorageService;
	private final com.swcampus.domain.review.ReviewRepository reviewRepository;

	@Transactional
	public Lecture registerLecture(Lecture lecture) {
		return registerLecture(lecture, null, null, null);
	}

	@Transactional
	public Lecture registerLecture(Lecture lecture, byte[] imageContent, String imageName, String contentType) {
		return registerLecture(lecture, imageContent, imageName, contentType, Collections.emptyList());
	}

	@Transactional
	public Lecture registerLecture(Lecture lecture, byte[] imageContent, String imageName, String contentType,
			List<ImageContent> teacherImages) {
		String imageUrl = lecture.getLectureImageUrl();

		if (imageContent != null && imageContent.length > 0) {
			imageUrl = fileStorageService.upload(imageContent, "lectures", imageName, contentType);
		}

		List<Teacher> updatedTeachers = processNewTeachers(lecture.getTeachers(), teacherImages);

		Lecture newLecture = lecture.toBuilder()
				.lectureImageUrl(imageUrl)
				.lectureAuthStatus(LectureAuthStatus.PENDING)
				.teachers(updatedTeachers)
				.build();
		return lectureRepository.save(newLecture);
	}

	public record ImageContent(byte[] content, String name, String contentType) {
	}

	@Transactional
	public Lecture modifyLecture(Long lectureId, Long orgId, Lecture lecture, byte[] imageContent, String imageName,
			String contentType,
			List<ImageContent> teacherImages) {
		Lecture existingLecture = getLecture(lectureId);

		if (!existingLecture.getOrgId().equals(orgId)) {
			throw new AccessDeniedException("해당 강의를 수정할 권한이 없습니다.");
		}

		if (existingLecture.getLectureAuthStatus() != LectureAuthStatus.REJECTED) {
			throw new LectureNotModifiableException();
		}

		String imageUrl = existingLecture.getLectureImageUrl();

		if (imageContent != null && imageContent.length > 0) {
			imageUrl = fileStorageService.upload(imageContent, "lectures", imageName, contentType);
		}

		List<Teacher> updatedTeachers = processNewTeachers(lecture.getTeachers(), teacherImages);

		Lecture updatedLecture = lecture.toBuilder()
				.lectureId(lectureId)
				.lectureImageUrl(imageUrl)
				.status(existingLecture.getStatus())
				.lectureAuthStatus(LectureAuthStatus.PENDING)
				.createdAt(existingLecture.getCreatedAt())
				.teachers(updatedTeachers)
				.build();
		return lectureRepository.save(updatedLecture);
	}

	public Lecture getLecture(Long lectureId) {
		return lectureRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
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
		Map<Long, Double> averageScores = getAverageScoresByLectureIds(lectureIds);
		Map<Long, Long> reviewCounts = getReviewCountsByLectureIds(lectureIds);

		return lectures.map(lecture -> LectureSummaryDto.from(
				lecture,
				averageScores.get(lecture.getLectureId()),
				reviewCounts.get(lecture.getLectureId())));
	}

	/**
	 * 카테고리별 평점 높은 강의 조회 (평점/리뷰 통계 포함)
	 */
	public List<LectureSummaryDto> getTopRatedLecturesByCategoryWithStats(Long categoryId, int limit) {
		List<Lecture> lectures = getTopRatedLecturesByCategory(categoryId, limit);

		List<Long> lectureIds = lectures.stream()
				.map(Lecture::getLectureId)
				.toList();
		Map<Long, Double> averageScores = getAverageScoresByLectureIds(lectureIds);
		Map<Long, Long> reviewCounts = getReviewCountsByLectureIds(lectureIds);

		return lectures.stream()
				.map(lecture -> LectureSummaryDto.from(
						lecture,
						averageScores.get(lecture.getLectureId()),
						reviewCounts.get(lecture.getLectureId())))
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

}