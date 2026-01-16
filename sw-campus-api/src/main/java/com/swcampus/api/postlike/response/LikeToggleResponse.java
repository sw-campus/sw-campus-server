package com.swcampus.api.postlike.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeToggleResponse {
    private final boolean liked;
    private final String message;

    public static LikeToggleResponse added() {
        return new LikeToggleResponse(true, "추천하였습니다.");
    }

    public static LikeToggleResponse removed() {
        return new LikeToggleResponse(false, "추천을 취소하였습니다.");
    }
}
