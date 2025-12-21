package com.swcampus.api.member.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "닉네임 사용 가능 여부 응답")
public class NicknameAvailableResponse {

    @Schema(description = "사용 가능 여부", example = "true")
    private boolean available;

    public static NicknameAvailableResponse of(boolean available) {
        return new NicknameAvailableResponse(available);
    }
}
