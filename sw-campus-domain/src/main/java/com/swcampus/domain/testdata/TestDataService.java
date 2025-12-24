package com.swcampus.domain.testdata;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.lecture.*;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.domain.review.ReviewRepository;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TestDataService {

    private static final String TEST_PASSWORD = "admin123";
    private static final DateTimeFormatter BATCH_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final TestDataRepository testDataRepository;
    private final OrganizationRepository organizationRepository;
    private final LectureRepository lectureRepository;
    private final MemberRepository memberRepository;
    private final CertificateRepository certificateRepository;
    private final ReviewRepository reviewRepository;
    private final MemberSurveyRepository memberSurveyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TestDataCreateResult createTestData() {
        if (testDataRepository.exists()) {
            throw new IllegalStateException("테스트 데이터가 이미 존재합니다. 먼저 삭제 후 다시 생성하세요.");
        }

        String batchId = "batch_" + LocalDateTime.now().format(BATCH_ID_FORMAT);
        String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);

        // 1. 기관담당자 Member 생성 (2명)
        List<Long> orgMemberIds = createOrganizationMembers(batchId, encodedPassword);

        // 2. Organization 생성 (2개) - 기관담당자와 연결
        List<Long> organizationIds = createOrganizations(batchId, orgMemberIds);

        // 3. 기관담당자의 orgId 업데이트
        updateMembersWithOrgId(orgMemberIds, organizationIds);

        // 4. Lecture 생성 (기관당 2개 = 4개)
        List<Long> lectureIds = createLectures(batchId, organizationIds);

        // 5. 일반회원 Member 생성 (15명)
        List<Long> userMemberIds = createUserMembers(batchId, encodedPassword);

        // 6. Certificate 생성 (일반회원 15명 × 강의 4개 = 60건)
        List<Long> certificateIds = createCertificates(batchId, userMemberIds, lectureIds);

        // 7. Review 생성 (일반회원 15명 × 강의 4개 = 60건)
        List<Long> reviewIds = createReviews(batchId, userMemberIds, lectureIds, certificateIds);

        // 8. Survey 생성 (일반회원 중 처음 10명)
        List<Long> surveyMemberIds = createSurveys(batchId, userMemberIds.subList(0, 10));

        // Member IDs 병합 (기관담당자 2명 + 일반회원 15명)
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.addAll(orgMemberIds);
        allMemberIds.addAll(userMemberIds);

        return TestDataCreateResult.builder()
                .batchId(batchId)
                .organizationIds(organizationIds)
                .lectureIds(lectureIds)
                .memberIds(allMemberIds)
                .certificateIds(certificateIds)
                .reviewIds(reviewIds)
                .surveyMemberIds(surveyMemberIds)
                .build();
    }

    private List<Long> createOrganizationMembers(String batchId, String encodedPassword) {
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Member member = Member.of(
                    null,
                    "test_org_" + i + "@test.com",
                    encodedPassword,
                    "테스트기관" + i + "담당자",
                    "테스트기관담당자" + i,
                    "010-0000-000" + i,
                    Role.ORGANIZATION,
                    null,  // orgId - 나중에 업데이트
                    "서울",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            Member saved = memberRepository.save(member);
            ids.add(saved.getId());
            registerTestData(batchId, "members", saved.getId());
        }
        return ids;
    }

    private List<Long> createOrganizations(String batchId, List<Long> memberIds) {
        List<Long> ids = new ArrayList<>();
        String[] orgNames = {"한국소프트웨어기술진흥협회 : 종로", "한국소프트웨어기술진흥협회 : 가산"};
        String[] descriptions = {
                "한국소프트웨어기술진흥협회 종로센터입니다. 소프트웨어 개발 교육을 전문으로 합니다.",
                "한국소프트웨어기술진흥협회 가산센터입니다. AI/ML 교육을 전문으로 합니다."
        };

        // 시설 이미지 URL (공개 S3 버킷)
        String bucketUrl = "https://sw-campus-public-prod-afe42bff.s3.amazonaws.com";
        String[][] facilityImages = {
                {
                        bucketUrl + "/organizations/2024/12/24/a-test-1.jpg",
                        bucketUrl + "/organizations/2024/12/24/a-test-2.jpg",
                        bucketUrl + "/organizations/2024/12/24/a-test-3.jpg",
                        bucketUrl + "/organizations/2024/12/24/a-test-4.jpg"
                },
                {
                        bucketUrl + "/organizations/2024/12/24/b-test-1.jpg",
                        bucketUrl + "/organizations/2024/12/24/b-test-2.jpg",
                        bucketUrl + "/organizations/2024/12/24/b-test-3.jpg",
                        bucketUrl + "/organizations/2024/12/24/b-test-4.jpg"
                }
        };

        // 로고 URL (S3)
        String[] logoUrls = {
                bucketUrl + "/organizations/2024/12/24/a-logo.png",
                bucketUrl + "/organizations/2024/12/24/b-logo.png"
        };

        for (int i = 0; i < 2; i++) {
            Organization org = Organization.of(
                    null,
                    memberIds.get(i),
                    orgNames[i],
                    descriptions[i],
                    ApprovalStatus.APPROVED,
                    null,  // certificateKey
                    null,  // govAuth
                    facilityImages[i][0],  // facilityImageUrl
                    facilityImages[i][1],  // facilityImageUrl2
                    facilityImages[i][2],  // facilityImageUrl3
                    facilityImages[i][3],  // facilityImageUrl4
                    logoUrls[i],  // logoUrl
                    null,  // homepage
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            Organization saved = organizationRepository.save(org);
            ids.add(saved.getId());
            registerTestData(batchId, "organizations", saved.getId());
        }
        return ids;
    }

    private void updateMembersWithOrgId(List<Long> memberIds, List<Long> orgIds) {
        for (int i = 0; i < memberIds.size(); i++) {
            Member member = memberRepository.findById(memberIds.get(i))
                    .orElseThrow(() -> new IllegalStateException("Member not found"));
            member.setOrgId(orgIds.get(i));
            memberRepository.save(member);
        }
    }

    private List<Long> createLectures(String batchId, List<Long> organizationIds) {
        List<Long> ids = new ArrayList<>();
        // 각 기관에 백엔드 1개 + AI 1개 강의 (같은 카테고리끼리 비교 가능)
        String[][] lectureNames = {
                {"Java 백엔드 개발 부트캠프", "AI/ML 입문 과정"},      // 기관 A
                {"Spring Boot 마스터 과정", "데이터 사이언스 실무"}    // 기관 B
        };

        // 커리큘럼 ID 매핑 (기존 seed 데이터 기준) - 각 강의당 10개 커리큘럼
        // 백엔드(Category 4): curriculums 11-20
        // AI(Category 11): curriculums 61-70
        Long[][][] curriculumIds = {
                // 기관 A: 백엔드 + AI
                {
                        {11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L},  // 백엔드
                        {61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L}   // AI
                },
                // 기관 B: 백엔드 + AI
                {
                        {11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L},  // 백엔드
                        {61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L}   // AI
                }
        };

        // 기본 이미지 URL (prod 환경)
        String defaultImageBaseUrl = "https://sw-campus-public-prod-afe42bff.s3.amazonaws.com/defaults";
        String[] defaultImages = {
                defaultImageBaseUrl + "/web-development.png",  // 백엔드
                defaultImageBaseUrl + "/data-ai.png"           // AI
        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.plusDays(30);
        LocalDateTime endDate = now.plusDays(120);
        LocalDateTime deadline = now.plusDays(25);

        for (int orgIdx = 0; orgIdx < organizationIds.size(); orgIdx++) {
            Long orgId = organizationIds.get(orgIdx);
            for (int lectIdx = 0; lectIdx < 2; lectIdx++) {
                // LectureCurriculum 목록 생성 (10개 커리큘럼)
                List<LectureCurriculum> lectureCurriculums = new ArrayList<>();
                Long[] currIds = curriculumIds[orgIdx][lectIdx];
                for (int i = 0; i < currIds.length; i++) {
                    // 0-3: BASIC, 4-7: ADVANCED, 8-9: NONE
                    CurriculumLevel level = (i < 4) ? CurriculumLevel.BASIC :
                            (i < 8) ? CurriculumLevel.ADVANCED : CurriculumLevel.NONE;
                    lectureCurriculums.add(LectureCurriculum.builder()
                            .curriculumId(currIds[i])
                            .level(level)
                            .build());
                }

                Lecture lecture = Lecture.builder()
                        .orgId(orgId)
                        .lectureName(lectureNames[orgIdx][lectIdx])
                        .lectureImageUrl(defaultImages[lectIdx])
                        .days(Set.of(LectureDay.MONDAY, LectureDay.WEDNESDAY, LectureDay.FRIDAY))
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .lectureLoc(LectureLocation.OFFLINE)
                        .location("서울시 강남구 테헤란로 123")
                        .recruitType(RecruitType.CARD_REQUIRED)
                        .subsidy(BigDecimal.valueOf(300000))
                        .lectureFee(BigDecimal.valueOf(5000000))
                        .eduSubsidy(BigDecimal.valueOf(200000))
                        .goal("실무 개발자 양성")
                        .maxCapacity(30)
                        .equipPc(EquipmentType.PC)
                        .equipMerit("최신 개발 환경")
                        .books(true)
                        .resume(true)
                        .mockInterview(true)
                        .employmentHelp(true)
                        .afterCompletion(true)
                        .url("https://example.com/apply")
                        .status(LectureStatus.RECRUITING)
                        .lectureAuthStatus(LectureAuthStatus.APPROVED)
                        .startAt(startDate)
                        .endAt(endDate)
                        .deadline(deadline)
                        .totalDays(90)
                        .totalTimes(720)
                        .createdAt(now)
                        .updatedAt(now)
                        .lectureCurriculums(lectureCurriculums)
                        .build();

                Lecture saved = lectureRepository.save(lecture);
                ids.add(saved.getLectureId());
                registerTestData(batchId, "lectures", saved.getLectureId());
            }
        }
        return ids;
    }

    private List<Long> createUserMembers(String batchId, String encodedPassword) {
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Member member = Member.of(
                    null,
                    "test_user_" + i + "@test.com",
                    encodedPassword,
                    "테스트유저" + i,
                    "테스트닉네임" + i,
                    "010-1234-" + String.format("%04d", i),
                    Role.USER,
                    null,
                    "서울",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            Member saved = memberRepository.save(member);
            ids.add(saved.getId());
            registerTestData(batchId, "members", saved.getId());
        }
        return ids;
    }

    private List<Long> createCertificates(String batchId, List<Long> userMemberIds, List<Long> lectureIds) {
        List<Long> ids = new ArrayList<>();
        for (int memberIdx = 0; memberIdx < userMemberIds.size(); memberIdx++) {
            Long memberId = userMemberIds.get(memberIdx);
            // 회원 1-2 (인덱스 0,1): 수료증 PENDING
            // 회원 3-15 (인덱스 2-14): 수료증 APPROVED
            ApprovalStatus certStatus = (memberIdx < 2) ? ApprovalStatus.PENDING : ApprovalStatus.APPROVED;

            for (Long lectureId : lectureIds) {
                Certificate cert = Certificate.of(
                        null,
                        memberId,
                        lectureId,
                        "test-certificate-" + memberId + "-" + lectureId + ".png",
                        "수료",
                        certStatus,
                        LocalDateTime.now()
                );
                Certificate saved = certificateRepository.save(cert);
                ids.add(saved.getId());
                registerTestData(batchId, "certificates", saved.getId());
            }
        }
        return ids;
    }

    private List<Long> createReviews(String batchId, List<Long> userMemberIds, List<Long> lectureIds,
                                      List<Long> certificateIds) {
        List<Long> ids = new ArrayList<>();
        Random random = new Random(42);  // 고정 시드로 재현 가능한 데이터

        // 총평 코멘트 (20자 이상)
        String[] comments = {
                "정말 유익한 교육이었습니다. 강사님들이 친절하게 설명해주셔서 이해하기 쉬웠어요. 다른 분들께도 추천드립니다.",
                "실무에 바로 적용할 수 있는 내용이 많았습니다. 특히 프로젝트 실습이 큰 도움이 되었습니다.",
                "커리큘럼이 체계적이고 좋았습니다. 기초부터 심화까지 단계별로 잘 구성되어 있어서 따라가기 좋았어요.",
                "시설이 깔끔하고 좋았어요. 최신 장비와 쾌적한 환경에서 공부할 수 있어서 집중이 잘 되었습니다.",
                "프로젝트 경험이 많은 도움이 되었습니다. 팀원들과 협업하면서 실무 감각을 익힐 수 있었어요.",
                "취업 연계 프로그램이 잘 되어 있어서 수료 후 취업에 큰 도움이 되었습니다. 감사합니다.",
                "멘토링 시스템이 잘 갖춰져 있어서 어려운 부분도 빠르게 해결할 수 있었습니다. 강력 추천합니다.",
                "온라인 강의 품질이 좋고, 질의응답도 빠르게 해주셔서 비대면으로도 충분히 학습할 수 있었습니다."
        };

        // 카테고리별 상세 코멘트 (20자 이상)
        Map<ReviewCategory, String[]> detailComments = Map.of(
                ReviewCategory.TEACHER, new String[]{
                        "강사님이 실무 경험이 풍부하셔서 현장 이야기를 많이 들을 수 있었습니다.",
                        "질문에 친절하게 답변해주시고, 이해될 때까지 설명해주셔서 좋았습니다.",
                        "강의 진행이 매끄럽고 핵심을 잘 짚어주셔서 이해하기 쉬웠습니다."
                },
                ReviewCategory.CURRICULUM, new String[]{
                        "커리큘럼이 실무 중심으로 구성되어 있어서 바로 적용할 수 있었습니다.",
                        "기초부터 심화까지 체계적으로 잘 구성되어 있어서 따라가기 좋았습니다.",
                        "최신 기술 트렌드를 반영한 커리큘럼이라 만족스러웠습니다."
                },
                ReviewCategory.MANAGEMENT, new String[]{
                        "행정 처리가 빠르고 정확해서 수강에 불편함이 없었습니다.",
                        "출결 관리와 상담이 잘 이루어져서 학습에 집중할 수 있었습니다.",
                        "담당자분이 친절하시고 문의사항에 신속하게 응답해주셨습니다."
                },
                ReviewCategory.FACILITY, new String[]{
                        "시설이 깔끔하고 최신 장비가 구비되어 있어서 쾌적하게 학습했습니다.",
                        "휴게 공간과 편의시설이 잘 갖춰져 있어서 휴식하기 좋았습니다.",
                        "접근성이 좋고 주변에 식당과 카페가 많아서 편리했습니다."
                },
                ReviewCategory.PROJECT, new String[]{
                        "팀 프로젝트를 통해 협업 능력과 실무 경험을 쌓을 수 있었습니다.",
                        "실제 서비스를 개발하는 과정에서 많은 것을 배울 수 있었습니다.",
                        "프로젝트 발표와 피드백 시간이 있어서 발표 능력도 향상되었습니다."
                }
        );

        int certIndex = 0;
        for (int memberIdx = 0; memberIdx < userMemberIds.size(); memberIdx++) {
            Long memberId = userMemberIds.get(memberIdx);

            // 회원별 후기 상태 결정
            // 회원 1-2 (인덱스 0,1): 후기 PENDING
            // 회원 3 (인덱스 2): 후기 REJECTED
            // 회원 4-15 (인덱스 3-14): 후기 APPROVED
            ApprovalStatus reviewStatus;
            if (memberIdx < 2) {
                reviewStatus = ApprovalStatus.PENDING;
            } else if (memberIdx == 2) {
                reviewStatus = ApprovalStatus.REJECTED;
            } else {
                reviewStatus = ApprovalStatus.APPROVED;
            }

            for (int lectIdx = 0; lectIdx < lectureIds.size(); lectIdx++) {
                Long lectureId = lectureIds.get(lectIdx);
                Long certificateId = certificateIds.get(certIndex++);

                // ReviewDetail 생성 (5개 카테고리)
                List<ReviewDetail> details = new ArrayList<>();
                for (ReviewCategory category : ReviewCategory.values()) {
                    double score = 3.0 + random.nextDouble() * 2.0;  // 3.0 ~ 5.0
                    score = Math.round(score * 10) / 10.0;  // 소수점 1자리
                    String[] categoryComments = detailComments.get(category);
                    String detailComment = categoryComments[random.nextInt(categoryComments.length)];
                    details.add(ReviewDetail.create(
                            category,
                            score,
                            detailComment
                    ));
                }

                // 평균 점수도 소수점 1자리로 반올림
                double avgScore = details.stream().mapToDouble(ReviewDetail::getScore).average().orElse(0.0);
                avgScore = Math.round(avgScore * 10) / 10.0;

                Review review = Review.of(
                        null,
                        memberId,
                        lectureId,
                        certificateId,
                        comments[random.nextInt(comments.length)],
                        avgScore,
                        reviewStatus,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        details
                );

                Review saved = reviewRepository.save(review);
                ids.add(saved.getId());
                registerTestData(batchId, "reviews", saved.getId());

                // ReviewDetail은 Review와 함께 저장되므로 별도 registry 필요 없음
                // 하지만 삭제 순서를 위해 reviews_details도 등록
                for (ReviewDetail detail : saved.getDetails()) {
                    registerTestData(batchId, "reviews_details", detail.getId());
                }
            }
        }
        return ids;
    }

    private List<Long> createSurveys(String batchId, List<Long> userMemberIds) {
        List<Long> ids = new ArrayList<>();

        // 10명의 유저에게 각각 다른 설문조사 데이터 생성
        String[] majors = {
                "컴퓨터공학", "전자공학", "경영학", "수학", "물리학",
                "정보통신공학", "소프트웨어학", "데이터사이언스", "산업공학", "비전공"
        };

        String[] wantedJobsList = {
                "백엔드 개발자", "프론트엔드 개발자", "풀스택 개발자", "데이터 분석가", "AI/ML 엔지니어",
                "DevOps 엔지니어", "클라우드 엔지니어", "모바일 개발자", "QA 엔지니어", "보안 엔지니어"
        };

        String[] licensesList = {
                "정보처리기사", "SQLD, 정보처리기사", "컴활1급", "AWS SAA, 정보처리기사",
                "정보보안기사", "SQLD", "네트워크관리사, 정보처리기사", "빅데이터분석기사",
                "리눅스마스터1급", null  // 마지막 유저는 자격증 없음
        };

        BigDecimal[] amounts = {
                BigDecimal.valueOf(1000000), BigDecimal.valueOf(1500000), BigDecimal.valueOf(2000000),
                BigDecimal.valueOf(2500000), BigDecimal.valueOf(3000000), BigDecimal.valueOf(3500000),
                BigDecimal.valueOf(4000000), BigDecimal.valueOf(4500000), BigDecimal.valueOf(5000000),
                BigDecimal.valueOf(500000)
        };

        for (int i = 0; i < userMemberIds.size(); i++) {
            Long memberId = userMemberIds.get(i);

            MemberSurvey survey = MemberSurvey.create(
                    memberId,
                    majors[i],
                    i % 3 != 0,  // 1, 2, 4, 5, 7, 8번은 부트캠프 수료 O (3, 6, 9, 10번은 X)
                    wantedJobsList[i],
                    licensesList[i],
                    i < 7,       // 1~7번은 국비카드 보유, 8~10번은 미보유
                    amounts[i]
            );

            memberSurveyRepository.save(survey);
            ids.add(memberId);
            registerTestData(batchId, "member_surveys", memberId);
        }

        return ids;
    }

    private void registerTestData(String batchId, String tableName, Long recordId) {
        TestDataRegistry registry = TestDataRegistry.create(batchId, tableName, recordId);
        testDataRepository.save(registry);
    }

    @Transactional
    public void deleteTestData() {
        if (!testDataRepository.exists()) {
            throw new IllegalStateException("삭제할 테스트 데이터가 없습니다.");
        }

        // FK 역순으로 삭제: reviews_details → reviews → certificates → member_surveys → members → lectures → organizations
        // reviews_details는 Review와 cascade로 삭제되므로 reviews만 삭제
        deleteByTable("reviews", reviewRepository::deleteById);
        deleteByTable("certificates", certificateRepository::deleteById);
        deleteByTable("member_surveys", memberSurveyRepository::deleteByMemberId);
        deleteByTable("members", memberRepository::deleteById);
        deleteByTable("lectures", lectureRepository::deleteById);
        deleteByTable("organizations", organizationRepository::deleteById);

        // Registry 비우기
        testDataRepository.deleteAll();
    }

    private void deleteByTable(String tableName, java.util.function.Consumer<Long> deleteFunction) {
        List<TestDataRegistry> registries = testDataRepository.findByTableName(tableName);
        for (TestDataRegistry registry : registries) {
            try {
                deleteFunction.accept(registry.getRecordId());
            } catch (Exception e) {
                // 이미 삭제되었거나 없는 경우 무시
            }
        }
    }

    @Transactional(readOnly = true)
    public TestDataSummary getSummary() {
        if (!testDataRepository.exists()) {
            return TestDataSummary.empty();
        }

        Optional<String> batchId = testDataRepository.findLatestBatchId();
        Map<String, Long> counts = testDataRepository.countByTable();

        // reviews_details 카운트 제외 (UI에서 보여줄 필요 없음)
        counts.remove("reviews_details");

        OffsetDateTime createdAt = testDataRepository.findByTableName("organizations")
                .stream()
                .findFirst()
                .map(TestDataRegistry::getCreatedAt)
                .orElse(null);

        return TestDataSummary.of(batchId.orElse(null), counts, createdAt);
    }
}
