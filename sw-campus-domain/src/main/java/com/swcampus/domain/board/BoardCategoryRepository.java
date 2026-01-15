package com.swcampus.domain.board;

import java.util.List;
import java.util.Optional;

public interface BoardCategoryRepository {
    List<BoardCategory> findAll();
    Optional<BoardCategory> findById(Long id);
    BoardCategory save(BoardCategory boardCategory);
}
