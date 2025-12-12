package com.swcampus.api.teacher.request;

import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 등록 요청")
public record TeacherCreateRequest(
        @Schema(description = "강사명", example = "김개발") String teacherName,
        @Schema(description = "강사 소개", example = "10년차 백엔드 개발자") String teacherDescription) {

    public Teacher toDomain() {
        return Teacher.builder()
                .teacherName(teacherName)
                .teacherDescription(teacherDescription)
                .build();
    }
}
