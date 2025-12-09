package com.swcampus.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "메시지 응답")
public class MessageResponse {

    @Schema(description = "메시지", example = "인증 메일이 발송되었습니다")
    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
