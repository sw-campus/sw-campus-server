package com.swcampus.api.lecture.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.swcampus.domain.lecture.EquipmentType;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureDay;
import com.swcampus.domain.lecture.LectureLocation;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 강의 승인/반려 응답")
public class AdminLectureApprovalResponse {
    @Schema(description = "강의 ID", example = "1")
    private Long lectureId;

    @Schema(description = "승인 상태", example = "APPROVED")
    private LectureAuthStatus lectureAuthStatus;

    @Schema(description = "메시지", example = "강의가 승인되었습니다.")
    private String message;

    // 강의 기본 정보
    @Schema(description = "강의명", example = "웹 개발 부트캠프")
    private String lectureName;

    @Schema(description = "수업 요일")
    private Set<LectureDay> days;

    @Schema(description = "수업 시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "수업 종료 시간", example = "18:00:00")
    private LocalTime endTime;

    @Schema(description = "수업 장소 유형", example = "OFFLINE")
    private LectureLocation lectureLoc;

    @Schema(description = "수업 장소 주소", example = "서울시 강남구 테헤란로 123")
    private String location;

    @Schema(description = "모집 유형", example = "CARD_REQUIRED")
    private RecruitType recruitType;

    @Schema(description = "정부 지원금", example = "316000")
    private BigDecimal subsidy;

    @Schema(description = "자기부담금", example = "0")
    private BigDecimal lectureFee;

    @Schema(description = "교육지원금", example = "5000000")
    private BigDecimal eduSubsidy;

    @Schema(description = "교육 목표", example = "풀스택 웹 개발자 양성")
    private String goal;

    @Schema(description = "최대 수강 인원", example = "30")
    private Integer maxCapacity;

    @Schema(description = "장비 제공 유형", example = "PC")
    private EquipmentType equipPc;

    @Schema(description = "장비 지원 혜택 설명", example = "최신 노트북 제공")
    private String equipMerit;

    @Schema(description = "교재 제공 여부", example = "true")
    private Boolean books;

    @Schema(description = "이력서 작성 지원", example = "true")
    private Boolean resume;

    @Schema(description = "모의 면접 지원", example = "true")
    private Boolean mockInterview;

    @Schema(description = "취업 연계 지원", example = "true")
    private Boolean employmentHelp;

    @Schema(description = "수료 후 사후관리 지원 여부", example = "true")
    private Boolean afterCompletion;

    @Schema(description = "강의 상세 페이지 URL", example = "https://example.com/lecture/1")
    private String url;

    @Schema(description = "프로젝트 수", example = "5")
    private Integer projectNum;

    @Schema(description = "프로젝트 총 시간", example = "200")
    private Integer projectTime;

    @Schema(description = "프로젝트 팀 구성", example = "4~5인 1팀")
    private String projectTeam;

    @Schema(description = "프로젝트 사용 도구", example = "Git, Jira, Slack")
    private String projectTool;

    @Schema(description = "프로젝트 멘토 지원 여부", example = "true")
    private Boolean projectMentor;

    @Schema(description = "교육 시작일")
    private LocalDateTime startAt;

    @Schema(description = "교육 종료일")
    private LocalDateTime endAt;

    @Schema(description = "모집 마감일")
    private LocalDateTime deadline;

    @Schema(description = "총 교육 일수", example = "120")
    private Integer totalDays;

    @Schema(description = "총 교육 시간", example = "960")
    private Integer totalTimes;

    @Schema(description = "선발 절차 목록")
    private List<StepResponse> steps;

    @Schema(description = "추가 혜택 목록")
    private List<AddResponse> adds;

    @Schema(description = "지원 자격 목록")
    private List<QualResponse> quals;

    @Schema(description = "강사 목록")
    private List<TeacherResponse> teachers;

    @Schema(description = "커리큘럼 목록")
    private List<CurriculumResponse> curriculums;

    public static AdminLectureApprovalResponse of(Lecture lecture, String message) {
        return AdminLectureApprovalResponse.builder()
                .lectureId(lecture.getLectureId())
                .lectureAuthStatus(lecture.getLectureAuthStatus())
                .message(message)
                .lectureName(lecture.getLectureName())
                .days(lecture.getDays())
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
                .steps(lecture.getSteps() != null
                        ? lecture.getSteps().stream().map(StepResponse::from).toList()
                        : List.of())
                .adds(lecture.getAdds() != null
                        ? lecture.getAdds().stream().map(AddResponse::from).toList()
                        : List.of())
                .quals(lecture.getQuals() != null
                        ? lecture.getQuals().stream().map(QualResponse::from).toList()
                        : List.of())
                .teachers(lecture.getTeachers() != null
                        ? lecture.getTeachers().stream().map(TeacherResponse::from).toList()
                        : List.of())
                .curriculums(lecture.getLectureCurriculums() != null
                        ? lecture.getLectureCurriculums().stream().map(CurriculumResponse::from).toList()
                        : List.of())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "선발 절차 응답")
    public static class StepResponse {
        @Schema(description = "절차 유형", example = "INTERVIEW")
        private String stepType;

        @Schema(description = "절차 순서", example = "1")
        private Integer stepOrder;

        public static StepResponse from(LectureStep step) {
            return StepResponse.builder()
                    .stepType(step.getStepType() != null ? step.getStepType().name() : null)
                    .stepOrder(step.getStepOrder())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "추가 혜택 응답")
    public static class AddResponse {
        @Schema(description = "혜택명", example = "취업 연계 프로그램")
        private String addName;

        public static AddResponse from(LectureAdd add) {
            return AddResponse.builder()
                    .addName(add.getAddName())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "지원 자격 응답")
    public static class QualResponse {
        @Schema(description = "자격 유형", example = "REQUIRED")
        private String type;

        @Schema(description = "자격 내용", example = "프로그래밍 기초 지식 보유자")
        private String text;

        public static QualResponse from(LectureQual qual) {
            return QualResponse.builder()
                    .type(qual.getType() != null ? qual.getType().name() : null)
                    .text(qual.getText())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "강사 정보 응답")
    public static class TeacherResponse {
        @Schema(description = "강사 ID", example = "1")
        private Long teacherId;

        @Schema(description = "강사명", example = "김개발")
        private String teacherName;

        @Schema(description = "강사 소개", example = "10년차 백엔드 개발자")
        private String teacherDescription;

        public static TeacherResponse from(Teacher teacher) {
            return TeacherResponse.builder()
                    .teacherId(teacher.getTeacherId())
                    .teacherName(teacher.getTeacherName())
                    .teacherDescription(teacher.getTeacherDescription())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "커리큘럼 응답")
    public static class CurriculumResponse {
        @Schema(description = "커리큘럼 ID", example = "1")
        private Long curriculumId;

        @Schema(description = "난이도", example = "BASIC")
        private String level;

        public static CurriculumResponse from(LectureCurriculum curriculum) {
            return CurriculumResponse.builder()
                    .curriculumId(curriculum.getCurriculumId())
                    .level(curriculum.getLevel() != null ? curriculum.getLevel().name() : null)
                    .build();
        }
    }
}
