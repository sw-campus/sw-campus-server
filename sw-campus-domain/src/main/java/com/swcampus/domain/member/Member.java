package com.swcampus.domain.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private Role role;
    private Long orgId;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Member createUser(String email, String password,
                                    String name, String nickname,
                                    String phone, String location) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.nickname = nickname;
        member.phone = phone;
        member.location = location;
        member.role = Role.USER;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        return member;
    }

    public static Member createOrganization(String email, String password,
                                            String name, String nickname,
                                            String phone, String location) {
        Member member = createUser(email, password, name, nickname, phone, location);
        member.role = Role.ORGANIZATION;
        return member;
    }

    public static Member of(Long id, String email, String password,
                            String name, String nickname, String phone,
                            Role role, Long orgId,
                            String location, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        Member member = new Member();
        member.id = id;
        member.email = email;
        member.password = password;
        member.name = name;
        member.nickname = nickname;
        member.phone = phone;
        member.role = role;
        member.orgId = orgId;
        member.location = location;
        member.createdAt = createdAt;
        member.updatedAt = updatedAt;
        return member;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * OAuth 사용자 생성
     *
     * @param email    이메일
     * @param name     이름 (null인 경우 닉네임 사용)
     * @param nickname 닉네임 (외부에서 생성하여 전달)
     */
    public static Member createOAuthUser(String email, String name, String nickname) {
        Member member = new Member();
        member.email = email;
        member.nickname = nickname;
        member.name = (name != null && !name.isBlank()) ? name : nickname;
        member.password = null;  // OAuth 사용자는 비밀번호 없음
        member.phone = null;     // nullable
        member.location = null;  // nullable
        member.role = Role.USER;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        return member;
    }

    public void updateProfile(String nickname, String phone, String location) {
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (phone != null) this.phone = phone;
        if (location != null) this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSocialUser() {
        return this.password == null || this.password.isBlank();
    }
}
