package com.swcampus.domain.cart;

import java.util.List;

public interface CartCacheRepository {
    List<Long> getCartLectureIds(Long userId);

    void saveCartLectureIds(Long userId, List<Long> lectureIds);

    void deleteCart(Long userId);
}
