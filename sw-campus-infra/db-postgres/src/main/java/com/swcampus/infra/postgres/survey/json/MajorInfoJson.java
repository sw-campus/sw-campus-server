package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.MajorInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MajorInfoJson {
    private boolean hasMajor;
    private String majorName;

    public static MajorInfoJson from(MajorInfo domain) {
        if (domain == null) return null;

        MajorInfoJson json = new MajorInfoJson();
        json.setHasMajor(domain.isHasMajor());
        json.setMajorName(domain.getMajorName());
        return json;
    }

    public MajorInfo toDomain() {
        return MajorInfo.builder()
                .hasMajor(hasMajor)
                .majorName(majorName)
                .build();
    }
}
