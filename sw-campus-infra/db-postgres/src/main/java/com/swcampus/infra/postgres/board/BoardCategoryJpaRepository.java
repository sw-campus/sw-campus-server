package com.swcampus.infra.postgres.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardCategoryJpaRepository extends JpaRepository<BoardCategoryEntity, Long> {
    
    // 계층 구조 정렬을 위해 ID 순 등 적절한 정렬 필요 시 추가
    // 현재는 단순 전체 조회 사용
}
