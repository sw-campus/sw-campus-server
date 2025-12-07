package com.swcampus.domain.lecture;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Lecture {
	private Long lectureId;
	private Long orgId;
	private String lectureName;
	private String days; // 월,수,금 등
	private LocalTime startTime;
	private LocalTime endTime;
	private String lectureLoc; // 온라인/오프라인/혼합
	private String location; // 오프라인 주소 (NULL 허용)
	private String recruitType; // 내일배움카드 필요/불필요
	private BigDecimal subsidy; // 지원금
	private BigDecimal lectureFee; // 훈련비
	private BigDecimal eduSubsidy; // 훈련수당
	private String goal; // 훈련목표
	private Integer maxCapacity; // 모집정원
	private String equipPc; // 시설 PC 사양
	private String equipLaptop; // 시설 노트북 사양
	private Boolean equipGpu; // GPU 여부
	private Boolean books; // 교재 지원 유무
	private Boolean resume; // 이력서 첨삭
	private Boolean mockInterview; // 모의면접
	private Boolean employmentHelp; // 협력사 매칭
	private Integer afterCompletion; // 수료 후 사후관리 (개월)
	private String url; // 신청 링크
	private String lectureImageUrl; // 이미지 URL
	private String status; // 모집중, 진행중, 종료
	private Boolean lectureAuthStatus; // 등록 상태
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// 1:N Relationships
	private List<Cohort> cohorts; // 기수
	private List<LectureStep> steps; // 선발절차
	private List<LectureAdd> adds; // 추가혜택
	private List<LectureQual> quals; // 지원자격

	// N:M Relationships
	private List<com.swcampus.domain.teacher.Teacher> teachers;
	private List<LectureCurriculum> lectureCurriculums;
}