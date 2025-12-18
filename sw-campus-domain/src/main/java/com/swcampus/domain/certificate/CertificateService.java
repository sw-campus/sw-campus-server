package com.swcampus.domain.certificate;

import com.swcampus.domain.certificate.exception.CertificateAlreadyExistsException;
import com.swcampus.domain.certificate.exception.CertificateLectureMismatchException;
import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.ocr.OcrClient;
import com.swcampus.domain.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final LectureRepository lectureRepository;
    private final FileStorageService fileStorageService;
    private final OcrClient ocrClient;

    /**
     * 수료증 인증 여부 확인
     */
    public Optional<Certificate> checkCertificate(Long memberId, Long lectureId) {
        return certificateRepository.findByMemberIdAndLectureId(memberId, lectureId);
    }

    /**
     * 수료증 인증 처리
     */
    @Transactional
    public Certificate verifyCertificate(Long memberId, Long lectureId,
            byte[] imageBytes, String fileName,
            String contentType) {
        // 1. 이미 인증했는지 확인
        if (certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId)) {
            throw new CertificateAlreadyExistsException(memberId, lectureId);
        }

        // 2. 강의 정보 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));

        // 3. OCR로 텍스트 추출
        List<String> extractedLines = ocrClient.extractText(imageBytes, fileName);

        // 4. 강의명 매칭 검증
        boolean isValid = validateLectureName(lecture.getLectureName(), extractedLines);
        if (!isValid) {
            throw new CertificateLectureMismatchException();
        }

        // 5. S3에 이미지 업로드
        String imageUrl = fileStorageService.upload(imageBytes, "certificates", fileName, contentType);

        // 6. 수료증 저장 (OCR 검증 성공 시 status = "SUCCESS")
        Certificate certificate = Certificate.create(memberId, lectureId, imageUrl, "SUCCESS");
        return certificateRepository.save(certificate);
    }

    /**
     * 강의명 유연한 매칭
     * - 공백 제거
     * - 소문자 변환
     * - 부분 일치 확인
     */
    private boolean validateLectureName(String lectureName, List<String> ocrLines) {
        if (ocrLines == null || ocrLines.isEmpty()) {
            return false;
        }

        String normalizedLectureName = normalize(lectureName);
        String ocrText = String.join("", ocrLines);
        String normalizedOcrText = normalize(ocrText);

        return normalizedOcrText.contains(normalizedLectureName);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", "").toLowerCase();
    }

    /**
     * 회원의 승인된 수료증 목록 조회
     */
    public List<Certificate> findAllByMemberId(Long memberId) {
        return certificateRepository.findAllByMemberId(memberId);
    }
}
