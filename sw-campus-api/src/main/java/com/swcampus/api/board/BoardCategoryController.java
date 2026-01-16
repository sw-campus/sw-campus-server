package com.swcampus.api.board;

import com.swcampus.api.board.response.BoardCategoryTreeResponse;
import com.swcampus.domain.board.BoardCategory;
import com.swcampus.domain.board.BoardCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/board-categories")
@RequiredArgsConstructor
@Tag(name = "BoardCategory", description = "게시판 카테고리 API")
public class BoardCategoryController {

    private final BoardCategoryService boardCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "게시판 카테고리 트리 조회", description = "게시판 카테고리의 전체 트리를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<BoardCategoryTreeResponse>> getCategoryTree() {
        List<BoardCategory> categoryTree = boardCategoryService.getCategoryTree();
        List<BoardCategoryTreeResponse> response = categoryTree.stream()
                .map(BoardCategoryTreeResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
