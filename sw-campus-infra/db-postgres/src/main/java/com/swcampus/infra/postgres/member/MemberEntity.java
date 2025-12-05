package com.swcampus.infra.postgres.member;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.Role;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "org_auth")
    private Integer orgAuth;

    @Column(name = "org_id")
    private Long orgId;

    private String location;

    public static MemberEntity from(Member member) {
        MemberEntity entity = new MemberEntity();
        entity.id = member.getId();
        entity.email = member.getEmail();
        entity.password = member.getPassword();
        entity.name = member.getName();
        entity.nickname = member.getNickname();
        entity.phone = member.getPhone();
        entity.role = member.getRole();
        entity.orgAuth = member.getOrgAuth();
        entity.orgId = member.getOrgId();
        entity.location = member.getLocation();
        return entity;
    }

    public Member toDomain() {
        return Member.of(
            this.id,
            this.email,
            this.password,
            this.name,
            this.nickname,
            this.phone,
            this.role,
            this.orgAuth,
            this.orgId,
            this.location,
            this.getCreatedAt(),
            this.getUpdatedAt()
        );
    }
}
