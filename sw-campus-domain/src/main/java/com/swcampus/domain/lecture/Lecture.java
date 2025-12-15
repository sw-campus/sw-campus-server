package com.swcampus.domain.lecture;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Lecture {
	private Long lectureId;
	private Long orgId;
	private String orgName; // 검색 결과용
	private String lectureName;
	private Set<LectureDay> days; // 월,수,금 등
	private LocalTime startTime;
	private LocalTime endTime;
	private LectureLocation lectureLoc; // 온라인/오프라인/혼합
	private String location; // 오프라인 주소 (NULL 허용)
	private RecruitType recruitType; // 내일배움카드 필요/불필요
	private BigDecimal subsidy; // 지원금
	private BigDecimal lectureFee; // 훈련비
	private BigDecimal eduSubsidy; // 훈련수당
	private String goal; // 훈련목표
	private Integer maxCapacity; // 모집정원
	private EquipmentType equipPc; // 시설 PC
	private String equipMerit; // 시설 장점
	private Boolean books; // 교재 지원 유무
	private Boolean resume; // 이력서 첨삭
	private Boolean mockInterview; // 모의면접
	private Boolean employmentHelp; // 협력사 매칭
	private Boolean afterCompletion; // 수료 후 사후관리 (개월) -> (여부)
	private String url; // 신청 링크
	private String lectureImageUrl; // 이미지 URL
	private LectureStatus status; // 모집중, 모집 종료
	private LectureAuthStatus lectureAuthStatus; // 승인 대기, 승인 완료, 승인 반려

	// 프로젝트 관련
	private Integer projectNum;
	private Integer projectTime;
	private String projectTeam;
	private String projectTool;
	private Boolean projectMentor;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private LocalDateTime startAt; // 시작날짜
	private LocalDateTime endAt; // 종료날짜
	private LocalDateTime deadline; // 모집 마감일
	private Integer totalDays; // 총일수
	private Integer totalTimes; // 총 시간

	// 1:N Relationships
	private List<LectureStep> steps; // 선발절차
	private List<LectureAdd> adds; // 추가혜택
	private List<LectureQual> quals; // 지원자격

	// N:M Relationships
	private List<com.swcampus.domain.teacher.Teacher> teachers;
	private List<LectureCurriculum> lectureCurriculums;

	public Lecture close() {
		return this.toBuilder()
				.status(LectureStatus.FINISHED)
				.build();
	}
}