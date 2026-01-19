package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.ProgrammingExperience;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgrammingExperienceJson {
    private boolean hasExperience;
    private String bootcampName;

    public static ProgrammingExperienceJson from(ProgrammingExperience domain) {
        if (domain == null) return null;
        return new ProgrammingExperienceJson(domain.isHasExperience(), domain.getBootcampName());
    }

    public ProgrammingExperience toDomain() {
        return ProgrammingExperience.builder()
                .hasExperience(hasExperience)
                .bootcampName(bootcampName)
                .build();
    }
}
