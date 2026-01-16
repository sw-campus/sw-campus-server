package com.swcampus.api.admin.request;

import com.swcampus.domain.survey.QuestionPart;
import com.swcampus.domain.survey.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "문항 생성 요청")
public record CreateQuestionRequest(
        @Schema(description = "문항 텍스트", example = "개발 경험이 있으신가요?", required = true)
        @NotBlank(message = "문항 텍스트는 필수입니다")
        String questionText,

        @Schema(description = "문항 타입", example = "RADIO", required = true)
        @NotNull(message = "문항 타입은 필수입니다")
        QuestionType questionType,

        @Schema(description = "필수 여부", example = "true")
        boolean isRequired,

        @Schema(description = "파트 (성향 테스트용)", example = "PART1")
        QuestionPart part,

        @Schema(description = "표시 조건 (JSON)")
        Map<String, Object> showCondition,

        @Schema(description = "메타데이터 (JSON)")
        Map<String, Object> metadata
) {
    // fieldKey는 서버에서 "q{순서}" 형식으로 자동 생성됩니다.
}
