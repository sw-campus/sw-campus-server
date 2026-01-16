package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.AptitudeTest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeTestJson {
    private Map<String, Integer> part1Answers;
    private Map<String, Integer> part2Answers;
    private Map<String, String> part3Answers;

    public static AptitudeTestJson from(AptitudeTest domain) {
        if (domain == null) return null;
        return new AptitudeTestJson(
                domain.getPart1Answers(),
                domain.getPart2Answers(),
                domain.getPart3Answers()
        );
    }

    public AptitudeTest toDomain() {
        return AptitudeTest.builder()
                .part1Answers(part1Answers)
                .part2Answers(part2Answers)
                .part3Answers(part3Answers)
                .build();
    }
}
