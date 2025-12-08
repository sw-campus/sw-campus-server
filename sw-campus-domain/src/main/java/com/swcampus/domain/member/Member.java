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
}
