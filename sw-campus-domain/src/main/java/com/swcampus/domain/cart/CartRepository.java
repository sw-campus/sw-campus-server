package com.swcampus.domain.cart;

import java.util.List;

public interface CartRepository {
    Cart save(Cart cart);

    void deleteByUserIdAndLectureId(Long userId, Long lectureId);

    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);

    long countByUserId(Long userId);

    List<Cart> findAllByUserId(Long userId);
}
