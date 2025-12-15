package com.swcampus.api.category.response;

import com.swcampus.domain.category.Curriculum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "커리큘럼 응답")
public class CurriculumResponse {

    @Schema(description = "커리큘럼 ID", example = "1")
    private final Long curriculumId;

    @Schema(description = "카테고리 ID", example = "3")
    private final Long categoryId;

    @Schema(description = "커리큘럼 이름", example = "파이썬 기초")
    private final String curriculumName;

    public CurriculumResponse(Curriculum curriculum) {
        this.curriculumId = curriculum.getCurriculumId();
        this.categoryId = curriculum.getCategoryId();
        this.curriculumName = curriculum.getCurriculumName();
    }
}
