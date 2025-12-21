package com.swcampus.domain.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    List<Member> findAllByIds(List<Long> ids);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Member> searchByKeyword(String keyword, Pageable pageable);
}
