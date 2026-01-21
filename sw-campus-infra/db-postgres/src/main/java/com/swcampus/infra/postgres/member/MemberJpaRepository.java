package com.swcampus.infra.postgres.member;

import com.swcampus.domain.member.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByOrgId(Long orgId);
    Optional<MemberEntity> findByOrgId(Long orgId);
    Optional<MemberEntity> findFirstByRoleOrderByIdAsc(Role role);

    @Query("SELECT m FROM MemberEntity m WHERE " +
           "(:role IS NULL OR m.role = :role) AND (" +
           "(:keyword IS NULL OR :keyword = '') OR " +
           "m.name ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.nickname ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.email ILIKE CONCAT('%', :keyword, '%'))")
    Page<MemberEntity> searchByRoleAndKeyword(@Param("role") Role role, @Param("keyword") String keyword, Pageable pageable);

    boolean existsByNicknameIgnoreCase(String nickname);

    boolean existsByNicknameIgnoreCaseAndIdNot(String nickname, Long id);

    long countByRole(Role role);

    Optional<MemberEntity> findByEmailAndNameAndPhone(String email, String name, String phone);
}

