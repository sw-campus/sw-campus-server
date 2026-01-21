package com.swcampus.infra.postgres.cart;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
    List<CartEntity> findAllByUserIdOrderByIdDesc(Long userId);

    void deleteByUserIdAndLectureId(Long userId, Long lectureId);

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);

    long countByUserId(Long userId);
}
