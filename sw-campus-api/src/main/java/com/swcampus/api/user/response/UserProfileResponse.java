package com.swcampus.api.user.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {
    private final Long userId;
    private final String nickname;
    private final LocalDateTime joinedAt;
    private final long postCount;

    public static UserProfileResponse of(Long userId, String nickname, LocalDateTime joinedAt, long postCount) {
        return UserProfileResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .joinedAt(joinedAt)
                .postCount(postCount)
                .build();
    }
}
