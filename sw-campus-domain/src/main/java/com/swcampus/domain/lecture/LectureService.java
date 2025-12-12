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