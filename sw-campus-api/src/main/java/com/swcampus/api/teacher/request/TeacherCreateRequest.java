package com.swcampus.api.teacher.request;

import com.swcampus.domain.teacher.Teacher;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "강사 등록 요청")
public record TeacherCreateRequest(
        @NotBlank(message = "강사명은 필수입니다") @Schema(description = "강사명", example = "김개발") String teacherName,

        @NotBlank(message = "강사 소개는 필수입니다") @Schema(description = "강사 소개", example = "10년차 백엔드 개발자") String teacherDescription) {

    public Teacher toDomain() {
        return Teacher.builder()
                .teacherName(teacherName)
                .teacherDescription(teacherDescription)
                .build();
    }
}
