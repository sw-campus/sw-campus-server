package com.swcampus.domain.cart;

import com.swcampus.domain.cart.exception.AlreadyInCartException;
import com.swcampus.domain.cart.exception.CartLimitExceededException;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService - 장바구니 서비스 테스트")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartCacheRepository cartCacheRepository;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가/삭제 (Toggle)")
    class AddCart {

        @Test
        @DisplayName("이미 장바구니에 있는 강의라면 예외가 발생한다")
        void duplicate_exception() {
            // given
            Long userId = 1L;
            Long lectureId = 100L;

            when(cartRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> cartService.addCart(userId, lectureId))
                    .isInstanceOf(AlreadyInCartException.class)
                    .hasMessageContaining("이미 장바구니에 담긴 강의입니다");

            verify(cartRepository, never()).deleteByUserIdAndLectureId(anyLong(), anyLong());
            verify(cartCacheRepository, never()).deleteCart(anyLong());
            verify(cartRepository, never()).countByUserId(anyLong());
        }

        @Test
        @DisplayName("장바구니에 없는 강의이고 한도 내라면 추가된다")
        void add_success() {
            // given
            Long userId = 1L;
            Long lectureId = 100L;

            when(cartRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(false);
            when(cartRepository.countByUserId(userId)).thenReturn(5L);

            // when
            cartService.addCart(userId, lectureId);

            // then
            verify(cartRepository).save(any(Cart.class));
            verify(cartCacheRepository).deleteCart(userId);
        }

        @Test
        @DisplayName("장바구니가 꽉 찼다면(10개) 예외가 발생한다")
        void toggle_add_fail_limit() {
            // given
            Long userId = 1L;
            Long lectureId = 100L;

            when(cartRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(false);
            when(cartRepository.countByUserId(userId)).thenReturn(10L);

            // when & then
            assertThatThrownBy(() -> cartService.addCart(userId, lectureId))
                    .isInstanceOf(CartLimitExceededException.class)
                    .hasMessageContaining("장바구니는 최대 10개까지");

            verify(cartRepository, never()).save(any(Cart.class));
            verify(cartCacheRepository, never()).deleteCart(anyLong());
        }
    }

    @Nested
    @DisplayName("장바구니 조회")
    class GetCart {

        @Test
        @DisplayName("요청 시 캐시에 데이터가 있다면 DB 조회 없이 반환한다 (Cache Hit)")
        void cache_hit() {
            // given
            Long userId = 1L;
            List<Long> cachedIds = List.of(100L, 101L);
            Lecture l1 = mock(Lecture.class);
            Lecture l2 = mock(Lecture.class);

            when(cartCacheRepository.getCartLectureIds(userId)).thenReturn(cachedIds);
            when(lectureRepository.findAllByIdsWithoutReviewStats(cachedIds)).thenReturn(List.of(l1, l2));

            // when
            List<Lecture> result = cartService.getCartList(userId);

            // then
            assertThat(result).hasSize(2);
            verify(cartRepository, never()).findAllByUserId(anyLong());
        }

        @Test
        @DisplayName("캐시가 비어있다면 DB에서 조회 후 캐시를 갱신한다 (Cache Miss)")
        void cache_miss() {
            // given
            Long userId = 1L;
            when(cartCacheRepository.getCartLectureIds(userId)).thenReturn(null);

            Cart c1 = Cart.builder().userId(userId).lectureId(100L).build();
            Cart c2 = Cart.builder().userId(userId).lectureId(200L).build();
            when(cartRepository.findAllByUserId(userId)).thenReturn(List.of(c1, c2));

            Lecture l1 = mock(Lecture.class);
            Lecture l2 = mock(Lecture.class);
            when(lectureRepository.findAllByIdsWithoutReviewStats(anyList())).thenReturn(List.of(l1, l2));

            // when
            List<Lecture> result = cartService.getCartList(userId);

            // then
            assertThat(result).hasSize(2);
            verify(cartCacheRepository).saveCartLectureIds(userId, List.of(100L, 200L));
        }

        @Test
        @DisplayName("장바구니가 비어있어도 캐시를 갱신한다 (Empty Cart)")
        void cache_miss_empty() {
            // given
            Long userId = 1L;
            when(cartCacheRepository.getCartLectureIds(userId)).thenReturn(null);
            when(cartRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

            // when
            List<Lecture> result = cartService.getCartList(userId);

            // then
            assertThat(result).isEmpty();
            verify(cartCacheRepository).saveCartLectureIds(userId, Collections.emptyList());
            verify(lectureRepository, never()).findAllByIdsWithoutReviewStats(anyList());
        }
    }
}
