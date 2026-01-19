package com.swcampus.api.board.response;

import com.swcampus.domain.board.BoardCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Schema(description = "게시판 카테고리 트리 응답")
public class BoardCategoryTreeResponse {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리명", example = "자유게시판")
    private String name;

    @Schema(description = "하위 카테고리 목록")
    private List<BoardCategoryTreeResponse> children = new ArrayList<>();

    public BoardCategoryTreeResponse(BoardCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.children = category.getChildren().stream()
                .map(BoardCategoryTreeResponse::new)
                .toList();
    }
}
