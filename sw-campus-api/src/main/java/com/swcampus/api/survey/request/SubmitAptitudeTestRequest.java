package com.swcampus.api.survey.request;

import com.swcampus.domain.survey.AptitudeTest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "성향 테스트 제출 요청")
public class SubmitAptitudeTestRequest {

    @Schema(description = "Part 1 응답 (논리/사고력) - q1~q4, 값은 선택지 번호", example = "{\"q1\": 3, \"q2\": 2, \"q3\": 2, \"q4\": 2}")
    @NotNull(message = "Part 1 응답은 필수입니다")
    @Size(min = 4, max = 4, message = "Part 1은 4문항 모두 응답해야 합니다")
    private Map<String, Integer> part1Answers;

    @Schema(description = "Part 2 응답 (끈기/학습태도) - q5~q8, 값은 선택지 번호", example = "{\"q5\": 2, \"q6\": 1, \"q7\": 3, \"q8\": 2}")
    @NotNull(message = "Part 2 응답은 필수입니다")
    @Size(min = 4, max = 4, message = "Part 2는 4문항 모두 응답해야 합니다")
    private Map<String, Integer> part2Answers;

    @Schema(description = "Part 3 응답 (직무 성향) - q9~q15, 값은 F/B/D", example = "{\"q9\": \"B\", \"q10\": \"B\", \"q11\": \"D\", \"q12\": \"B\", \"q13\": \"B\", \"q14\": \"B\", \"q15\": \"B\"}")
    @NotNull(message = "Part 3 응답은 필수입니다")
    @Size(min = 7, max = 7, message = "Part 3은 7문항 모두 응답해야 합니다")
    private Map<String, @Pattern(regexp = "^[FBD]$", message = "Part 3 응답값은 F, B, D 중 하나여야 합니다") String> part3Answers;

    public AptitudeTest toDomain() {
        return AptitudeTest.builder()
                .part1Answers(part1Answers)
                .part2Answers(part2Answers)
                .part3Answers(part3Answers)
                .build();
    }
}
