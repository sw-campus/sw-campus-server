package com.swcampus.infra.postgres.cart;

import com.swcampus.domain.cart.Cart;
import com.swcampus.domain.cart.CartRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartEntityRepository implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = CartEntity.from(cart);
        CartEntity savedEntity = cartJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public void deleteByUserIdAndLectureId(Long userId, Long lectureId) {
        cartJpaRepository.deleteByUserIdAndLectureId(userId, lectureId);
    }

    @Override
    public boolean existsByUserIdAndLectureId(Long userId, Long lectureId) {
        return cartJpaRepository.existsByUserIdAndLectureId(userId, lectureId);
    }

    @Override
    public long countByUserId(Long userId) {
        return cartJpaRepository.countByUserId(userId);
    }

    @Override
    public List<Cart> findAllByUserId(Long userId) {
        return cartJpaRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(CartEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByUserId(Long userId) {
        cartJpaRepository.deleteByUserId(userId);
    }
}
