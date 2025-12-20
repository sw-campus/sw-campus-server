package com.swcampus.infra.postgres.member;

import com.swcampus.domain.member.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByOrgId(Long orgId);
    Optional<MemberEntity> findByOrgId(Long orgId);
    Optional<MemberEntity> findFirstByRoleOrderByIdAsc(Role role);
}
