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
import com.swcampus.domain.teacher.Teacher;
import com.swcampus.domain.teacher.TeacherRepository;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
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
    private final BannerRepository bannerRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

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

        // 4. Teacher 생성 (10명)
        List<Long> teacherIds = createTeachers(batchId);

        // 5. Lecture 생성 (승인된 기관만, 기관당 2개 = 4개, 각 강의에 Teacher 2~3명 배치)
        // 분당센터(3번째)는 PENDING 상태이므로 강의 생성 제외
        List<Long> approvedOrgIds = organizationIds.subList(0, 2);
        List<Long> lectureIds = createLectures(batchId, approvedOrgIds, teacherIds);

        // 6. 일반회원 Member 생성 (15명)
        List<Long> userMemberIds = createUserMembers(batchId, encodedPassword);

        // 7. Certificate 생성 (일반회원 15명 × 강의 4개 = 60건)
        List<Long> certificateIds = createCertificates(batchId, userMemberIds, lectureIds);

        // 8. Review 생성 (일반회원 15명 × 강의 4개 = 60건)
        List<Long> reviewIds = createReviews(batchId, userMemberIds, lectureIds, certificateIds);

        // 9. Survey 생성 (일반회원 중 처음 10명)
        List<Long> surveyMemberIds = createSurveys(batchId, userMemberIds.subList(0, 10));

        // 10. Banner 생성 (BIG 3개, MIDDLE 2개, SMALL 3개)
        List<Long> bannerIds = createBanners(batchId, lectureIds);

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
                .bannerIds(bannerIds)
                .teacherIds(teacherIds)
                .build();
    }

    private List<Long> createOrganizationMembers(String batchId, String encodedPassword) {
        List<Long> ids = new ArrayList<>();
        String[] regions = {"서울", "서울", "경기"};
        for (int i = 1; i <= 3; i++) {
            Member member = Member.of(
                    null,
                    "test_org_" + i + "@test.com",
                    encodedPassword,
                    "테스트기관" + i + "담당자",
                    "테스트기관담당자" + i,
                    "010-0000-000" + i,
                    Role.ORGANIZATION,
                    null,  // orgId - 나중에 업데이트
                    regions[i - 1],
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
        String[] orgNames = {
                "한국소프트웨어기술진흥협회 : 종로",
                "한국소프트웨어기술진흥협회 : 가산",
                "한국소프트웨어기술진흥협회 : 분당"
        };
        String[] descriptions = {
                "한국소프트웨어기술진흥협회 종로센터입니다. 소프트웨어 개발 교육을 전문으로 합니다.",
                "한국소프트웨어기술진흥협회 가산센터입니다. AI/ML 교육을 전문으로 합니다.",
                "한국소프트웨어기술진흥협회 분당센터입니다. 클라우드/DevOps 교육을 전문으로 합니다."
        };
        ApprovalStatus[] statuses = {
                ApprovalStatus.APPROVED,
                ApprovalStatus.APPROVED,
                ApprovalStatus.PENDING  // 분당센터는 승인 대기 상태
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
                },
                {
                        bucketUrl + "/organizations/2024/12/24/c-test-1.jpg",
                        bucketUrl + "/organizations/2024/12/24/c-test-2.jpg",
                        bucketUrl + "/organizations/2024/12/24/c-test-3.jpg",
                        bucketUrl + "/organizations/2024/12/24/c-test-4.jpg"
                }
        };

        // 로고 URL (S3)
        String[] logoUrls = {
                bucketUrl + "/organizations/2024/12/24/a-logo.png",
                bucketUrl + "/organizations/2024/12/24/b-logo.png",
                bucketUrl + "/organizations/2024/12/24/c-logo.png"
        };

        // 재직증명서 Key (Private S3 Bucket)
        String[] certificateKeys = {
                "employment-certificates/2024/12/24/test-employment-1.png",
                "employment-certificates/2024/12/24/test-employment-2.png",
                "employment-certificates/2024/12/24/test-employment-3.png"
        };

        for (int i = 0; i < 3; i++) {
            Organization org = Organization.of(
                    null,
                    memberIds.get(i),
                    orgNames[i],
                    descriptions[i],
                    statuses[i],
                    certificateKeys[i],  // certificateKey (재직증명서)
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

    private List<Long> createTeachers(String batchId) {
        List<Long> ids = new ArrayList<>();
        String bucketUrl = "https://sw-campus-public-prod-afe42bff.s3.amazonaws.com";
        String teacherPath = bucketUrl + "/teachers/2024/12/24/";

        // 10명의 선생님 정보: [이름, 설명, 이미지 파일명]
        String[][] teacherData = {
                {"김영수", "Java/Spring 전문 강사. 10년 이상 백엔드 개발 경험 보유.", "teacher-1.png"},
                {"이미경", "풀스택 개발자 출신 강사. AWS 공인 솔루션 아키텍트.", "teacher-2.png"},
                {"박준호", "AI/ML 연구원 출신. 딥러닝 분야 논문 다수 게재.", "teacher-3.png"},
                {"최수진", "데이터 사이언티스트. 대기업 데이터 분석 프로젝트 리드 경험.", "teacher-4.png"},
                {"정민우", "Python/Django 전문가. 오픈소스 컨트리뷰터.", "teacher-5.png"},
                {"한서연", "프론트엔드 개발자 출신. React/Vue 전문 강사.", "teacher-6.png"},
                {"강동현", "DevOps 엔지니어. CI/CD 파이프라인 구축 전문가.", "teacher-7.png"},
                {"윤지현", "클라우드 아키텍트. GCP/Azure 공인 자격 보유.", "teacher-8.png"},
                {"송태영", "보안 전문가. 정보보안기사, CISSP 자격 보유.", "teacher-9.png"},
                {"임하나", "모바일 개발자. iOS/Android 네이티브 앱 개발 경험.", "teacher-10.png"}
        };

        for (String[] data : teacherData) {
            Teacher teacher = Teacher.builder()
                    .teacherName(data[0])
                    .teacherDescription(data[1])
                    .teacherImageUrl(teacherPath + data[2])
                    .build();

            Teacher saved = teacherRepository.save(teacher);
            ids.add(saved.getTeacherId());
            registerTestData(batchId, "teachers", saved.getTeacherId());
        }

        return ids;
    }

    private List<Long> createLectures(String batchId, List<Long> organizationIds, List<Long> teacherIds) {
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

        // 각 강의에 배치할 선생님 인덱스 (10명을 4개 강의에 2~3명씩 중복 없이 배치)
        // 강의 0 (기관A-백엔드): 선생님 0, 1 (2명)
        // 강의 1 (기관A-AI): 선생님 2, 3, 4 (3명)
        // 강의 2 (기관B-백엔드): 선생님 5, 6 (2명)
        // 강의 3 (기관B-AI): 선생님 7, 8, 9 (3명)
        int[][] teacherIndices = {
                {0, 1},       // 강의 0
                {2, 3, 4},    // 강의 1
                {5, 6},       // 강의 2
                {7, 8, 9}     // 강의 3
        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.plusDays(30);
        LocalDateTime endDate = now.plusDays(120);
        LocalDateTime deadline = now.plusDays(25);

        int lectureCount = 0;
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

                // 현재 강의에 배치할 선생님 목록 생성
                List<Teacher> teachers = new ArrayList<>();
                for (int teacherIdx : teacherIndices[lectureCount]) {
                    teachers.add(Teacher.builder()
                            .teacherId(teacherIds.get(teacherIdx))
                            .build());
                }

                // 마지막 강의(4번째)는 PENDING 상태로 생성
                LectureAuthStatus authStatus = (lectureCount == 3)
                        ? LectureAuthStatus.PENDING
                        : LectureAuthStatus.APPROVED;

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
                        .lectureAuthStatus(authStatus)
                        .startAt(startDate)
                        .endAt(endDate)
                        .deadline(deadline)
                        .totalDays(90)
                        .totalTimes(720)
                        .createdAt(now)
                        .updatedAt(now)
                        .lectureCurriculums(lectureCurriculums)
                        .teachers(teachers)
                        .build();

                Lecture saved = lectureRepository.save(lecture);
                ids.add(saved.getLectureId());
                registerTestData(batchId, "lectures", saved.getLectureId());
                lectureCount++;
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

            for (int lectureIdx = 0; lectureIdx < lectureIds.size(); lectureIdx++) {
                Long lectureId = lectureIds.get(lectureIdx);
                // 수료증 Key (Private S3 Bucket): certificates/2024/12/24/test-cert-user{n}-lecture{m}.png
                String imageKey = String.format("certificates/2024/12/24/test-cert-user%d-lecture%d.png",
                        memberIdx + 1, lectureIdx + 1);

                Certificate cert = Certificate.of(
                        null,
                        memberId,
                        lectureId,
                        imageKey,
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
        int reviewCount = 0;
        for (int memberIdx = 0; memberIdx < userMemberIds.size(); memberIdx++) {
            Long memberId = userMemberIds.get(memberIdx);

            for (int lectIdx = 0; lectIdx < lectureIds.size(); lectIdx++) {
                // 리뷰별 상태 결정
                // 회원 인덱스 3의 첫 번째 강의 리뷰만 PENDING (수료증이 APPROVED인 회원)
                // (리뷰 통계는 수료증이 APPROVED인 리뷰만 카운트하므로)
                ApprovalStatus reviewStatus;
                if (memberIdx == 3 && lectIdx == 0) {
                    reviewStatus = ApprovalStatus.PENDING;
                } else {
                    reviewStatus = ApprovalStatus.APPROVED;
                }
                reviewCount++;
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

    private List<Long> createBanners(String batchId, List<Long> lectureIds) {
        List<Long> ids = new ArrayList<>();
        String bucketUrl = "https://sw-campus-public-prod-afe42bff.s3.amazonaws.com";
        String bannerPath = bucketUrl + "/banners/2024/12/24/";

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDate = now.minusDays(30);  // 30일 전부터 시작
        OffsetDateTime endDate = now.plusDays(60);     // 60일 후까지 진행

        // 배너 정의: [타입, 이미지 파일명, 연결할 강의 인덱스]
        Object[][] bannerDefs = {
                {BannerType.BIG, "test-big-1.jpg", 0},
                {BannerType.BIG, "test-big-2.jpg", 1},
                {BannerType.BIG, "test-big-3.jpg", 2},
                {BannerType.MIDDLE, "test-middle-1.jpg", 1},
                {BannerType.MIDDLE, "test-middle-2.jpg", 2},
                {BannerType.SMALL, "test-small-1.jpg", 0},
                {BannerType.SMALL, "test-small-2.jpg", 1},
                {BannerType.SMALL, "test-small-3.jpg", 3}
        };

        for (Object[] def : bannerDefs) {
            BannerType type = (BannerType) def[0];
            String imageFile = (String) def[1];
            int lectureIndex = (int) def[2];

            Long lectureId = lectureIds.get(lectureIndex);

            Banner banner = Banner.builder()
                    .lectureId(lectureId)
                    .type(type)
                    .url("https://edu.kosta.or.kr/")
                    .imageUrl(bannerPath + imageFile)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(true)
                    .build();

            Banner saved = bannerRepository.save(banner);
            ids.add(saved.getId());
            registerTestData(batchId, "banners", saved.getId());
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

        // FK 역순으로 삭제: banners → reviews → certificates → member_surveys → members → lectures → lecture_teachers → teachers → organizations
        // reviews_details는 Review와 cascade로 삭제되므로 reviews만 삭제
        deleteByTable("banners", bannerRepository::deleteById);
        deleteByTable("reviews", reviewRepository::deleteById);
        deleteByTable("certificates", certificateRepository::deleteById);
        deleteByTable("member_surveys", memberSurveyRepository::deleteByMemberId);
        deleteByTable("members", memberRepository::deleteById);
        deleteByTable("lectures", lectureRepository::deleteById);

        // lecture_teachers 삭제 (테스트 데이터 teacher를 참조하는 모든 연결 삭제)
        List<Long> teacherIds = testDataRepository.findByTableName("teachers").stream()
                .map(TestDataRegistry::getRecordId)
                .toList();
        if (!teacherIds.isEmpty()) {
            entityManager.createNativeQuery("DELETE FROM lecture_teachers WHERE teacher_id IN :teacherIds")
                    .setParameter("teacherIds", teacherIds)
                    .executeUpdate();
        }

        deleteByTable("teachers", teacherRepository::deleteById);
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
