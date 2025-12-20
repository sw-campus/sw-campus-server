package com.swcampus.infra.postgres.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT m FROM MemberEntity m WHERE " +
           "(:keyword IS NULL OR :keyword = '') OR " +
           "m.name ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.nickname ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.email ILIKE CONCAT('%', :keyword, '%')")
    Page<MemberEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
