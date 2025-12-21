package com.swcampus.domain.member;

import com.swcampus.domain.member.exception.DuplicateNicknameException;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    /**
     * 비밀번호 검증 (회원정보 수정 화면 진입 전 확인용)
     * @param memberId 사용자 ID
     * @param rawPassword 입력된 비밀번호 (평문)
     * @return 비밀번호 일치 여부
     */
    public boolean validatePassword(Long memberId, String rawPassword) {
        Member member = getMember(memberId);
        
        // OAuth 사용자(비밀번호 없음)는 비밀번호 검증 절차 생략 (true 반환)
        if (member.isSocialUser()) {
            return true;
        }
        
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    @Transactional
    public void updateProfile(Long memberId, String nickname, String phone, String address) {
        Member member = getMember(memberId);

        // 닉네임 변경 시 중복 검사 (자신 제외)
        if (nickname != null && !nickname.isBlank()
                && !nickname.equalsIgnoreCase(member.getNickname())) {
            if (!isNicknameAvailable(nickname, memberId)) {
                throw new DuplicateNicknameException(nickname);
            }
        }

        member.updateProfile(nickname, phone, address);
        memberRepository.save(member);
    }

    public boolean isNicknameAvailable(String nickname, Long excludeMemberId) {
        if (nickname == null || nickname.isBlank()) {
            return false;
        }

        if (excludeMemberId != null) {
            return !memberRepository.existsByNicknameIgnoreCaseAndIdNot(nickname, excludeMemberId);
        }
        return !memberRepository.existsByNicknameIgnoreCase(nickname);
    }
}
