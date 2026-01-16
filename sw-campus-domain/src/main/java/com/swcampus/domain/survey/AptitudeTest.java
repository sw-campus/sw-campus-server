package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptitudeTest {
    /**
     * Part 1 응답 (논리/사고력) - q1~q4, 값은 선택지 번호 (1~4)
     */
    private Map<String, Integer> part1Answers;

    /**
     * Part 2 응답 (끈기/학습태도) - q5~q8, 값은 선택지 번호 (1~3)
     */
    private Map<String, Integer> part2Answers;

    /**
     * Part 3 응답 (직무 성향) - q9~q15, 값은 F/B/D
     */
    private Map<String, String> part3Answers;

    @Builder
    public AptitudeTest(
            Map<String, Integer> part1Answers,
            Map<String, Integer> part2Answers,
            Map<String, String> part3Answers
    ) {
        this.part1Answers = part1Answers;
        this.part2Answers = part2Answers;
        this.part3Answers = part3Answers;
    }
}
