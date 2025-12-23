package com.swcampus.api.teacher.response;

import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 정보 응답")
public record TeacherResponse(
        @Schema(description = "강사 ID", example = "1") Long teacherId,
        @Schema(description = "강사명", example = "김개발") String teacherName,
        @Schema(description = "강사 소개", example = "10년차 백엔드 개발자") String teacherDescription,
        @Schema(description = "강사 이미지 URL", example = "https://example.com/teacher1.jpg") String teacherImageUrl) {

    public static TeacherResponse from(Teacher teacher) {
        return new TeacherResponse(
                teacher.getTeacherId(),
                teacher.getTeacherName(),
                teacher.getTeacherDescription(),
                teacher.getTeacherImageUrl());
    }
}
