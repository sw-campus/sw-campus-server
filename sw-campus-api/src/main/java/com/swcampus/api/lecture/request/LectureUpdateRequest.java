package com.swcampus.api.lecture.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.swcampus.domain.lecture.CurriculumLevel;
import com.swcampus.domain.lecture.EquipmentType;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureDay;
import com.swcampus.domain.lecture.LectureLocation;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureQualType;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.lecture.SelectionStepType;
import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 수정 요청")
public record LectureUpdateRequest(
        @NotNull(message = "기관 ID는 필수입니다") @Schema(description = "기관 ID", example = "1") Long orgId,

        @NotBlank(message = "강의명은 필수입니다") @Schema(description = "강의명", example = "웹 개발 부트캠프") String lectureName,

        @Schema(description = "수업 요일 (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]") Set<LectureDay> days,

        @Schema(description = "수업 시작 시간", example = "09:00:00") LocalTime startTime,

        @Schema(description = "수업 종료 시간", example = "18:00:00") LocalTime endTime,

        @Schema(description = "수업 장소 유형 (ONLINE, OFFLINE, MIXED)", example = "OFFLINE") LectureLocation lectureLoc,

        @Schema(description = "수업 장소 주소", example = "서울시 강남구 테헤란로 123") String location,

        @Schema(description = "모집 유형 (CARD_REQUIRED, GENERAL)", example = "CARD_REQUIRED") RecruitType recruitType,

        @Schema(description = "훈련 장려금", example = "316000") BigDecimal subsidy,

        @Schema(description = "수강료", example = "0") BigDecimal lectureFee,

        @Schema(description = "교육비 지원금", example = "5000000") BigDecimal eduSubsidy,

        @Schema(description = "교육 목표", example = "풀스택 웹 개발자 양성") String goal,

        @Schema(description = "최대 수강 인원", example = "30") Integer maxCapacity,

        @Schema(description = "장비 제공 유형 (NONE, PC, LAPTOP, PERSONAL)", example = "PC") EquipmentType equipPc,

        @Schema(description = "장비 지원 혜택 설명", example = "최신 노트북 제공") String equipMerit,

        @Schema(description = "교재 제공 여부", example = "true") Boolean books,

        @Schema(description = "이력서 작성 지원", example = "true") Boolean resume,

        @Schema(description = "모의 면접 지원", example = "true") Boolean mockInterview,

        @Schema(description = "취업 연계 지원", example = "true") Boolean employmentHelp,

        @Schema(description = "수료 후 사후관리 지원 여부", example = "true") Boolean afterCompletion,

        @Schema(description = "강의 상세 페이지 URL", example = "https://example.com/lecture/1") String url,

        @Schema(description = "프로젝트 수", example = "5") Integer projectNum,

        @Schema(description = "프로젝트 총 시간", example = "200") Integer projectTime,

        @Schema(description = "프로젝트 팀 구성", example = "4~5인 1팀") String projectTeam,

        @Schema(description = "프로젝트 사용 도구", example = "Git, Jira, Slack") String projectTool,

        @Schema(description = "프로젝트 멘토 지원 여부", example = "true") Boolean projectMentor,

        @Schema(description = "교육 시작일 (yyyy-MM-dd)", example = "2025-03-01") String startAt,

        @Schema(description = "교육 종료일 (yyyy-MM-dd)", example = "2025-08-31") String endAt,

        @Schema(description = "모집 마감일 (yyyy-MM-dd)", example = "2025-02-15") String deadline,

        @Schema(description = "총 교육 일수", example = "120") Integer totalDays,

        @Schema(description = "총 교육 시간", example = "960") Integer totalTimes,

        @Schema(description = "선발 절차 목록") List<StepRequest> steps,

        @Schema(description = "추가 혜택 목록") List<AddRequest> adds,

        @Schema(description = "지원 자격 목록") List<QualRequest> quals,

        @Schema(description = "강사 목록") List<TeacherRequest> teachers,

        @Schema(description = "커리큘럼 목록") List<CurriculumRequest> curriculums) {

    public Lecture toDomain() {
        return Lecture.builder()
                .orgId(orgId)
                .lectureName(lectureName)
                .days(days != null ? days : Collections.emptySet())
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
                .equipMerit(equipMerit)
                .books(books)
                .resume(resume)
                .mockInterview(mockInterview)
                .employmentHelp(employmentHelp)
                .afterCompletion(afterCompletion)
                .url(url)

                // Status fields are deliberately omitted here to be handled by the service
                // (preserving existing or resetting)

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

    @Schema(description = "선발 절차")
    public record StepRequest(
            @Schema(description = "절차 유형 (DOCUMENT, CODING_TEST, INTERVIEW, PRE_TASK)", example = "INTERVIEW") SelectionStepType stepType,

            @Schema(description = "절차 순서", example = "1") Integer stepOrder) {
        public LectureStep toDomain() {
            return LectureStep.builder()
                    .stepType(stepType)
                    .stepOrder(stepOrder)
                    .build();
        }
    }

    @Schema(description = "추가 혜택")
    public record AddRequest(
            @Schema(description = "혜택명", example = "취업 연계 프로그램") String addName) {
        public LectureAdd toDomain() {
            return LectureAdd.builder()
                    .addName(addName)
                    .build();
        }
    }

    @Schema(description = "지원 자격")
    public record QualRequest(
            @Schema(description = "자격 유형 (REQUIRED, PREFERRED)", example = "REQUIRED") LectureQualType type,

            @Schema(description = "자격 내용", example = "프로그래밍 기초 지식 보유자") String text) {
        public LectureQual toDomain() {
            return LectureQual.builder()
                    .type(type)
                    .text(text)
                    .build();
        }
    }

    @Schema(description = "강사 정보")
    public record TeacherRequest(
            @Schema(description = "강사 ID (기존 강사 연결 시)", hidden = true) Long teacherId,
            @Schema(description = "강사명 (신규 강사 생성 시)", example = "김개발") String teacherName,
            @Schema(description = "강사 소개 (신규 강사 생성 시)", example = "10년차 백엔드 개발자") String teacherDescription) {
        public Teacher toDomain() {
            return Teacher.builder()
                    .teacherId(teacherId)
                    .teacherName(teacherName)
                    .teacherDescription(teacherDescription)
                    .build();
        }
    }

    @Schema(description = "커리큘럼 연결 정보")
    public record CurriculumRequest(
            @Schema(description = "커리큘럼 ID", example = "1") Long curriculumId,

            @Schema(description = "난이도 (NONE, BASIC, ADVANCED)", hidden = true) CurriculumLevel level) {
        // 커리큘럼은 연결 정보(Info) 객체로 변환
        public LectureCurriculum toDomainInfo() {
            return LectureCurriculum.builder()
                    .curriculumId(curriculumId)
                    .level(level != null ? level : CurriculumLevel.NONE)
                    .build();
        }
    }
}
