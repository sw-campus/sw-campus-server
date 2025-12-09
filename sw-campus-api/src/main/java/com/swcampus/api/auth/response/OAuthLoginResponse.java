package com.swcampus.api.auth.response;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.oauth.OAuthLoginResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthLoginResponse {

    private Long memberId;
    private String email;
    private String name;
    private String nickname;
    private String role;

    public static OAuthLoginResponse from(OAuthLoginResult result) {
        Member member = result.getMember();
        return OAuthLoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .build();
    }
}
