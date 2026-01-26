package com.swcampus.domain.member;

import com.swcampus.domain.auth.RefreshTokenRepository;
import com.swcampus.domain.bookmark.BookmarkRepository;
import com.swcampus.domain.cart.CartRepository;
import com.swcampus.domain.member.exception.DuplicateNicknameException;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.SocialAccount;
import com.swcampus.domain.oauth.SocialAccountRepository;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.survey.MemberSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationRepository organizationRepository;
    private final CartRepository cartRepository;
    private final MemberSurveyRepository memberSurveyRepository;
    private final BookmarkRepository bookmarkRepository;

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public List<Member> getMembersByIds(List<Long> memberIds) {
        return memberRepository.findAllByIds(memberIds);
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

    /**
     * 회원 탈퇴 처리
     * 게시글/댓글의 작성자는 NULL로 설정하고, 나머지 관련 데이터는 삭제합니다.
     * 기관 회원의 경우 기관은 비활성화 처리합니다.
     *
     * @param memberId 탈퇴할 회원 ID
     * @return OAuth 프로바이더 목록 (연결 해제 안내용)
     */
    @Transactional
    public List<OAuthProvider> withdraw(Long memberId) {
        Member member = getMember(memberId);

        // 관리자는 탈퇴 불가
        if (member.getRole() == Role.ADMIN) {
            throw new IllegalStateException("관리자는 탈퇴할 수 없습니다.");
        }

        // 기관 회원인 경우 기관 비활성화 처리
        if (member.getRole() == Role.ORGANIZATION) {
            organizationRepository.findByUserId(memberId)
                    .ifPresent(org -> {
                        org.deactivate();
                        organizationRepository.save(org);
                    });
        }

        // OAuth 프로바이더 정보 먼저 조회 (삭제 전)
        List<OAuthProvider> providers = socialAccountRepository.findByMemberId(memberId).stream()
                .map(SocialAccount::getProvider)
                .toList();

        // 관련 데이터 삭제 (게시글/댓글/후기/수료증/좋아요는 FK ON DELETE SET NULL로 자동 처리)
        refreshTokenRepository.deleteByMemberId(memberId);
        socialAccountRepository.deleteByMemberId(memberId);
        cartRepository.deleteByUserId(memberId);
        memberSurveyRepository.deleteByMemberId(memberId);
        bookmarkRepository.deleteByUserId(memberId);

        // 마지막으로 회원 삭제
        memberRepository.deleteById(memberId);

        return providers;
    }
}
