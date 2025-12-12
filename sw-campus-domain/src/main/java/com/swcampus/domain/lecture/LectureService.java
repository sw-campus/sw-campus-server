package com.swcampus.domain.lecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

	private final LectureRepository lectureRepository;
	private final com.swcampus.domain.storage.FileStorageService fileStorageService;

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

		// 신규 강사 수 계산
		long newTeacherCount = 0;
		if (lecture.getTeachers() != null) {
			newTeacherCount = lecture.getTeachers().stream()
					.filter(t -> t.getTeacherId() == null)
					.count();
		}

		// 이미지 수 계산
		int imageCount = (teacherImages != null) ? teacherImages.size() : 0;

		// 유효성 검사: 신규 강사 수와 이미지 수가 일치해야 함 (이미지가 없는 경우는 허용?)
		// 사용자의 요청: "신규 강사만 카운트하기 때문에... 불일치할 수 있습니다" -> 엄격한 매핑 요구
		// 정책: 신규 강사가 있으면 그 수만큼의 이미지가 "있거나" 아니면 "아예 없거나" (모두 이미지 없음)가 아니라,
		// 리스트로 넘어오는 images는 순서대로 매핑되므로, images가 존재한다면 개수가 정확해야 함.
		// 만약 teacherImages가 null이거나 empty면? -> 이미지를 안 올리는 경우로 간주 (허용).
		// 하지만 teacherImages가 있다면, newTeacherCount와 정확히 일치해야 1:1 매핑 보장.

		if (imageCount > 0 && imageCount != newTeacherCount) {
			throw new IllegalArgumentException("신규 강사 수(" + newTeacherCount + ")와 업로드된 이미지 수(" + imageCount
					+ ")가 일치하지 않습니다. 신규 강사 등록 시 이미지를 순서대로 모두 첨부하거나, 아예 첨부하지 않아야 합니다.");
		}

		List<com.swcampus.domain.teacher.Teacher> updatedTeachers = new ArrayList<>();
		int imageIndex = 0;
		if (lecture.getTeachers() != null) {
			for (com.swcampus.domain.teacher.Teacher teacher : lecture.getTeachers()) {
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

		Lecture newLecture = lecture.toBuilder()
				.lectureImageUrl(imageUrl)
				.lectureAuthStatus(LectureAuthStatus.PENDING)
				.teachers(updatedTeachers)
				.build();
		return lectureRepository.save(newLecture);
	}

	public record ImageContent(byte[] content, String name, String contentType) {
	}

	public Lecture getLecture(Long lectureId) {
		return lectureRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
	}

	public Page<Lecture> searchLectures(LectureSearchCondition condition) {
		return lectureRepository.searchLectures(condition);
	}

	@Transactional(readOnly = true)
	public List<Lecture> getLectureListByOrgId(Long orgId) {
		return lectureRepository.findAllByOrgId(orgId);
	}

	public Map<Long, Long> getRecruitingLectureCounts(List<Long> orgIds) {
		return lectureRepository.countLecturesByStatusAndOrgIdIn(LectureStatus.RECRUITING, orgIds);
	}

}