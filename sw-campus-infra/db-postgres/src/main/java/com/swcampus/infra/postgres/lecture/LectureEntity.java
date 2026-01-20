package com.swcampus.infra.postgres.lecture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureDay;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "LECTURES")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "steps", "adds", "quals", "lectureTeachers", "lectureCurriculums", "specialCurriculums" })
public class LectureEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lectures_seq")
	@SequenceGenerator(name = "lectures_seq", sequenceName = "lectures_lecture_id_seq", allocationSize = 1)
	@Column(name = "LECTURE_ID")
	private Long lectureId;

	@Column(name = "ORG_ID", nullable = false)
	private Long orgId;

	@Transient
	/**
	 * Organization name for search result display.
	 * <p>
	 * This field is only used for MyBatis result mapping to avoid an N+1 query
	 * problem
	 * when fetching organization names during search results. It should NOT be set
	 * or used
	 * in JPA operations, as it is not persisted in the database.
	 */
	private String orgName;

	@Transient
	/**
	 * Category name for search result display.
	 * <p>
	 * This field is only used for MyBatis result mapping to avoid an N+1 query
	 * problem
	 * when fetching category names during search results. It should NOT be set
	 * or used
	 * in JPA operations, as it is not persisted in the database.
	 */
	private String categoryName;

	@Transient
	private Double averageScore;

	@Transient
	private Long reviewCount;

	@Column(name = "LECTURE_NAME", nullable = false)
	private String lectureName;

	@Column(name = "DAYS")
	private String days;

	@Column(name = "START_TIME", nullable = false)
	private LocalTime startTime;

	@Column(name = "END_TIME", nullable = false)
	private LocalTime endTime;

	@Column(name = "LECTURE_LOC", nullable = false)
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.LectureLocation lectureLoc;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "RECRUIT_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.RecruitType recruitType;

	@Column(name = "SUBSIDY", nullable = false)
	private BigDecimal subsidy;

	@Column(name = "LECTURE_FEE", nullable = false)
	private BigDecimal lectureFee;

	@Column(name = "EDU_SUBSIDY", nullable = false)
	private BigDecimal eduSubsidy;

	@Column(name = "GOAL")
	private String goal;

	@Column(name = "MAX_CAPACITY")
	private Integer maxCapacity;

	@Column(name = "EQUIP_PC")
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.EquipmentType equipPc;

	@Column(name = "EQUIP_MERIT")
	private String equipMerit;

	@Column(name = "BOOKS", nullable = false)
	private Boolean books;

	@Column(name = "RESUME", nullable = false)
	private Boolean resume;

	@Column(name = "MOCK_INTERVIEW", nullable = false)
	private Boolean mockInterview;

	@Column(name = "EMPLOYMENT_HELP", nullable = false)
	private Boolean employmentHelp;

	@Column(name = "AFTER_COMPLETION")
	private Boolean afterCompletion;

	@Column(name = "URL")
	private String url;

	@Column(name = "LECTURE_IMAGE_URL")
	private String lectureImageUrl;

	@Column(name = "STATUS", nullable = false)
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.LectureStatus status;

	@Column(name = "LECTURE_AUTH_STATUS")
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.LectureAuthStatus lectureAuthStatus;

	// Project Related (New)
	@Column(name = "PROJECT_NUM")
	private Integer projectNum;

	@Column(name = "PROJECT_TIME")
	private Integer projectTime;

	@Column(name = "PROJECT_TEAM")
	private String projectTeam;

	@Column(name = "PROJECT_TOOL")
	private String projectTool;

	@Column(name = "PROJECT_MENTOR")
	private Boolean projectMentor;

	@Column(name = "START_DATE", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "END_DATE", nullable = false)
	private LocalDateTime endAt;

	@Column(name = "DEADLINE")
	private LocalDateTime deadline;

	@Column(name = "TOTAL_DAYS", nullable = false)
	private Integer totalDays;

	@Column(name = "TOTAL_TIMES", nullable = false)
	private Integer totalTimes;

	@CreationTimestamp
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	// @UpdateTimestamp // 하위 데이터 변경 시에도 갱신을 위해 수동 관리
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	// --- 1:N Relationships ---
	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureStepEntity> steps = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureAddEntity> adds = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureQualEntity> quals = new ArrayList<>();

	// --- N:M Relationships ---

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureTeacherEntity> lectureTeachers = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureCurriculumEntity> lectureCurriculums = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureSpecialCurriculumEntity> specialCurriculums = new ArrayList<>();

	public static LectureEntity from(com.swcampus.domain.lecture.Lecture lecture) {
		if (lecture == null) {
			return null;
		}

		LectureEntity entity = LectureEntity.builder()
				.lectureId(lecture.getLectureId())
				.orgId(lecture.getOrgId())
				.lectureName(lecture.getLectureName())
				.days(lecture.getDays() != null
						? lecture.getDays().stream().map(Enum::name).collect(java.util.stream.Collectors.joining(","))
						: null)
				.startTime(lecture.getStartTime())
				.endTime(lecture.getEndTime())
				.lectureLoc(lecture.getLectureLoc())
				.location(lecture.getLocation())
				.recruitType(lecture.getRecruitType())
				.subsidy(lecture.getSubsidy())
				.lectureFee(lecture.getLectureFee())
				.eduSubsidy(lecture.getEduSubsidy())
				.goal(lecture.getGoal())
				.maxCapacity(lecture.getMaxCapacity())
				.equipPc(lecture.getEquipPc())
				.equipMerit(lecture.getEquipMerit())
				.books(lecture.getBooks())
				.resume(lecture.getResume())
				.mockInterview(lecture.getMockInterview())
				.employmentHelp(lecture.getEmploymentHelp())
				.afterCompletion(lecture.getAfterCompletion())
				.url(lecture.getUrl())
				.lectureImageUrl(lecture.getLectureImageUrl())
				.status(lecture.getStatus())
				.lectureAuthStatus(lecture.getLectureAuthStatus())
				.createdAt(lecture.getCreatedAt())
				// 하위 데이터 변경 시에도 강제로 업데이트 시간 갱신
				.updatedAt(LocalDateTime.now())
				// Project
				.projectNum(lecture.getProjectNum())
				.projectTime(lecture.getProjectTime())
				.projectTeam(lecture.getProjectTeam())
				.projectTool(lecture.getProjectTool())
				.projectMentor(lecture.getProjectMentor())
				.startAt(lecture.getStartAt())
				.endAt(lecture.getEndAt())
				.deadline(lecture.getDeadline())
				.totalDays(lecture.getTotalDays())
				.totalTimes(lecture.getTotalTimes())

				.averageScore(lecture.getAverageScore())
				.reviewCount(lecture.getReviewCount())
				.build();

		// 1:N Relationships (Steps)
		if (lecture.getSteps() != null) {
			entity.getSteps().addAll(lecture.getSteps().stream()
					.map(s -> LectureStepEntity.builder()
							.stepId(s.getStepId())
							.lecture(entity)
							.stepType(s.getStepType())
							.stepOrder(s.getStepOrder())
							.build())
					.toList());
		}

		// 1:N Relationships (Adds)
		if (lecture.getAdds() != null) {
			entity.getAdds().addAll(lecture.getAdds().stream()
					.map(a -> LectureAddEntity.builder()
							.addId(a.getAddId())
							.lecture(entity)
							.addName(a.getAddName())
							.build())
					.toList());
		}

		// 1:N Relationships (Quals)
		if (lecture.getQuals() != null) {
			entity.getQuals().addAll(lecture.getQuals().stream()
					.map(q -> LectureQualEntity.builder()
							.qualId(q.getQualId())
							.lecture(entity)
							.type(q.getType())
							.text(q.getText())
							.build())
					.toList());
		}

		// 1:N Relationships (SpecialCurriculums)
		if (lecture.getSpecialCurriculums() != null) {
			entity.getSpecialCurriculums().addAll(lecture.getSpecialCurriculums().stream()
					.map(sc -> LectureSpecialCurriculumEntity.builder()
							.specialCurriculumId(sc.getSpecialCurriculumId())
							.lecture(entity)
							.title(sc.getTitle())
							.sortOrder(sc.getSortOrder())
							.build())
					.toList());
		}

		return entity;
	}

	public Lecture toDomain() {
		return Lecture.builder()
				.lectureId(lectureId).orgId(orgId).orgName(orgName).categoryName(categoryName).lectureName(lectureName)
				.days(days != null && !days.isEmpty()
						? Arrays.stream(days.split(",")).map(LectureDay::valueOf)
								.collect(Collectors.toSet())
						: Collections.emptySet())
				.startTime(startTime).endTime(endTime)
				.lectureLoc(lectureLoc).location(location).recruitType(recruitType)
				.subsidy(subsidy).lectureFee(lectureFee).eduSubsidy(eduSubsidy)
				.goal(goal).maxCapacity(maxCapacity)
				.equipPc(equipPc).equipMerit(equipMerit)
				.books(books).resume(resume).mockInterview(mockInterview)
				.employmentHelp(employmentHelp).afterCompletion(afterCompletion)
				.url(url).lectureImageUrl(lectureImageUrl)
				.status(status).lectureAuthStatus(lectureAuthStatus)
				// Project
				.projectNum(projectNum).projectTime(projectTime).projectTeam(projectTeam).projectTool(projectTool)
				.projectMentor(projectMentor)
				.startAt(startAt).endAt(endAt).deadline(deadline).totalDays(totalDays).totalTimes(totalTimes)

				.averageScore(averageScore).reviewCount(reviewCount)
				.createdAt(createdAt).updatedAt(updatedAt)
				// Lists mapping
				.steps(steps.stream().map(s -> s.toDomain(this.lectureId)).toList())
				.adds(adds.stream().map(LectureAddEntity::toDomain).toList())
				.quals(quals.stream().map(LectureQualEntity::toDomain).toList())
				// N:M mapping (Entity -> Domain)
				.teachers(lectureTeachers.stream()
						.map(lt -> lt.getTeacher().toDomain())
						.toList())
				.lectureCurriculums(lectureCurriculums.stream()
						.map(LectureCurriculumEntity::toDomain)
						.toList())
				.specialCurriculums(specialCurriculums.stream()
						.map(LectureSpecialCurriculumEntity::toDomain)
						.toList())
				.build();
	}

	public void updateFields(Lecture lecture) {
		this.orgId = lecture.getOrgId();
		this.lectureName = lecture.getLectureName();
		this.days = lecture.getDays() != null
				? lecture.getDays().stream().map(Enum::name).collect(Collectors.joining(","))
				: null;
		this.startTime = lecture.getStartTime();
		this.endTime = lecture.getEndTime();
		this.lectureLoc = lecture.getLectureLoc();
		this.location = lecture.getLocation();
		this.recruitType = lecture.getRecruitType();
		this.subsidy = lecture.getSubsidy();
		this.lectureFee = lecture.getLectureFee();
		this.eduSubsidy = lecture.getEduSubsidy();
		this.goal = lecture.getGoal();
		this.maxCapacity = lecture.getMaxCapacity();
		this.equipPc = lecture.getEquipPc();
		this.equipMerit = lecture.getEquipMerit();
		this.books = lecture.getBooks();
		this.resume = lecture.getResume();
		this.mockInterview = lecture.getMockInterview();
		this.employmentHelp = lecture.getEmploymentHelp();
		this.afterCompletion = lecture.getAfterCompletion();
		this.url = lecture.getUrl();
		this.lectureImageUrl = lecture.getLectureImageUrl();
		this.status = lecture.getStatus();
		this.lectureAuthStatus = lecture.getLectureAuthStatus();
		// Project
		this.projectNum = lecture.getProjectNum();
		this.projectTime = lecture.getProjectTime();
		this.projectTeam = lecture.getProjectTeam();
		this.projectTool = lecture.getProjectTool();
		this.projectMentor = lecture.getProjectMentor();
		this.startAt = lecture.getStartAt();
		this.endAt = lecture.getEndAt();
		this.deadline = lecture.getDeadline();
		this.totalDays = lecture.getTotalDays();
		this.totalTimes = lecture.getTotalTimes();
		this.updatedAt = LocalDateTime.now();
	}

	public void updateAuthStatus(com.swcampus.domain.lecture.LectureAuthStatus status) {
		this.lectureAuthStatus = status;
		this.updatedAt = LocalDateTime.now();
	}
}