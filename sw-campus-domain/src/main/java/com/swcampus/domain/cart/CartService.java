package com.swcampus.domain.cart;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartCacheRepository cartCacheRepository;
    private final LectureRepository lectureRepository;

    @Transactional
    public boolean addCart(Long userId, Long lectureId) {
        boolean exists = cartRepository.existsByUserIdAndLectureId(userId, lectureId);

        if (exists) {
            cartRepository.deleteByUserIdAndLectureId(userId, lectureId);
            cartCacheRepository.deleteCart(userId); // Simple invalidation
            return false;
        } else {
            // Add
            long count = cartRepository.countByUserId(userId);
            if (count >= 10) {
                throw new IllegalStateException("장바구니는 최대 10개까지 추가할 수 있습니다");
            }
            cartRepository.save(Cart.builder()
                    .userId(userId)
                    .lectureId(lectureId)
                    .build());
            cartCacheRepository.deleteCart(userId); // Invalidate cache
            return true; // Added
        }
    }

    @Transactional
    public void removeCart(Long userId, Long lectureId) {
        cartRepository.deleteByUserIdAndLectureId(userId, lectureId);
        cartCacheRepository.deleteCart(userId);
    }

    @Transactional(readOnly = true)
    public List<Lecture> getCartList(Long userId) {
        // 1. Try Cache
        List<Long> cachedIds = cartCacheRepository.getCartLectureIds(userId);

        if (cachedIds != null && !cachedIds.isEmpty()) {
            return lectureRepository.findAllByIds(cachedIds);
        }

        // 2. Fallback DB
        List<Cart> carts = cartRepository.findAllByUserId(userId);
        List<Long> lectureIds = carts.stream()
                .map(Cart::getLectureId)
                .toList();

        // 3. Update Cache (Even if empty)
        cartCacheRepository.saveCartLectureIds(userId, lectureIds);

        if (lectureIds.isEmpty()) {
            return List.of();
        }

        return lectureRepository.findAllByIds(lectureIds);
    }
}
