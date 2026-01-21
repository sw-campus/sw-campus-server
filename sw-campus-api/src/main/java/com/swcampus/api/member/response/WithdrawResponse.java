package com.swcampus.api.member.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "회원 탈퇴 응답")
public record WithdrawResponse(
    @Schema(description = "탈퇴 성공 여부")
    boolean success,
    
    @Schema(description = "OAuth 연결된 프로바이더 목록 (연결 해제 안내용)", example = "[\"GITHUB\", \"GOOGLE\"]")
    List<String> oauthProviders,
    
    @Schema(description = "응답 메시지")
    String message
) {
    public static WithdrawResponse success(List<String> oauthProviders) {
        return new WithdrawResponse(true, oauthProviders, "회원 탈퇴가 완료되었습니다.");
    }
}
