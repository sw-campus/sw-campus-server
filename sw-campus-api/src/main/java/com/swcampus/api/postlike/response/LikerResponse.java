package com.swcampus.api.postlike.response;

import com.swcampus.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "좋아요 누른 사용자 정보")
public class LikerResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    public static LikerResponse from(Member member) {
        return LikerResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .build();
    }
}
