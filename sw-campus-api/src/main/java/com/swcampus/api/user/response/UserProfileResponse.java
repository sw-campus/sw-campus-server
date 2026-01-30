package com.swcampus.api.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "유저 프로필 응답")
public class UserProfileResponse {

    @Schema(description = "유저 ID", example = "1")
    private final Long userId;

    @Schema(description = "닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "가입일시")
    private final LocalDateTime joinedAt;

    @Schema(description = "작성한 게시글 수", example = "10")
    private final long postCount;

    @Schema(description = "작성한 댓글 수", example = "15")
    private final long commentCount;

    public static UserProfileResponse of(Long userId, String nickname, LocalDateTime joinedAt,
                                         long postCount, long commentCount) {
        return UserProfileResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .joinedAt(joinedAt)
                .postCount(postCount)
                .commentCount(commentCount)
                .build();
    }
}
