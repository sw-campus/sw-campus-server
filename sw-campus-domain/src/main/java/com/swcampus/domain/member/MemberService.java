package com.swcampus.domain.member;

import com.swcampus.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void updateProfile(Long memberId, String nickname, String phone, String address) {
        Member member = getMember(memberId);
        member.updateProfile(nickname, phone, address);
        memberRepository.save(member);
    }
}
