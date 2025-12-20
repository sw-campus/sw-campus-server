package com.swcampus.infra.postgres.member;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberEntityRepository implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    @Override
    public Member save(Member member) {
        MemberEntity entity = MemberEntity.from(member);
        MemberEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id).map(MemberEntity::toDomain);
    }

    @Override
    public List<Member> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(MemberEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(MemberEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByOrgId(Long orgId) {
        return jpaRepository.existsByOrgId(orgId);
    }

    @Override
    public Optional<Member> findByOrgId(Long orgId) {
        return jpaRepository.findByOrgId(orgId).map(MemberEntity::toDomain);
    }

    @Override
    public Optional<Member> findFirstByRole(Role role) {
        return jpaRepository.findFirstByRoleOrderByIdAsc(role).map(MemberEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
