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
           "(:keyword IS NULL OR :keyword = '') OR " +
           "m.name ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.nickname ILIKE CONCAT('%', :keyword, '%') OR " +
           "m.email ILIKE CONCAT('%', :keyword, '%')")
    Page<MemberEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberEntity m WHERE LOWER(m.nickname) = LOWER(:nickname)")
    boolean existsByNicknameIgnoreCase(@Param("nickname") String nickname);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberEntity m WHERE LOWER(m.nickname) = LOWER(:nickname) AND m.id <> :excludeId")
    boolean existsByNicknameIgnoreCaseAndIdNot(@Param("nickname") String nickname, @Param("excludeId") Long excludeId);
}
