package com.swcampus.domain.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
     * OAuth 사용자 생성 - 랜덤 닉네임 자동 생성
     */
    public static Member createOAuthUser(String email, String name) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.nickname = generateRandomNickname();  // 랜덤 닉네임 생성
        member.password = null;  // OAuth 사용자는 비밀번호 없음
        member.phone = null;     // nullable
        member.location = null;  // nullable
        member.role = Role.USER;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        return member;
    }

    /**
     * 랜덤 닉네임 생성 (예: "사용자_a1b2c3d4")
     */
    private static String generateRandomNickname() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        return "사용자_" + randomSuffix;
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
