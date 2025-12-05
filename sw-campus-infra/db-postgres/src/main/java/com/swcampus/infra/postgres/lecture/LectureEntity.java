package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "LECTURES")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LECTURE_ID")
	private Long lectureId;

	@Column(name = "ORG_ID", nullable = false)
	private Long orgId;

	@Column(name = "LECTURE_NAME", nullable = false)
	private String lectureName;

	@Column(name = "DAYS")
	private String days;

	@Column(name = "START_TIME", nullable = false)
	private LocalTime startTime;

	@Column(name = "END_TIME", nullable = false)
	private LocalTime endTime;

	@Column(name = "LECTURE_LOC", nullable = false)
	private String lectureLoc;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "RECRUIT_TYPE", nullable = false)
	private String recruitType;

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
	private String equipPc;

	@Column(name = "EQUIP_LAPTOP")
	private String equipLaptop;

	@Column(name = "EQUIP_GPU")
	private Boolean equipGpu;

	@Column(name = "BOOKS", nullable = false)
	private Boolean books;

	@Column(name = "RESUME", nullable = false)
	private Boolean resume;

	@Column(name = "MOCK_INTERVIEW", nullable = false)
	private Boolean mockInterview;

	@Column(name = "EMPLOYMENT_HELP", nullable = false)
	private Boolean employmentHelp;

	@Column(name = "AFTER_COMPLETION")
	private Integer afterCompletion;

	@Column(name = "URL")
	private String url;

	@Column(name = "LECTURE_IMAGE_URL")
	private String lectureImageUrl;

	@Column(name = "STATUS", nullable = false)
	private String status;

	@Column(name = "LECTURE_AUTH_STATUS")
	private Boolean lectureAuthStatus;

	@CreationTimestamp
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	// --- 1:N Relationships ---

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CohortEntity> cohorts = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureStepEntity> steps = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureAddEntity> adds = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureQualEntity> quals = new ArrayList<>();

	// --- N:M Relationships (Mapping Tables) ---

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureTeacherEntity> lectureTeachers = new ArrayList<>();

	@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<LectureCurriculumEntity> lectureCurriculums = new ArrayList<>();

	// --- Mapper Methods ---

	public com.swcampus.domain.lecture.Lecture toDomain() {
		return com.swcampus.domain.lecture.Lecture.builder()
			.lectureId(lectureId).orgId(orgId).lectureName(lectureName)
			.days(days).startTime(startTime).endTime(endTime)
			.lectureLoc(lectureLoc).location(location).recruitType(recruitType)
			.subsidy(subsidy).lectureFee(lectureFee).eduSubsidy(eduSubsidy)
			.goal(goal).maxCapacity(maxCapacity)
			.equipPc(equipPc).equipLaptop(equipLaptop).equipGpu(equipGpu)
			.books(books).resume(resume).mockInterview(mockInterview)
			.employmentHelp(employmentHelp).afterCompletion(afterCompletion)
			.url(url).lectureImageUrl(lectureImageUrl)
			.status(status).lectureAuthStatus(lectureAuthStatus)
			.createdAt(createdAt).updatedAt(updatedAt)
			// Lists mapping
			.cohorts(cohorts.stream().map(CohortEntity::toDomain).toList())
			.steps(steps.stream().map(LectureStepEntity::toDomain).toList())
			.adds(adds.stream().map(LectureAddEntity::toDomain).toList())
			.quals(quals.stream().map(LectureQualEntity::toDomain).toList())
			// N:M mapping (Entity -> Domain)
			.teachers(lectureTeachers.stream()
				.map(lt -> lt.getTeacher().toDomain())
				.toList())
			.lectureCurriculums(lectureCurriculums.stream()
				.map(LectureCurriculumEntity::toDomain)
				.toList())
			.build();
	}
}