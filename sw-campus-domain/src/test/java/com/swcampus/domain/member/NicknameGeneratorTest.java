package com.swcampus.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NicknameGenerator 테스트")
class NicknameGeneratorTest {

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("3단어 조합 닉네임을 생성한다")
        void generateThreeWordCombination() {
            // when
            String nickname = NicknameGenerator.generate();

            // then
            assertThat(nickname).isNotNull();
            assertThat(nickname).isNotBlank();
        }

        @Test
        @DisplayName("닉네임은 형용사 + 명사 + 명사 형식이다")
        void nicknameContainsAdjectiveAndTwoNouns() {
            // when
            String nickname = NicknameGenerator.generate();

            // then
            boolean containsAdjective = NicknameWords.ADJECTIVES.stream()
                    .anyMatch(nickname::startsWith);
            assertThat(containsAdjective)
                    .as("닉네임은 형용사로 시작해야 한다: %s", nickname)
                    .isTrue();

            // 명사 포함 여부 검증 (최소 1개 이상)
            long nounCount = NicknameWords.NOUNS.stream()
                    .filter(nickname::contains)
                    .count();
            assertThat(nounCount)
                    .as("닉네임은 최소 1개 이상의 명사를 포함해야 한다: %s", nickname)
                    .isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("닉네임 길이는 6자 이상 10자 이하이다")
        void nicknameLengthIsWithinRange() {
            // when
            String nickname = NicknameGenerator.generate();

            // then
            // 형용사(2-4자) + 명사(2-3자) + 명사(2-3자) = 6-10자
            assertThat(nickname.length())
                    .as("닉네임 길이가 6-10자 범위를 벗어났습니다: %s (%d자)", nickname, nickname.length())
                    .isBetween(6, 10);
        }

        @RepeatedTest(100)
        @DisplayName("100회 생성 시 다양한 닉네임이 생성된다")
        void generatesDiverseNicknames() {
            // given
            Set<String> nicknames = new HashSet<>();

            // when
            for (int i = 0; i < 100; i++) {
                nicknames.add(NicknameGenerator.generate());
            }

            // then
            assertThat(nicknames.size())
                    .as("100회 생성 시 최소 50개 이상의 서로 다른 닉네임이 생성되어야 한다")
                    .isGreaterThanOrEqualTo(50);
        }
    }

    @Nested
    @DisplayName("generateFallback")
    class GenerateFallback {

        @Test
        @DisplayName("폴백 닉네임은 '사용자_' 접두사로 시작한다")
        void fallbackStartsWithPrefix() {
            // when
            String nickname = NicknameGenerator.generateFallback();

            // then
            assertThat(nickname).startsWith("사용자_");
        }

        @Test
        @DisplayName("폴백 닉네임은 12자이다 (사용자_ + 8자 UUID)")
        void fallbackHasCorrectLength() {
            // when
            String nickname = NicknameGenerator.generateFallback();

            // then
            assertThat(nickname.length()).isEqualTo(12);
        }

        @Test
        @DisplayName("폴백 닉네임의 UUID 부분은 8자리 16진수이다")
        void fallbackUuidPartIsValid() {
            // when
            String nickname = NicknameGenerator.generateFallback();
            String uuidPart = nickname.substring(4); // "사용자_" 제외

            // then
            assertThat(uuidPart).matches("[a-f0-9]{8}");
        }

        @RepeatedTest(10)
        @DisplayName("폴백 닉네임은 매번 다른 값을 생성한다")
        void fallbackGeneratesUniqueValues() {
            // given
            Set<String> nicknames = new HashSet<>();

            // when
            for (int i = 0; i < 10; i++) {
                nicknames.add(NicknameGenerator.generateFallback());
            }

            // then
            assertThat(nicknames.size()).isEqualTo(10);
        }
    }
}
