package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.RefreshToken;
import com.swcampus.domain.auth.RefreshTokenRepository;
import com.swcampus.infra.postgres.TestApplication;
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
@Import(RefreshTokenRepositoryImpl.class)
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("RefreshToken 저장 및 memberId로 조회")
    void saveAndFindByMemberId() {
        // given
        RefreshToken rt = RefreshToken.create(1L, "test-refresh-token", 604800);

        // when
        refreshTokenRepository.save(rt);
        Optional<RefreshToken> found = refreshTokenRepository.findByMemberId(1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(1L);
        assertThat(found.get().getToken()).isEqualTo("test-refresh-token");
    }

    @Test
    @DisplayName("token 값으로 조회")
    void findByToken() {
        // given
        RefreshToken rt = RefreshToken.create(2L, "unique-token-value", 604800);
        refreshTokenRepository.save(rt);

        // when
        Optional<RefreshToken> found = refreshTokenRepository.findByToken("unique-token-value");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 memberId로 조회")
    void findByMemberId_notFound() {
        // when
        Optional<RefreshToken> found = refreshTokenRepository.findByMemberId(999L);

        // then
        assertThat(found).isEmpty();
    }
}
