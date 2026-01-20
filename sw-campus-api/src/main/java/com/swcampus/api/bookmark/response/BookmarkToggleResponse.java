package com.swcampus.api.bookmark.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookmarkToggleResponse {
    private final boolean bookmarked;
    private final String message;

    public static BookmarkToggleResponse added() {
        return new BookmarkToggleResponse(true, "북마크에 추가되었습니다.");
    }

    public static BookmarkToggleResponse removed() {
        return new BookmarkToggleResponse(false, "북마크가 해제되었습니다.");
    }
}
