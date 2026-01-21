package com.swcampus.domain.cart;

import com.swcampus.domain.cart.exception.AlreadyInCartException;
import com.swcampus.domain.cart.exception.CartLimitExceededException;

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

    /**
     * 장바구니에 강의를 추가합니다.
     *
     * <p>
     * 이미 장바구니에 존재하는 강의라면 {@link AlreadyInCartException}을 던집니다.
     * 장바구니에는 최대 10개의 강의만 담을 수 있습니다.
     * </p>
     *
     * @param userId    사용자 ID
     * @param lectureId 강의 ID
     * @throws AlreadyInCartException     이미 장바구니에 존재하는 경우
     * @throws CartLimitExceededException 장바구니 제한(10개)을 초과한 경우
     */
    @Transactional
    public void addCart(Long userId, Long lectureId) {
        boolean exists = cartRepository.existsByUserIdAndLectureId(userId, lectureId);

        if (exists) {
            throw new AlreadyInCartException("이미 장바구니에 담긴 강의입니다.");
        }

        // Add
        long count = cartRepository.countByUserId(userId);
        if (count >= 10) {
            throw new CartLimitExceededException("장바구니는 최대 10개까지 추가할 수 있습니다");
        }
        cartRepository.save(Cart.builder()
                .userId(userId)
                .lectureId(lectureId)
                .build());
        cartCacheRepository.deleteCart(userId); // Invalidate cache
    }

    /**
     * 장바구니에서 특정 강의를 제거합니다.
     *
     * @param userId    사용자 ID
     * @param lectureId 강의 ID
     */
    @Transactional
    public void removeCart(Long userId, Long lectureId) {
        cartRepository.deleteByUserIdAndLectureId(userId, lectureId);
        cartCacheRepository.deleteCart(userId);
    }

    /**
     * 사용자의 장바구니에 담긴 강의 목록을 조회합니다.
     *
     * <p>
     * 성능 적화를 위해 캐시(`CartCacheRepository`)를 우선 조회하며,
     * 캐시 미스 시 DB에서 조회하고 캐시를 갱신합니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 장바구니에 담긴 강의 목록 ({@link Lecture})
     */
    @Transactional(readOnly = true)
    public List<Lecture> getCartList(Long userId) {
        // 1. Try Cache
        List<Long> cachedIds = cartCacheRepository.getCartLectureIds(userId);

        if (cachedIds != null && !cachedIds.isEmpty()) {
            // 장바구니에서는 리뷰 통계 불필요 → 쿼리 1회 절약
            return lectureRepository.findAllByIdsWithoutReviewStats(cachedIds);
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

        // 장바구니에서는 리뷰 통계 불필요 → 쿼리 1회 절약
        return lectureRepository.findAllByIdsWithoutReviewStats(lectureIds);
    }
}
