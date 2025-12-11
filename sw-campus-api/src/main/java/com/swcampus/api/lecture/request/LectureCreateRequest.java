package com.swcampus.api.lecture.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.teacher.Teacher;

public record LectureCreateRequest(
		Long orgId,
		String lectureName,
		Set<String> days, // MONDAY, TUESDAY ...
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

		// 하위 데이터
		List<StepRequest> steps,
		List<AddRequest> adds,
		List<QualRequest> quals,
		List<TeacherRequest> teachers,
		List<CurriculumRequest> curriculums) {

	public Lecture toDomain() {
		return Lecture.builder()
				.orgId(orgId)
				.lectureName(lectureName)
				.days(days != null
						? days.stream().map(com.swcampus.domain.lecture.LectureDay::valueOf).collect(Collectors.toSet())
						: Collections.emptySet())
				.startTime(startTime)
				.endTime(endTime)
				.lectureLoc(com.swcampus.domain.lecture.LectureLocation.valueOf(lectureLoc))
				.location(location)
				.recruitType(com.swcampus.domain.lecture.RecruitType.valueOf(recruitType))
				.subsidy(subsidy)
				.lectureFee(lectureFee)
				.eduSubsidy(eduSubsidy)
				.goal(goal)
				.maxCapacity(maxCapacity)
				.equipPc(com.swcampus.domain.lecture.EquipmentType.valueOf(equipPc))
				.equipMerit(equipMerit)
				.books(books)
				.resume(resume)
				.mockInterview(mockInterview)
				.employmentHelp(employmentHelp)
				.afterCompletion(afterCompletion)
				.url(url)
				.lectureImageUrl(lectureImageUrl)
				.status(com.swcampus.domain.lecture.LectureStatus.RECRUITING) // 초기 상태
				.lectureAuthStatus(com.swcampus.domain.lecture.LectureAuthStatus.PENDING) // 관리자 승인시 APPROVED

				// Project
				.projectNum(projectNum)
				.projectTime(projectTime)
				.projectTeam(projectTeam)
				.projectTool(projectTool)
				.projectMentor(projectMentor)

				.startAt(LocalDate.parse(startAt).atStartOfDay())
				.endAt(LocalDate.parse(endAt).atStartOfDay())
				.deadline(deadline != null ? LocalDate.parse(deadline).atStartOfDay() : null)
				.totalDays(totalDays)
				.totalTimes(totalTimes)

				// 선발절차(Steps) 변환
				.steps(steps != null
						? steps.stream().map(StepRequest::toDomain).toList()
						: List.of())

				// 추가혜택(Adds) 변환
				.adds(adds != null
						? adds.stream().map(AddRequest::toDomain).toList()
						: List.of())

				// 지원자격(Quals) 변환
				.quals(quals != null
						? quals.stream().map(QualRequest::toDomain).toList()
						: List.of())

				// 강사(Teachers) 변환
				.teachers(teachers != null
						? teachers.stream().map(TeacherRequest::toDomain).toList()
						: List.of())

				// 커리큘럼(Curriculums) 변환 (연결 정보)
				.lectureCurriculums(curriculums != null
						? curriculums.stream().map(CurriculumRequest::toDomainInfo).toList()
						: List.of())

				.build();
	}

	public record StepRequest(String stepType, Integer stepOrder) {
		public LectureStep toDomain() {
			return LectureStep.builder()
					.stepType(com.swcampus.domain.lecture.SelectionStepType.valueOf(stepType))
					.stepOrder(stepOrder)
					.build();
		}
	}

	public record AddRequest(String addName) {
		public LectureAdd toDomain() {
			return LectureAdd.builder()
					.addName(addName)
					.build();
		}
	}

	public record QualRequest(String type, String text) {
		public LectureQual toDomain() {
			return LectureQual.builder()
					.type(com.swcampus.domain.lecture.LectureQualType.valueOf(type))
					.text(text)
					.build();
		}
	}

	public record TeacherRequest(Long teacherId, String teacherName, String teacherDescription,
			String teacherImageUrl) {
		public Teacher toDomain() {
			return Teacher.builder()
					.teacherId(teacherId)
					.teacherName(teacherName)
					.teacherDescription(teacherDescription)
					.teacherImageUrl(teacherImageUrl)
					.build();
		}
	}

	public record CurriculumRequest(Long curriculumId, String level) {
		// 커리큘럼은 연결 정보(Info) 객체로 변환
		public LectureCurriculum toDomainInfo() {
			return LectureCurriculum.builder()
					.curriculumId(curriculumId)
					.level(com.swcampus.domain.lecture.CurriculumLevel.valueOf(level))
					.build();
		}
	}
}