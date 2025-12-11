package com.swcampus.api.lecture.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.swcampus.domain.lecture.Lecture;

public record LectureResponse(
		Long lectureId,
		Long orgId,
		String orgName,
		String lectureName,
		Set<String> days,
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
		String equipMerit,
		Boolean books,
		Boolean resume,
		Boolean mockInterview,
		Boolean employmentHelp,
		Integer afterCompletion,
		String url,
		String lectureImageUrl,
		String status,
		String lectureAuthStatus,
		
		// Project
		Integer projectNum,
		Integer projectTime,
		String projectTeam,
		String projectTool,
		Boolean projectMentor,
		
		String startAt,
		String endAt,
		String deadline,
		Integer totalDays,
		Integer totalTimes,

		// 하위 응답
		List<StepResponse> steps,
		List<AddResponse> adds,
		List<QualResponse> quals,
		List<TeacherResponse> teachers,
		List<CurriculumResponse> curriculums) {
	public static LectureResponse from(Lecture lecture) {
		return new LectureResponse(
				lecture.getLectureId(),
				lecture.getOrgId(),
				lecture.getOrgName(),
				lecture.getLectureName(),
				lecture.getDays() != null 
						? lecture.getDays().stream().map(Enum::name).collect(Collectors.toSet()) 
						: Collections.emptySet(),
				lecture.getStartTime(),
				lecture.getEndTime(),
				lecture.getLectureLoc() != null ? lecture.getLectureLoc().name() : null,
				lecture.getLocation(),
				lecture.getRecruitType() != null ? lecture.getRecruitType().name() : null,
				lecture.getSubsidy(),
				lecture.getLectureFee(),
				lecture.getEduSubsidy(),
				lecture.getGoal(),
				lecture.getMaxCapacity(),
				lecture.getEquipPc() != null ? lecture.getEquipPc().name() : null,
				lecture.getEquipMerit(),
				lecture.getBooks(),
				lecture.getResume(),
				lecture.getMockInterview(),
				lecture.getEmploymentHelp(),
				lecture.getAfterCompletion(),
				lecture.getUrl(),
				lecture.getLectureImageUrl(),
				lecture.getStatus().name(),
				lecture.getLectureAuthStatus() != null ? lecture.getLectureAuthStatus().name() : null,
				
				lecture.getProjectNum(),
				lecture.getProjectTime(),
				lecture.getProjectTeam(),
				lecture.getProjectTool(),
				lecture.getProjectMentor(),

				lecture.getStartAt() != null ? lecture.getStartAt().toString() : null,
				lecture.getEndAt() != null ? lecture.getEndAt().toString() : null,
				lecture.getDeadline() != null ? lecture.getDeadline().toString() : null,
				lecture.getTotalDays(),
				lecture.getTotalTimes(),
				lecture.getSteps() != null ? lecture.getSteps().stream().map(StepResponse::from).toList() : List.of(),
				lecture.getAdds() != null ? lecture.getAdds().stream().map(AddResponse::from).toList() : List.of(),
				lecture.getQuals() != null ? lecture.getQuals().stream().map(QualResponse::from).toList() : List.of(),
				lecture.getTeachers() != null ? lecture.getTeachers().stream().map(TeacherResponse::from).toList()
						: List.of(),
				lecture.getLectureCurriculums() != null
						? lecture.getLectureCurriculums().stream().map(CurriculumResponse::from).toList()
						: List.of());
	}

	public record StepResponse(Long stepId, String stepType, Integer stepOrder) {
		public static StepResponse from(com.swcampus.domain.lecture.LectureStep s) {
			return new StepResponse(s.getStepId(), s.getStepType() != null ? s.getStepType().name() : null, s.getStepOrder());
		}
	}

	public record AddResponse(Long addId, String addName) {
		public static AddResponse from(com.swcampus.domain.lecture.LectureAdd a) {
			return new AddResponse(a.getAddId(), a.getAddName());
		}
	}

	public record QualResponse(Long qualId, String type, String text) {
		public static QualResponse from(com.swcampus.domain.lecture.LectureQual q) {
			return new QualResponse(q.getQualId(), q.getType().name(), q.getText());
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
					lc.getLevel().name());
		}
	}
}