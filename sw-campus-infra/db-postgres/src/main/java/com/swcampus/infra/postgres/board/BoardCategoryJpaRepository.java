package com.swcampus.infra.postgres.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardCategoryJpaRepository extends JpaRepository<BoardCategoryEntity, Long> {
    
    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT board_category_id
            FROM swcampus.board_categories
            WHERE board_category_id = :parentId
            UNION ALL
            SELECT c.board_category_id
            FROM swcampus.board_categories c
            INNER JOIN category_tree ct ON c.board_pid = ct.board_category_id
        )
        SELECT board_category_id FROM category_tree
    """, nativeQuery = true)
    List<Long> findRecursiveChildIds(Long parentId);
}
