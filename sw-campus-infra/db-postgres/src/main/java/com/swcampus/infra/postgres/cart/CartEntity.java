package com.swcampus.infra.postgres.cart;

import com.swcampus.domain.cart.Cart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lectureId;

    public CartEntity(Long userId, Long lectureId) {
        this.userId = userId;
        this.lectureId = lectureId;
    }

    public static CartEntity from(Cart cart) {
        return new CartEntity(cart.getUserId(), cart.getLectureId());
    }

    public Cart toDomain() {
        return Cart.builder()
                .id(id)
                .userId(userId)
                .lectureId(lectureId)
                .build();
    }
}
