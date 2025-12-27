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
    private final LectureNameMatcher lectureNameMatcher;

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

        // 4. 강의명 다단계 매칭 검증
        validateLectureName(lecture.getLectureName(), extractedLines);

        // 5. S3 Private Bucket에 이미지 업로드
        String imageKey = fileStorageService.uploadPrivate(imageBytes, "certificates", fileName, contentType);

        // 6. 수료증 저장 (OCR 검증 성공 시 status = "SUCCESS")
        Certificate certificate = Certificate.create(memberId, lectureId, imageKey, "SUCCESS");
        return certificateRepository.save(certificate);
    }

    /**
     * 강의명 다단계 매칭 검증
     * - 0단계: OCR 유효성 검사
     * - 1차: 정확한 매칭
     * - 2차: 유사 문자 정규화 매칭
     * - 3차: Jaro-Winkler 유사도 매칭 (>= 0.8)
     */
    private void validateLectureName(String lectureName, List<String> ocrLines) {
        // 다단계 매칭 시도 (0단계~3차)
        if (!lectureNameMatcher.match(lectureName, ocrLines)) {
            throw new CertificateLectureMismatchException();
        }
    }

    /**
     * 회원의 승인된 수료증 목록 조회
     */
    public List<Certificate> findAllByMemberId(Long memberId) {
        return certificateRepository.findAllByMemberId(memberId);
    }
}
