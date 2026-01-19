package com.swcampus.api.post.response;

import com.swcampus.domain.post.AdjacentPosts;
import com.swcampus.domain.post.PostSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이전/다음 게시글 응답")
public class AdjacentPostResponse {

    @Schema(description = "이전 게시글 정보")
    private AdjacentPostItem previous;

    @Schema(description = "다음 게시글 정보")
    private AdjacentPostItem next;

    @Getter
    @Builder
    @Schema(description = "이전/다음 게시글 아이템")
    public static class AdjacentPostItem {
        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 제목", example = "첫 번째 게시글")
        private String title;

        @Schema(description = "카테고리 이름", example = "자유게시판")
        private String categoryName;

        public static AdjacentPostItem from(PostSummary summary) {
            if (summary == null) return null;
            return AdjacentPostItem.builder()
                    .id(summary.getPost().getId())
                    .title(summary.getPost().getTitle())
                    .categoryName(summary.getCategoryName())
                    .build();
        }
    }

    public static AdjacentPostResponse from(AdjacentPosts adjacentPosts) {
        return AdjacentPostResponse.builder()
                .previous(AdjacentPostItem.from(adjacentPosts.getPrevious()))
                .next(AdjacentPostItem.from(adjacentPosts.getNext()))
                .build();
    }
}
