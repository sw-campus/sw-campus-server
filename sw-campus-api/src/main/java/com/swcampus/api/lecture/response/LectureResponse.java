package com.swcampus.api.lecture.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureSpecialCurriculum;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 정보 응답")
public record LectureResponse(
		@Schema(description = "강의 ID", example = "1") Long lectureId,

		@Schema(description = "카테고리 ID", example = "10") Long categoryId,

		@Schema(description = "기관 ID", example = "1") Long orgId,

		@Schema(description = "기관명", example = "패스트캠퍼스") String orgName,

		@Schema(description = "강의명", example = "웹 개발 부트캠프") String lectureName,

		@Schema(description = "수업 요일", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]") Set<String> days,

		@Schema(description = "수업 시작 시간", example = "09:00:00") LocalTime startTime,

		@Schema(description = "수업 종료 시간", example = "18:00:00") LocalTime endTime,

		@Schema(description = "수업 장소 유형", example = "OFFLINE") String lectureLoc,

		@Schema(description = "수업 장소 주소", example = "서울시 강남구 테헤란로 123") String location,

		@Schema(description = "모집 유형", example = "GOVERNMENT_FUNDED") String recruitType,

		@Schema(description = "훈련 장려금", example = "316000") BigDecimal subsidy,

		@Schema(description = "수강료", example = "0") BigDecimal lectureFee,

		@Schema(description = "교육비 지원금", example = "5000000") BigDecimal eduSubsidy,

		@Schema(description = "교육 목표", example = "풀스택 웹 개발자 양성") String goal,

		@Schema(description = "최대 수강 인원", example = "30") Integer maxCapacity,

		@Schema(description = "장비 제공 유형", example = "PROVIDED") String equipPc,

		@Schema(description = "장비 지원 혜택 설명", example = "최신 노트북 제공") String equipMerit,

		@Schema(description = "교재 제공 여부", example = "true") Boolean books,

		@Schema(description = "이력서 작성 지원", example = "true") Boolean resume,

		@Schema(description = "모의 면접 지원", example = "true") Boolean mockInterview,

		@Schema(description = "취업 연계 지원", example = "true") Boolean employmentHelp,

		@Schema(description = "수료 후 사후관리 지원 여부", example = "true") Boolean afterCompletion,

		@Schema(description = "강의 상세 페이지 URL", example = "https://example.com/lecture/1") String url,

		@Schema(description = "강의 대표 이미지 URL", example = "https://example.com/images/lecture1.jpg") String lectureImageUrl,

		@Schema(description = "강의 상태 (RECRUITING, FINISHED)", example = "RECRUITING") String status,

		@Schema(description = "관리자 승인 상태 (PENDING, APPROVED, REJECTED)", example = "APPROVED") String lectureAuthStatus,

		@Schema(description = "프로젝트 수", example = "5") Integer projectNum,

		@Schema(description = "프로젝트 총 시간", example = "200") Integer projectTime,

		@Schema(description = "프로젝트 팀 구성", example = "4~5인 1팀") String projectTeam,

		@Schema(description = "프로젝트 사용 도구", example = "Git, Jira, Slack") String projectTool,

		@Schema(description = "프로젝트 멘토 지원 여부", example = "true") Boolean projectMentor,

		@Schema(description = "교육 시작일", example = "2025-03-01T00:00:00") String startAt,

		@Schema(description = "교육 종료일", example = "2025-08-31T00:00:00") String endAt,

		@Schema(description = "모집 마감일", example = "2025-02-15T00:00:00") String deadline,

		@Schema(description = "총 교육 일수", example = "120") Integer totalDays,

		@Schema(description = "총 교육 시간", example = "960") Integer totalTimes,

		@Schema(description = "선발 절차 목록") List<StepResponse> steps,

		@Schema(description = "추가 혜택 목록") List<AddResponse> adds,

		@Schema(description = "지원 자격 목록") List<QualResponse> quals,

		@Schema(description = "강사 목록") List<TeacherResponse> teachers,

		@Schema(description = "카테고리명", example = "백엔드") String categoryName,

		@Schema(description = "커리큘럼 목록") List<CurriculumResponse> curriculums,

		@Schema(description = "특화 커리큘럼 목록") List<SpecialCurriculumResponse> specialCurriculums,

		@Schema(description = "기관 로고 이미지 URL", example = "https://example.com/logo.jpg") String orgLogoUrl,

		@Schema(description = "기관 시설 이미지 URL 목록") List<String> orgFacilityImageUrls,

		@Schema(description = "리뷰 평균 점수", example = "4.5") Double averageScore,

		@Schema(description = "리뷰 수", example = "10") Long reviewCount) {
	public static LectureResponse from(Lecture lecture) {
		return from(lecture, null, null, null);
	}

	public static LectureResponse from(Lecture lecture, Organization organization) {
		return from(lecture, organization, null, null);
	}

	public static LectureResponse from(Lecture lecture, Organization organization, Double averageScore,
			Long reviewCount) {
		return new LectureResponse(
				lecture.getLectureId(),
				lecture.extractCategoryId(), // categoryId 추가
				lecture.getOrgId(),
				organization != null ? organization.getName() : lecture.getOrgName(),
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
				mapListSafe(lecture.getSteps(), StepResponse::from),
				mapListSafe(lecture.getAdds(), AddResponse::from),
				mapListSafe(lecture.getQuals(), QualResponse::from),
				mapListSafe(lecture.getTeachers(), TeacherResponse::from),
				extractCategoryName(lecture),
				mapListSafe(lecture.getLectureCurriculums(), CurriculumResponse::from),
				mapListSafe(lecture.getSpecialCurriculums(), SpecialCurriculumResponse::from),
				organization != null ? organization.getLogoUrl() : null,
				extractFacilityImageUrls(organization),

				averageScore != null ? averageScore : lecture.getAverageScore(),
				reviewCount != null ? reviewCount : lecture.getReviewCount());
	}

	private static <T, R> List<R> mapListSafe(List<T> list, Function<T, R> mapper) {
		if (list == null) {
			return Collections.emptyList();
		}
		return list.stream().map(mapper).toList();
	}

	private static List<String> extractFacilityImageUrls(Organization organization) {
		if (organization == null) {
			return List.of();
		}
		return java.util.stream.Stream.of(
				organization.getFacilityImageUrl(),
				organization.getFacilityImageUrl2(),
				organization.getFacilityImageUrl3(),
				organization.getFacilityImageUrl4())
				.filter(url -> url != null && !url.isBlank())
				.toList();
	}

	private static String extractCategoryName(Lecture lecture) {
		// If categoryName is already set (from MyBatis search result), use it
		if (lecture.getCategoryName() != null && !lecture.getCategoryName().isBlank()) {
			return lecture.getCategoryName();
		}

		// Otherwise, extract from lectureCurriculums (for JPA detail query)
		if (lecture.getLectureCurriculums() == null || lecture.getLectureCurriculums().isEmpty()) {
			return null;
		}
		var firstCurriculum = lecture.getLectureCurriculums().get(0).getCurriculum();
		if (firstCurriculum == null || firstCurriculum.getCategory() == null) {
			return null;
		}
		return firstCurriculum.getCategory().getCategoryName();
	}

	@Schema(description = "선발 절차 응답")
	public record StepResponse(
			@Schema(description = "절차 ID", example = "1") Long stepId,

			@Schema(description = "절차 유형", example = "INTERVIEW") String stepType,

			@Schema(description = "절차 순서", example = "1") Integer stepOrder) {
		public static StepResponse from(LectureStep s) {
			return new StepResponse(s.getStepId(), s.getStepType() != null ? s.getStepType().name() : null,
					s.getStepOrder());
		}
	}

	@Schema(description = "추가 혜택 응답")
	public record AddResponse(
			@Schema(description = "혜택 ID", example = "1") Long addId,

			@Schema(description = "혜택명", example = "취업 연계 프로그램") String addName) {
		public static AddResponse from(LectureAdd a) {
			return new AddResponse(a.getAddId(), a.getAddName());
		}
	}

	@Schema(description = "지원 자격 응답")
	public record QualResponse(
			@Schema(description = "자격 ID", example = "1") Long qualId,

			@Schema(description = "자격 유형", example = "REQUIRED") String type,

			@Schema(description = "자격 내용", example = "프로그래밍 기초 지식 보유자") String text) {
		public static QualResponse from(LectureQual q) {
			return new QualResponse(q.getQualId(), q.getType().name(), q.getText());
		}
	}

	@Schema(description = "강사 응답")
	public record TeacherResponse(
			@Schema(description = "강사 ID", example = "1") Long teacherId,

			@Schema(description = "강사명", example = "김개발") String teacherName,

			@Schema(description = "강사 소개", example = "10년차 백엔드 개발자") String teacherDescription,

			@Schema(description = "강사 이미지 URL", example = "https://example.com/teacher1.jpg") String teacherImageUrl) {
		public static TeacherResponse from(Teacher t) {
			return new TeacherResponse(t.getTeacherId(), t.getTeacherName(), t.getTeacherDescription(),
					t.getTeacherImageUrl());
		}
	}

	@Schema(description = "커리큘럼 응답")
	public record CurriculumResponse(
			@Schema(description = "커리큘럼 ID", example = "1") Long curriculumId,

			@Schema(description = "커리큘럼명", example = "Java 기초") String curriculumName,

			@Schema(description = "커리큘럼 설명", example = "Java의 기본 문법과 객체지향 프로그래밍을 학습합니다.") String curriculumDesc,

			@Schema(description = "난이도", example = "NONE") String level) {
		public static CurriculumResponse from(LectureCurriculum lc) {
			var curriculum = lc.getCurriculum();
			return new CurriculumResponse(
					lc.getCurriculumId(),
					curriculum != null ? curriculum.getCurriculumName() : "",
					curriculum != null ? curriculum.getCurriculumDesc() : "",
					lc.getLevel().name());
		}
	}

	@Schema(description = "특화 커리큘럼 응답")
	public record SpecialCurriculumResponse(
			@Schema(description = "특화 커리큘럼 ID", example = "1") Long specialCurriculumId,

			@Schema(description = "제목", example = "AI 프로젝트") String title,

			@Schema(description = "정렬 순서", example = "1") Integer sortOrder) {
		public static SpecialCurriculumResponse from(LectureSpecialCurriculum sc) {
			return new SpecialCurriculumResponse(
					sc.getSpecialCurriculumId(),
					sc.getTitle(),
					sc.getSortOrder());
		}
	}
}