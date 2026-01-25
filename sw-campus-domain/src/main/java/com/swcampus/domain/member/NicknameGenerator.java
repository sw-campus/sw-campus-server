package com.swcampus.domain.member;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * OAuth 사용자를 위한 3단어 조합 닉네임 생성기
 * 형용사 + 명사 + 명사 = "행복한고양이구름"
 */
public final class NicknameGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private NicknameGenerator() {
    }

    /**
     * 3단어 조합 닉네임 생성
     * 형용사 + 명사1 + 명사2
     *
     * @return 생성된 닉네임 (예: "행복한고양이구름")
     */
    public static String generate() {
        String adjective = randomElement(NicknameWords.ADJECTIVES);
        String noun1 = randomElement(NicknameWords.NOUNS);
        String noun2 = randomElement(NicknameWords.NOUNS);
        return adjective + noun1 + noun2;
    }

    /**
     * 폴백 닉네임 생성 (중복 재시도 실패 시 사용)
     *
     * @return UUID 기반 닉네임 (예: "사용자_a1b2c3d4")
     */
    public static String generateFallback() {
        return "사용자_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String randomElement(List<String> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
