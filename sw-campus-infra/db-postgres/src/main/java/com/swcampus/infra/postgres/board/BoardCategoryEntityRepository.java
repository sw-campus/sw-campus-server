package com.swcampus.infra.postgres.board;

import com.swcampus.domain.board.BoardCategory;
import com.swcampus.domain.board.BoardCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardCategoryEntityRepository implements BoardCategoryRepository {

    private final BoardCategoryJpaRepository jpaRepository;

    @Override
    public List<BoardCategory> findAll() {
        return jpaRepository.findAll().stream()
                .map(BoardCategoryEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<BoardCategory> findById(Long id) {
        return jpaRepository.findById(id)
                .map(BoardCategoryEntity::toDomain);
    }

    @Override
    public BoardCategory save(BoardCategory boardCategory) {
        BoardCategoryEntity entity = BoardCategoryEntity.from(boardCategory);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public List<Long> findRecursiveChildIds(Long parentId) {
        return jpaRepository.findRecursiveChildIds(parentId);
    }
}
