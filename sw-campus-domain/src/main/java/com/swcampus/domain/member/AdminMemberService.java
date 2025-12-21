package com.swcampus.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public Page<Member> searchMembers(String keyword, Pageable pageable) {
        return memberRepository.searchByKeyword(keyword, pageable);
    }
}
