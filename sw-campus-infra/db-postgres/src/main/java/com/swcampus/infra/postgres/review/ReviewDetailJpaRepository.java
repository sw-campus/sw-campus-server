package com.swcampus.infra.postgres.review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewDetailJpaRepository extends JpaRepository<ReviewDetailEntity, Long> {
}
