package com.swcampus.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 도메인 테스트")
class MemberTest {

    @Test
    @DisplayName("일반 회원을 생성할 수 있다")
    void createUser() {
        // given
        String email = "user@example.com";
        String password = "encodedPassword";
        String name = "홍길동";
        String nickname = "길동이";
        String phone = "010-1234-5678";
        String location = "서울시 강남구";

        // when
        Member member = Member.createUser(email, password, name, nickname, phone, location);

        // then
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPassword()).isEqualTo(password);
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getNickname()).isEqualTo(nickname);
        assertThat(member.getPhone()).isEqualTo(phone);
        assertThat(member.getLocation()).isEqualTo(location);
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getOrgId()).isNull();
        assertThat(member.getCreatedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("교육기관을 생성할 수 있다")
    void createOrganization() {
        // given & when
        Member member = Member.createOrganization(
            "org@example.com", "encodedPassword",
            "김교육", "교육기관", "010-9876-5432", "서울시 서초구"
        );

        // then
        assertThat(member.getRole()).isEqualTo(Role.ORGANIZATION);
        assertThat(member.getOrgId()).isNull(); // orgId는 Organization 생성 후 설정됨
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword() {
        // given
        Member member = Member.createUser(
            "user@example.com", "oldPassword",
            "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
        );
        String newPassword = "newEncodedPassword";

        // when
        member.changePassword(newPassword);

        // then
        assertThat(member.getPassword()).isEqualTo(newPassword);
    }
}
