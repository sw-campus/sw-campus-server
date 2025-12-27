package com.swcampus.domain.certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LectureNameMatcherTest {

    private LectureNameMatcher lectureNameMatcher;

    @BeforeEach
    void setUp() {
        lectureNameMatcher = new LectureNameMatcher();
    }

    @Nested
    @DisplayName("0단계: OCR 유효성 검사")
    class Stage0ValidationTest {

        @Test
        @DisplayName("OCR 결과가 빈 리스트면 false 반환")
        void match_emptyOcrLines_returnsFalse() {
            // given
            String lectureName = "[구름] 자바 스프링";
            List<String> ocrLines = List.of();

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("OCR 결과가 null이면 false 반환")
        void match_nullOcrLines_returnsFalse() {
            // given
            String lectureName = "[구름] 자바 스프링";

            // when
            boolean result = lectureNameMatcher.match(lectureName, null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("OCR 텍스트 길이가 강의명의 50% 미만이면 false 반환")
        void match_ocrLengthTooShort_returnsFalse() {
            // given
            String lectureName = "[구름] 자바 스프링 풀스택 개발자 과정";
            List<String> ocrLines = List.of("짧은");  // 강의명 길이의 50% 미만

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("1차: 정확한 매칭")
    class Stage1ExactMatchTest {

        @Test
        @DisplayName("강의명이 OCR 텍스트에 정확히 포함되면 true 반환")
        void match_exactMatch_returnsTrue() {
            // given
            String lectureName = "[구름] 자바 스프링";
            List<String> ocrLines = List.of("수료증", "[구름] 자바 스프링", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("공백이 다르더라도 정확히 매칭되면 true 반환")
        void match_differentSpacing_returnsTrue() {
            // given
            String lectureName = "[구름] 자바 스프링";
            List<String> ocrLines = List.of("수료증", "[구름]자바스프링", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("대소문자가 다르더라도 정확히 매칭되면 true 반환")
        void match_caseInsensitive_returnsTrue() {
            // given
            String lectureName = "Java Spring";
            List<String> ocrLines = List.of("수료증", "JAVA SPRING 과정", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("2차: 유사 문자 정규화 매칭")
    class Stage2NormalizedMatchTest {

        @Test
        @DisplayName("곱셈 기호(×)가 알파벳 x와 매칭되면 true 반환")
        void match_multiplicationSign_returnsTrue() {
            // given
            String lectureName = "[구름 x 인프런] 자바 스프링";
            List<String> ocrLines = List.of("수료증", "[구름 × 인프런] 자바 스프링", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("em dash(—)가 hyphen(-)과 매칭되면 true 반환")
        void match_emDash_returnsTrue() {
            // given
            String lectureName = "자바 스프링 - 기초";
            List<String> ocrLines = List.of("수료증", "자바 스프링 — 기초", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("en dash(–)가 hyphen(-)과 매칭되면 true 반환")
        void match_enDash_returnsTrue() {
            // given
            String lectureName = "자바 스프링 - 기초";
            List<String> ocrLines = List.of("수료증", "자바 스프링 – 기초", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("스마트 따옴표가 일반 따옴표와 매칭되면 true 반환")
        void match_smartQuotes_returnsTrue() {
            // given
            String lectureName = "\"자바\" 스프링";
            List<String> ocrLines = List.of("수료증", "\u201C자바\u201D 스프링", "홍길동");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("3차: 유사도 매칭 (Jaro-Winkler)")
    class Stage3SimilarityMatchTest {

        @Test
        @DisplayName("유사도 80% 이상이면 true 반환")
        void match_similarityAboveThreshold_returnsTrue() {
            // given
            String lectureName = "자바 스프링 기초";
            // OCR에서 일부 글자가 다르게 인식된 경우
            List<String> ocrLines = List.of("자바 스프링 기쵸");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("유사도 80% 미만이면 false 반환")
        void match_similarityBelowThreshold_returnsFalse() {
            // given
            String lectureName = "자바 스프링 풀스택 개발자 과정";
            List<String> ocrLines = List.of("파이썬 백엔드 개발자 과정");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("최종 실패 케이스")
    class FinalFailureTest {

        @Test
        @DisplayName("전혀 다른 강의명이면 false 반환")
        void match_completelyDifferent_returnsFalse() {
            // given
            String lectureName = "자바 스프링";
            List<String> ocrLines = List.of("파이썬", "머신러닝", "과정");

            // when
            boolean result = lectureNameMatcher.match(lectureName, ocrLines);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("유사 문자 정규화")
    class NormalizeHomoglyphsTest {

        @Test
        @DisplayName("모든 유사 문자가 정규화됨")
        void normalizeHomoglyphs_allHomoglyphs_normalized() {
            // given
            String text = "\u00D7 \u2014 \u2013 \u2018 \u2019 \u201C \u201D"; // 모든 유사 문자 포함

            // when
            String result = lectureNameMatcher.normalizeHomoglyphs(text);

            // then
            assertThat(result).isEqualTo("x - - ' ' \" \"");
        }

        @Test
        @DisplayName("null 입력 시 빈 문자열 반환")
        void normalizeHomoglyphs_null_returnsEmptyString() {
            // when
            String result = lectureNameMatcher.normalizeHomoglyphs(null);

            // then
            assertThat(result).isEmpty();
        }
    }
}
