package com.swcampus.infra.postgres.member;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.infra.postgres.TestApplication;
import com.swcampus.infra.postgres.TestJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestApplication.class)
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
@DisplayName("MemberRepository 테스트")
class MemberRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository = new MemberEntityRepository(memberJpaRepository);
    }

    @Test
    @DisplayName("회원을 저장할 수 있다")
    void save() {
        // given
        Member member = Member.createUser(
            "user@example.com", "encodedPassword",
            "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
        );

        // when
        Member saved = memberRepository.save(member);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("ID로 회원을 조회할 수 있다")
    void findById() {
        // given
        Member member = Member.createUser(
            "user@example.com", "encodedPassword",
            "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
        );
        Member saved = memberRepository.save(member);

        // when
        Optional<Member> found = memberRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void findByEmail() {
        // given
        Member member = Member.createUser(
            "user@example.com", "encodedPassword",
            "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
        );
        memberRepository.save(member);

        // when
        Optional<Member> found = memberRepository.findByEmail("user@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 결과를 반환한다")
    void findByEmail_notFound() {
        // when
        Optional<Member> found = memberRepository.findByEmail("unknown@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        Member member = Member.createUser(
            "user@example.com", "encodedPassword",
            "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
        );
        memberRepository.save(member);

        // when & then
        assertThat(memberRepository.existsByEmail("user@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("unknown@example.com")).isFalse();
    }
}
