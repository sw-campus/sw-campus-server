package com.swcampus.api.cart;

import com.swcampus.api.cart.response.CartLectureResponse;
import com.swcampus.domain.cart.CartService;
import com.swcampus.domain.lecture.Lecture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestParam Long lectureId) {
        Long currentUserId = getCurrentUserId();
        cartService.addCart(currentUserId, lectureId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeCart(@RequestParam Long lectureId) {
        Long currentUserId = getCurrentUserId();
        cartService.removeCart(currentUserId, lectureId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CartLectureResponse>> getCart() {
        Long currentUserId = getCurrentUserId();
        List<Lecture> lectures = cartService.getCartList(currentUserId);
        List<CartLectureResponse> response = lectures.stream()
                .map(CartLectureResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
}
