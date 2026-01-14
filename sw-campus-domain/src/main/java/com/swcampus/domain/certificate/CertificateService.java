package com.swcampus.domain.certificate;

import com.swcampus.domain.certificate.exception.CertificateAlreadyExistsException;
import com.swcampus.domain.certificate.exception.CertificateLectureMismatchException;
import com.swcampus.domain.certificate.exception.CertificateNotEditableException;
import com.swcampus.domain.certificate.exception.CertificateNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.ocr.OcrClient;
import com.swcampus.domain.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CertificateService {

    @Value("${certificate.ocr.enabled:false}")
    private boolean ocrEnabled;

    private final CertificateRepository certificateRepository;
    private final LectureRepository lectureRepository;
    private final FileStorageService fileStorageService;
    private final OcrClient ocrClient;  // nullable - OCR 비활성화 시 null
    private final LectureNameMatcher lectureNameMatcher;

    public CertificateService(
            CertificateRepository certificateRepository,
            LectureRepository lectureRepository,
            FileStorageService fileStorageService,
            @Autowired(required = false) OcrClient ocrClient,
            LectureNameMatcher lectureNameMatcher) {
        this.certificateRepository = certificateRepository;
        this.lectureRepository = lectureRepository;
        this.fileStorageService = fileStorageService;
        this.ocrClient = ocrClient;
        this.lectureNameMatcher = lectureNameMatcher;
    }

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
            InputStream imageStream, long contentLength, String fileName,
            String contentType) {
        // 1. 이미 인증했는지 확인
        if (certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId)) {
            throw new CertificateAlreadyExistsException(memberId, lectureId);
        }

        // 2. 강의 정보 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));

        // 3-4. OCR 검증 (현재 비활성화 상태)
        // TODO(#384): OCR 활성화 시 InputStream 기반으로 변경 필요
        if (ocrEnabled && ocrClient != null) {
            throw new UnsupportedOperationException(
                    "OCR 검증은 현재 InputStream 기반 처리와 호환되지 않습니다. " +
                    "OCR 활성화 전 OcrClient를 InputStream 기반으로 변경해야 합니다. (Issue #384)");
        }
        log.info("OCR 검증 비활성화 상태 - 수료증 이미지만 저장합니다. memberId={}, lectureId={}", memberId, lectureId);

        // 5. S3 Private Bucket에 이미지 업로드 (InputStream 기반)
        String imageKey = fileStorageService.uploadPrivate(imageStream, contentLength, "certificates", fileName, contentType);

        // 6. 수료증 저장
        Certificate certificate = Certificate.create(memberId, lectureId, imageKey);
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
     * 회원의 모든 수료증 목록 조회 (상태와 무관하게 PENDING/APPROVED/REJECTED 모두 포함)
     */
    public List<Certificate> findAllByMemberId(Long memberId) {
        return certificateRepository.findAllByMemberId(memberId);
    }

    /**
     * 수료증 이미지 수정
     * - 소유권 검증: 본인 수료증만 수정 가능
     * - 상태 검증: PENDING/REJECTED만 수정 가능, APPROVED는 수정 불가
     * - 기존 이미지 S3에서 삭제 후 새 이미지 업로드
     * - 상태 PENDING으로 초기화 (재검증 필요)
     */
    @Transactional
    public Certificate updateCertificateImage(Long memberId, Long certificateId,
            InputStream imageStream, long contentLength, String fileName, String contentType) {
        // 1. 수료증 조회
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        // 2. 소유권 검증
        if (!certificate.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("수료증 수정 권한이 없습니다.");
        }

        // 3. 상태 검증 (APPROVED는 수정 불가)
        if (!certificate.canEdit()) {
            throw new CertificateNotEditableException(certificateId);
        }

        // 4. 기존 이미지 S3에서 삭제
        String oldImageKey = certificate.getImageKey();
        if (oldImageKey != null && !oldImageKey.isEmpty()) {
            try {
                fileStorageService.deletePrivate(oldImageKey);
            } catch (RuntimeException e) {
                log.warn("기존 수료증 이미지 삭제 실패: {}", oldImageKey, e);
            }
        }

        // 5. 새 이미지 S3에 업로드 (InputStream 기반)
        String newImageKey = fileStorageService.uploadPrivate(imageStream, contentLength, "certificates", fileName, contentType);

        // 6. imageKey 업데이트 + 상태 PENDING으로 초기화
        certificate.updateImageKey(newImageKey);

        // 7. 저장 및 반환
        return certificateRepository.save(certificate);
    }
}
