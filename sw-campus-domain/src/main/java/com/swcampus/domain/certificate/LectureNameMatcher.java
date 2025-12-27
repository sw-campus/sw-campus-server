package com.swcampus.domain.certificate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 강의명 다단계 매칭 유틸리티
 * <p>
 * 0단계: OCR 유효성 검사
 * 1차: 정확한 매칭
 * 2차: 유사 문자 정규화 매칭
 * 3차: 유사도 매칭 (Jaro-Winkler >= 0.8)
 */
@Slf4j
@Component
public class LectureNameMatcher {

    @Value("${certificate.matching.similarity-threshold:0.8}")
    private double similarityThreshold;

    private static final Map<Character, Character> HOMOGLYPH_MAP = Map.of(
            '\u00D7', 'x',   // × → x (곱셈 기호 → 알파벳)
            '\u2014', '-',   // — → - (em dash → hyphen)
            '\u2013', '-',   // – → - (en dash → hyphen)
            '\u2018', '\'',  // ' → ' (왼쪽 작은따옴표)
            '\u2019', '\'',  // ' → ' (오른쪽 작은따옴표)
            '\u201C', '"',   // " → " (왼쪽 큰따옴표)
            '\u201D', '"'    // " → " (오른쪽 큰따옴표)
    );

    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    /**
     * 다단계 매칭 수행
     *
     * @param lectureName 강의명
     * @param ocrLines    OCR 결과 라인 리스트
     * @return 매칭 성공 시 true, 실패 시 false
     */
    public boolean match(String lectureName, List<String> ocrLines) {
        String ocrText = (ocrLines == null || ocrLines.isEmpty())
                ? ""
                : String.join("", ocrLines);

        // 0단계: OCR 유효성 검사
        if (!isValidOcrResult(ocrText, lectureName)) {
            log.info("[수료증 검증] 최종 결과: lectureName='{}', matchedStep={}", lectureName, 0);
            return false;
        }

        String normalizedOcrText = normalize(ocrText);
        String normalizedLectureName = normalize(lectureName);

        // 1차: 정확한 매칭
        if (exactMatch(normalizedOcrText, normalizedLectureName)) {
            log.info("[수료증 검증] 최종 결과: lectureName='{}', matchedStep={}", lectureName, 1);
            return true;
        }

        // 2차: 유사 문자 정규화 매칭
        if (normalizedMatch(ocrText, lectureName)) {
            log.info("[수료증 검증] 최종 결과: lectureName='{}', matchedStep={}", lectureName, 2);
            return true;
        }

        // 3차: 유사도 매칭
        if (similarityMatch(normalizedOcrText, normalizedLectureName)) {
            log.info("[수료증 검증] 최종 결과: lectureName='{}', matchedStep={}", lectureName, 3);
            return true;
        }

        log.info("[수료증 검증] 최종 결과: lectureName='{}', matchedStep={}", lectureName, -1);
        return false;
    }

    /**
     * 0단계: OCR 결과 유효성 검사
     */
    boolean isValidOcrResult(String ocrText, String lectureName) {
        if (ocrText == null || ocrText.isEmpty()) {
            log.info("[수료증 검증] 0단계 유효성: ocrLength={}, lectureNameLength={}, valid={}",
                    0, lectureName.length(), false);
            return false;
        }

        int ocrLength = ocrText.length();
        int lectureNameLength = lectureName.length();
        boolean valid = ocrLength >= lectureNameLength * 0.5;

        log.info("[수료증 검증] 0단계 유효성: ocrLength={}, lectureNameLength={}, valid={}",
                ocrLength, lectureNameLength, valid);

        return valid;
    }

    /**
     * 1차: 정확한 매칭 (공백 제거, 소문자 변환 후 비교)
     */
    boolean exactMatch(String normalizedOcrText, String normalizedLectureName) {
        boolean matched = normalizedOcrText.contains(normalizedLectureName);
        log.info("[수료증 검증] 1차 정확한 매칭: matched={}", matched);
        return matched;
    }

    /**
     * 2차: 유사 문자 정규화 후 매칭
     */
    boolean normalizedMatch(String ocrText, String lectureName) {
        String homoglyphNormalizedOcr = normalizeHomoglyphs(ocrText);
        String homoglyphNormalizedLecture = normalizeHomoglyphs(lectureName);

        String normalizedOcr = normalize(homoglyphNormalizedOcr);
        String normalizedLecture = normalize(homoglyphNormalizedLecture);

        boolean matched = normalizedOcr.contains(normalizedLecture);
        log.info("[수료증 검증] 2차 정규화 매칭: matched={}", matched);
        return matched;
    }

    /**
     * 3차: Jaro-Winkler 유사도 매칭
     */
    boolean similarityMatch(String normalizedOcrText, String normalizedLectureName) {
        double similarity = jaroWinkler.apply(normalizedOcrText, normalizedLectureName);
        boolean matched = similarity >= similarityThreshold;

        log.info("[수료증 검증] 3차 유사도 매칭: similarity={}, threshold={}, matched={}",
                String.format("%.2f", similarity), similarityThreshold, matched);

        return matched;
    }

    /**
     * 유사 문자(Homoglyph) 정규화
     */
    String normalizeHomoglyphs(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(HOMOGLYPH_MAP.getOrDefault(c, c));
        }
        return sb.toString();
    }

    /**
     * 공백 제거 및 소문자 변환
     */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", "").toLowerCase();
    }
}
