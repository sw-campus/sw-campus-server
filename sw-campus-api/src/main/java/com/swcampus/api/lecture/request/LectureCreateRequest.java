package com.swcampus.api.lecture.request;

import com.swcampus.domain.lecture.Cohort;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.teacher.Teacher;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

public record LectureCreateRequest(
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

		// 하위 데이터
		List<CohortRequest> cohorts,
		List<StepRequest> steps,
		List<AddRequest> adds,
		List<QualRequest> quals,
		List<TeacherRequest> teachers,
		List<CurriculumRequest> curriculums) {
	public Lecture toDomain() {
		return Lecture.builder()
				.orgId(orgId)
				.lectureName(lectureName)
				.days(days)
				.startTime(startTime)
				.endTime(endTime)
				.lectureLoc(lectureLoc)
				.location(location)
				.recruitType(recruitType)
				.subsidy(subsidy)
				.lectureFee(lectureFee)
				.eduSubsidy(eduSubsidy)
				.goal(goal)
				.maxCapacity(maxCapacity)
				.equipPc(equipPc)
				.equipLaptop(equipLaptop)
				.equipGpu(equipGpu)
				.books(books)
				.resume(resume)
				.mockInterview(mockInterview)
				.employmentHelp(employmentHelp)
				.afterCompletion(afterCompletion)
				.url(url)
				.lectureImageUrl(lectureImageUrl)
				.status("RECRUITING") // 초기 상태
				.lectureAuthStatus(false) // 관리자 승인시 true
				// 1. 기수(Cohorts) 변환
				.cohorts(cohorts != null
						? cohorts.stream().map(CohortRequest::toDomain).toList()
						: List.of())

				// 2. 선발절차(Steps) 변환
				.steps(steps != null
						? steps.stream().map(StepRequest::toDomain).toList()
						: List.of())

				// 3. 추가혜택(Adds) 변환
				.adds(adds != null
						? adds.stream().map(AddRequest::toDomain).toList()
						: List.of())

				// 4. 지원자격(Quals) 변환
				.quals(quals != null
						? quals.stream().map(QualRequest::toDomain).toList()
						: List.of())

				// 5. 강사(Teachers) 변환
				.teachers(teachers != null
						? teachers.stream().map(TeacherRequest::toDomain).toList()
						: List.of())

				// 6. 커리큘럼(Curriculums) 변환 (연결 정보)
				.lectureCurriculums(curriculums != null
						? curriculums.stream().map(CurriculumRequest::toDomainInfo).toList()
						: List.of())

				.build();
	}

	public record CohortRequest(String startAt, String endAt) {
		public Cohort toDomain() {
			// 입력 포맷: "2024-01-01"
			return Cohort.builder()
					.startAt(LocalDate.parse(startAt).atStartOfDay())
					.endAt(LocalDate.parse(endAt).atStartOfDay())
					.build();
		}
	}

	public record StepRequest(String stepName, Integer stepOrder) {
		public LectureStep toDomain() {
			return LectureStep.builder()
					.stepName(stepName)
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
					.type(type)
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
					.level(level)
					.build();
		}
	}
}