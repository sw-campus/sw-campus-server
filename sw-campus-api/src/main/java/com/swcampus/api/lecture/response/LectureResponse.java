package com.swcampus.api.lecture.response;

import com.swcampus.domain.lecture.Lecture;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record LectureResponse(
		Long lectureId,
		Long orgId,
		String lectureName,
		String days,
		LocalTime startTime,
		LocalTime endTime,
		String lectureLoc,
		String location,
		String recruitType,
		BigDecimal subsidy,
		BigDecimal lectureFee,
		BigDecimal eduSubsidy,
		String goal,
		Integer maxCapacity,
		String equipPc,
		String equipLaptop,
		Boolean equipGpu,
		Boolean books,
		Boolean resume,
		Boolean mockInterview,
		Boolean employmentHelp,
		Integer afterCompletion,
		String url,
		String lectureImageUrl,
		String status,
		Boolean lectureAuthStatus,

		// 하위 응답
		List<CohortResponse> cohorts,
		List<StepResponse> steps,
		List<AddResponse> adds,
		List<QualResponse> quals,
		List<TeacherResponse> teachers,
		List<CurriculumResponse> curriculums) {
	public static LectureResponse from(Lecture lecture) {
		return new LectureResponse(
				lecture.getLectureId(),
				lecture.getOrgId(),
				lecture.getLectureName(),
				lecture.getDays(),
				lecture.getStartTime(),
				lecture.getEndTime(),
				lecture.getLectureLoc().name(),
				lecture.getLocation(),
				lecture.getRecruitType().name(),
				lecture.getSubsidy(),
				lecture.getLectureFee(),
				lecture.getEduSubsidy(),
				lecture.getGoal(),
				lecture.getMaxCapacity(),
				lecture.getEquipPc() != null ? lecture.getEquipPc().name() : null,
				lecture.getEquipLaptop() != null ? lecture.getEquipLaptop().name() : null,
				lecture.getEquipGpu(),
				lecture.getBooks(),
				lecture.getResume(),
				lecture.getMockInterview(),
				lecture.getEmploymentHelp(),
				lecture.getAfterCompletion(),
				lecture.getUrl(),
				lecture.getLectureImageUrl(),
				lecture.getStatus(),
				lecture.getLectureAuthStatus(),

				// Null-safe List Mapping
				lecture.getCohorts() != null ? lecture.getCohorts().stream().map(CohortResponse::from).toList()
						: List.of(),
				lecture.getSteps() != null ? lecture.getSteps().stream().map(StepResponse::from).toList() : List.of(),
				lecture.getAdds() != null ? lecture.getAdds().stream().map(AddResponse::from).toList() : List.of(),
				lecture.getQuals() != null ? lecture.getQuals().stream().map(QualResponse::from).toList() : List.of(),
				lecture.getTeachers() != null ? lecture.getTeachers().stream().map(TeacherResponse::from).toList()
						: List.of(),
				lecture.getLectureCurriculums() != null
						? lecture.getLectureCurriculums().stream().map(CurriculumResponse::from).toList()
						: List.of());
	}

	public record CohortResponse(Long cohortNum, String startAt, String endAt, Integer totalDays) {
		public static CohortResponse from(com.swcampus.domain.lecture.Cohort c) {
			return new CohortResponse(c.getCohortNum(), c.getStartAt().toString(), c.getEndAt().toString(), c.getTotalDays());
		}
	}

	public record StepResponse(Long stepId, String stepName, Integer stepOrder) {
		public static StepResponse from(com.swcampus.domain.lecture.LectureStep s) {
			return new StepResponse(s.getStepId(), s.getStepName(), s.getStepOrder());
		}
	}

	public record AddResponse(Long addId, String addName) {
		public static AddResponse from(com.swcampus.domain.lecture.LectureAdd a) {
			return new AddResponse(a.getAddId(), a.getAddName());
		}
	}

	public record QualResponse(Long qualId, String type, String text) {
		public static QualResponse from(com.swcampus.domain.lecture.LectureQual q) {
			return new QualResponse(q.getQualId(), q.getType(), q.getText());
		}
	}

	public record TeacherResponse(Long teacherId, String teacherName, String teacherImageUrl) {
		public static TeacherResponse from(com.swcampus.domain.teacher.Teacher t) {
			return new TeacherResponse(t.getTeacherId(), t.getTeacherName(), t.getTeacherImageUrl());
		}
	}

	public record CurriculumResponse(Long curriculumId, String curriculumName, String level) {
		public static CurriculumResponse from(com.swcampus.domain.lecture.LectureCurriculum lc) {
			return new CurriculumResponse(
					lc.getCurriculumId(),
					lc.getCurriculum() != null ? lc.getCurriculum().getCurriculumName() : "",
					lc.getLevel());
		}
	}
}