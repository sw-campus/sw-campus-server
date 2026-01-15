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
import com.swcampus.domain.survey.*;
import com.swcampus.domain.survey.MemberSurveyRepository;
import com.swcampus.domain.teacher.Teacher;
import com.swcampus.domain.teacher.TeacherRepository;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.default-image.base-url}")
    private String defaultImageBaseUrl;

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

        // 11. 시연용 계정 생성 (수료증/리뷰 없음, 설문조사 있음)
        Long demoMemberId = createDemoMember(batchId, encodedPassword);
        createDemoSurvey(batchId, demoMemberId);

        // Member IDs 병합 (기관담당자 3명 + 일반회원 15명 + 시연용 1명)
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.addAll(orgMemberIds);
        allMemberIds.addAll(userMemberIds);
        allMemberIds.add(demoMemberId);

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
        String bucketUrl = defaultImageBaseUrl.replace("/defaults", "");
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
        String bucketUrl = defaultImageBaseUrl.replace("/defaults", "");
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
        LocalDateTime now = LocalDateTime.now();

        // 4개 강의 데이터 정의 (각 기관에 2개씩)
        // 강의 0: Java 백엔드 개발 부트캠프 (기관 A - 종로)
        // 강의 1: AI/ML 입문 과정 (기관 A - 종로)
        // 강의 2: Spring Boot 마스터 과정 (기관 B - 가산)
        // 강의 3: 데이터 사이언스 실무 (기관 B - 가산)

        List<LectureTestData> lectureDataList = createLectureTestDataList();

        // 커리큘럼 ID 매핑 (기존 seed 데이터 기준)
        // 백엔드(Category 4): curriculums 11-20
        // AI(Category 11): curriculums 61-70
        Long[][] backendCurriculums = {
                {11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L},
                {11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L}
        };
        Long[][] aiCurriculums = {
                {61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L},
                {61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L}
        };

        // 기본 이미지 URL
        String[] defaultImages = {
                defaultImageBaseUrl + "/web-development.png",
                defaultImageBaseUrl + "/data-ai.png"
        };

        // 각 강의에 배치할 선생님 인덱스
        int[][] teacherIndices = {
                {0, 1},
                {2, 3, 4},
                {5, 6},
                {7, 8, 9}
        };

        int lectureCount = 0;
        for (int orgIdx = 0; orgIdx < organizationIds.size(); orgIdx++) {
            Long orgId = organizationIds.get(orgIdx);
            for (int lectIdx = 0; lectIdx < 2; lectIdx++) {
                LectureTestData data = lectureDataList.get(lectureCount);

                // LectureCurriculum 목록 생성 (강의별로 다른 레벨 패턴)
                List<LectureCurriculum> lectureCurriculums = new ArrayList<>();
                Long[] currIds = (lectIdx == 0) ? backendCurriculums[orgIdx] : aiCurriculums[orgIdx];
                CurriculumLevel[] levelPattern = getCurriculumLevelPattern(lectureCount);
                for (int i = 0; i < currIds.length; i++) {
                    lectureCurriculums.add(LectureCurriculum.builder()
                            .curriculumId(currIds[i])
                            .level(levelPattern[i])
                            .build());
                }

                // 선생님 목록 생성
                List<Teacher> teachers = new ArrayList<>();
                for (int teacherIdx : teacherIndices[lectureCount]) {
                    teachers.add(Teacher.builder()
                            .teacherId(teacherIds.get(teacherIdx))
                            .build());
                }

                // 선발절차 생성
                List<LectureStep> steps = createLectureSteps(data.stepTypes);

                // 추가혜택 생성
                List<LectureAdd> adds = createLectureAdds(data.addNames);

                // 지원자격 생성
                List<LectureQual> quals = createLectureQuals(data.requiredQuals, data.preferredQuals);

                // 마지막 강의(4번째)는 PENDING 상태로 생성
                LectureAuthStatus authStatus = (lectureCount == 3)
                        ? LectureAuthStatus.PENDING
                        : LectureAuthStatus.APPROVED;

                Lecture lecture = Lecture.builder()
                        .orgId(orgId)
                        .lectureName(data.name)
                        .lectureImageUrl(defaultImages[lectIdx])
                        .days(data.days)
                        .startTime(data.startTime)
                        .endTime(data.endTime)
                        .lectureLoc(data.lectureLoc)
                        .location(data.location)
                        .recruitType(data.recruitType)
                        .subsidy(data.subsidy)
                        .lectureFee(data.lectureFee)
                        .eduSubsidy(data.eduSubsidy)
                        .goal(data.goal)
                        .maxCapacity(data.maxCapacity)
                        .equipPc(data.equipPc)
                        .equipMerit(data.equipMerit)
                        .books(data.books)
                        // 취업지원서비스
                        .resume(data.resume)
                        .mockInterview(data.mockInterview)
                        .employmentHelp(data.employmentHelp)
                        .afterCompletion(data.afterCompletion)
                        // 프로젝트
                        .projectNum(data.projectNum)
                        .projectTime(data.projectTime)
                        .projectTeam(data.projectTeam)
                        .projectTool(data.projectTool)
                        .projectMentor(data.projectMentor)
                        .url(data.url)
                        .status(LectureStatus.RECRUITING)
                        .lectureAuthStatus(authStatus)
                        .startAt(data.startAt)
                        .endAt(data.endAt)
                        .deadline(data.deadline)
                        .totalDays(data.totalDays)
                        .totalTimes(data.totalTimes)
                        .createdAt(now)
                        .updatedAt(now)
                        .lectureCurriculums(lectureCurriculums)
                        .teachers(teachers)
                        .steps(steps)
                        .adds(adds)
                        .quals(quals)
                        .build();

                Lecture saved = lectureRepository.save(lecture);
                ids.add(saved.getLectureId());
                registerTestData(batchId, "lectures", saved.getLectureId());
                lectureCount++;
            }
        }
        return ids;
    }

    /**
     * 4개 강의에 대한 차별화된 테스트 데이터 생성
     */
    private List<LectureTestData> createLectureTestDataList() {
        LocalDateTime now = LocalDateTime.now();
        List<LectureTestData> list = new ArrayList<>();

        // 강의 0: Java 백엔드 개발 부트캠프 (기관 A - 종로) - 가장 체계적인 풀타임 과정
        LectureTestData data0 = new LectureTestData();
        data0.name = "Java 백엔드 개발 부트캠프";
        data0.days = Set.of(LectureDay.MONDAY, LectureDay.TUESDAY, LectureDay.WEDNESDAY, LectureDay.THURSDAY, LectureDay.FRIDAY);
        data0.startTime = LocalTime.of(9, 0);
        data0.endTime = LocalTime.of(18, 0);
        data0.lectureLoc = LectureLocation.OFFLINE;
        data0.location = "서울시 종로구 종로 33, 그랑서울타워 15층";
        data0.recruitType = RecruitType.CARD_REQUIRED;
        data0.subsidy = BigDecimal.valueOf(6100000);  // 정부지원금 610만원
        data0.lectureFee = BigDecimal.valueOf(400000);  // 자기부담금 40만원
        data0.eduSubsidy = BigDecimal.valueOf(316000);  // 훈련수당 31.6만원
        data0.goal = "Java와 Spring Boot를 활용한 엔터프라이즈급 백엔드 개발자 양성";
        data0.maxCapacity = 30;
        data0.equipPc = EquipmentType.PC;
        data0.equipMerit = "듀얼 모니터, 인체공학 의자, 개인 사물함 제공";
        data0.books = true;
        data0.resume = true;
        data0.mockInterview = true;
        data0.employmentHelp = true;
        data0.afterCompletion = true;
        data0.projectNum = 4;
        data0.projectTime = 320;
        data0.projectTeam = "4~5인 팀 프로젝트";
        data0.projectTool = "Git, Jira, Notion, Slack";
        data0.projectMentor = true;
        data0.url = "https://edu.kosta.or.kr/java-backend";
        data0.startAt = now.plusDays(45);
        data0.endAt = now.plusDays(165);
        data0.deadline = now.plusDays(40);
        data0.totalDays = 120;
        data0.totalTimes = 960;
        data0.stepTypes = List.of(SelectionStepType.DOCUMENT, SelectionStepType.CODING_TEST, SelectionStepType.INTERVIEW);
        data0.addNames = List.of("노트북 대여", "취업축하금 100만원", "우수 수료생 해외연수", "자격증 응시료 지원");
        data0.requiredQuals = List.of("내일배움카드 소지자", "6개월 이상 풀타임 참여 가능자");
        data0.preferredQuals = List.of("프로그래밍 언어 학습 경험자", "IT 관련 전공자", "팀 프로젝트 경험자");
        list.add(data0);

        // 강의 1: AI/ML 입문 과정 (기관 A - 종로) - 파트타임 온라인 병행 과정
        LectureTestData data1 = new LectureTestData();
        data1.name = "AI/ML 입문 과정";
        data1.days = Set.of(LectureDay.MONDAY, LectureDay.WEDNESDAY, LectureDay.FRIDAY);
        data1.startTime = LocalTime.of(19, 0);
        data1.endTime = LocalTime.of(22, 0);
        data1.lectureLoc = LectureLocation.MIXED;
        data1.location = "서울시 종로구 종로 33, 그랑서울타워 16층 (온라인 병행)";
        data1.recruitType = RecruitType.GENERAL;
        data1.subsidy = BigDecimal.valueOf(0);  // 정부지원금 없음 (일반과정)
        data1.lectureFee = BigDecimal.valueOf(3200000);  // 자기부담금 320만원 (전액)
        data1.eduSubsidy = BigDecimal.valueOf(0);  // 훈련수당 없음
        data1.goal = "Python 기반 머신러닝과 딥러닝 기초 역량 확보";
        data1.maxCapacity = 25;
        data1.equipPc = EquipmentType.LAPTOP;
        data1.equipMerit = "GPU 서버 원격 접속 환경, Jupyter Hub 제공";
        data1.books = true;
        data1.resume = false;
        data1.mockInterview = false;
        data1.employmentHelp = false;
        data1.afterCompletion = true;
        data1.projectNum = 2;
        data1.projectTime = 80;
        data1.projectTeam = "2~3인 팀 프로젝트";
        data1.projectTool = "GitHub, Google Colab";
        data1.projectMentor = true;
        data1.url = "https://edu.kosta.or.kr/ai-ml-basic";
        data1.startAt = now.plusDays(30);
        data1.endAt = now.plusDays(90);
        data1.deadline = now.plusDays(25);
        data1.totalDays = 60;
        data1.totalTimes = 180;
        data1.stepTypes = List.of(SelectionStepType.DOCUMENT, SelectionStepType.PRE_TASK);
        data1.addNames = List.of("AI 관련 도서 제공", "수료 후 심화과정 할인");
        data1.requiredQuals = List.of("Python 기초 문법 이해자", "개인 노트북 지참 가능자");
        data1.preferredQuals = List.of("수학/통계 기초 지식 보유자", "데이터 분석 관심자");
        list.add(data1);

        // 강의 2: Spring Boot 마스터 과정 (기관 B - 가산) - 실무 중심 풀타임 과정
        LectureTestData data2 = new LectureTestData();
        data2.name = "Spring Boot 마스터 과정";
        data2.days = Set.of(LectureDay.MONDAY, LectureDay.TUESDAY, LectureDay.WEDNESDAY, LectureDay.THURSDAY, LectureDay.FRIDAY);
        data2.startTime = LocalTime.of(9, 30);
        data2.endTime = LocalTime.of(18, 30);
        data2.lectureLoc = LectureLocation.OFFLINE;
        data2.location = "서울시 금천구 가산디지털1로 168, 우림라이온스밸리 B동 12층";
        data2.recruitType = RecruitType.CARD_REQUIRED;
        data2.subsidy = BigDecimal.valueOf(5450000);  // 정부지원금 545만원
        data2.lectureFee = BigDecimal.valueOf(350000);  // 자기부담금 35만원
        data2.eduSubsidy = BigDecimal.valueOf(280000);  // 훈련수당 28만원
        data2.goal = "Spring Boot 기반 MSA 아키텍처 설계 및 구현 역량 강화";
        data2.maxCapacity = 24;
        data2.equipPc = EquipmentType.PC;
        data2.equipMerit = "최신 사양 PC, 스탠딩 데스크 옵션, 카페테리아 무료 이용";
        data2.books = false;
        data2.resume = true;
        data2.mockInterview = true;
        data2.employmentHelp = true;
        data2.afterCompletion = false;
        data2.projectNum = 3;
        data2.projectTime = 240;
        data2.projectTeam = "3~4인 스크럼 팀";
        data2.projectTool = "Git, GitHub Actions, AWS, Docker";
        data2.projectMentor = true;
        data2.url = "https://edu.kosta.or.kr/spring-master";
        data2.startAt = now.plusDays(60);
        data2.endAt = now.plusDays(150);
        data2.deadline = now.plusDays(55);
        data2.totalDays = 90;
        data2.totalTimes = 720;
        data2.stepTypes = List.of(SelectionStepType.DOCUMENT, SelectionStepType.CODING_TEST, SelectionStepType.INTERVIEW);
        data2.addNames = List.of("AWS 크레딧 $100 제공", "협력사 인턴십 연계", "기술 블로그 운영 지원");
        data2.requiredQuals = List.of("내일배움카드 소지자", "Java 기초 문법 이해자", "3개월 이상 풀타임 참여 가능자");
        data2.preferredQuals = List.of("웹 개발 경험자", "Spring Framework 사용 경험자", "CS 기초 지식 보유자");
        list.add(data2);

        // 강의 3: 데이터 사이언스 실무 (기관 B - 가산) - 온라인 중심 과정
        LectureTestData data3 = new LectureTestData();
        data3.name = "데이터 사이언스 실무";
        data3.days = Set.of(LectureDay.TUESDAY, LectureDay.THURSDAY, LectureDay.SATURDAY);
        data3.startTime = LocalTime.of(10, 0);
        data3.endTime = LocalTime.of(17, 0);
        data3.lectureLoc = LectureLocation.ONLINE;
        data3.location = null;
        data3.recruitType = RecruitType.GENERAL;
        data3.subsidy = BigDecimal.valueOf(0);  // 정부지원금 없음 (일반과정)
        data3.lectureFee = BigDecimal.valueOf(4500000);  // 자기부담금 450만원 (전액)
        data3.eduSubsidy = BigDecimal.valueOf(0);  // 훈련수당 없음
        data3.goal = "실무 데이터 분석 프로젝트 수행 능력 및 ML 모델 배포 역량 확보";
        data3.maxCapacity = 40;
        data3.equipPc = EquipmentType.LAPTOP;
        data3.equipMerit = "클라우드 GPU 인스턴스 제공, 실습 데이터셋 무제한 접근";
        data3.books = true;
        data3.resume = true;
        data3.mockInterview = false;
        data3.employmentHelp = true;
        data3.afterCompletion = true;
        data3.projectNum = 5;
        data3.projectTime = 200;
        data3.projectTeam = "개인 프로젝트 + 2인 페어 프로젝트";
        data3.projectTool = "Python, Pandas, Scikit-learn, TensorFlow, MLflow";
        data3.projectMentor = false;
        data3.url = "https://edu.kosta.or.kr/data-science";
        data3.startAt = now.plusDays(21);
        data3.endAt = now.plusDays(105);
        data3.deadline = now.plusDays(18);
        data3.totalDays = 84;
        data3.totalTimes = 504;
        data3.stepTypes = List.of(SelectionStepType.DOCUMENT, SelectionStepType.PRE_TASK, SelectionStepType.INTERVIEW);
        data3.addNames = List.of("Kaggle 대회 참가 지원", "데이터 분석 자격증 응시료 지원", "포트폴리오 제작 지원");
        data3.requiredQuals = List.of("Python 중급 이상 활용 가능자", "통계학 기초 이해자");
        data3.preferredQuals = List.of("SQL 활용 경험자", "데이터 분석 프로젝트 경험자", "관련 분야 실무 경험자", "Kaggle 참여 경험자");
        list.add(data3);

        return list;
    }

    /**
     * 강의 테스트 데이터를 담는 내부 클래스
     */
    private static class LectureTestData {
        String name;
        Set<LectureDay> days;
        LocalTime startTime;
        LocalTime endTime;
        LectureLocation lectureLoc;
        String location;
        RecruitType recruitType;
        BigDecimal subsidy;
        BigDecimal lectureFee;
        BigDecimal eduSubsidy;
        String goal;
        Integer maxCapacity;
        EquipmentType equipPc;
        String equipMerit;
        Boolean books;
        Boolean resume;
        Boolean mockInterview;
        Boolean employmentHelp;
        Boolean afterCompletion;
        Integer projectNum;
        Integer projectTime;
        String projectTeam;
        String projectTool;
        Boolean projectMentor;
        String url;
        LocalDateTime startAt;
        LocalDateTime endAt;
        LocalDateTime deadline;
        Integer totalDays;
        Integer totalTimes;
        List<SelectionStepType> stepTypes;
        List<String> addNames;
        List<String> requiredQuals;
        List<String> preferredQuals;
    }

    /**
     * 강의별로 다른 커리큘럼 레벨 패턴 반환 (10개 커리큘럼)
     */
    private CurriculumLevel[] getCurriculumLevelPattern(int lectureIndex) {
        return switch (lectureIndex) {
            // 강의 0: Java 백엔드 - 기본 6개, 심화 4개 (입문자 친화적)
            case 0 -> new CurriculumLevel[]{
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED
            };
            // 강의 1: AI/ML 입문 - 기본 8개, 심화 2개 (완전 입문 과정)
            case 1 -> new CurriculumLevel[]{
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED
            };
            // 강의 2: Spring Boot 마스터 - 기본 3개, 심화 7개 (심화 과정)
            case 2 -> new CurriculumLevel[]{
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED
            };
            // 강의 3: 데이터 사이언스 - 기본 5개, 심화 5개 (균형)
            default -> new CurriculumLevel[]{
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.BASIC, CurriculumLevel.BASIC,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED,
                    CurriculumLevel.ADVANCED, CurriculumLevel.ADVANCED
            };
        };
    }

    private List<LectureStep> createLectureSteps(List<SelectionStepType> stepTypes) {
        List<LectureStep> steps = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < stepTypes.size(); i++) {
            steps.add(LectureStep.builder()
                    .stepType(stepTypes.get(i))
                    .stepOrder(i + 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }
        return steps;
    }

    private List<LectureAdd> createLectureAdds(List<String> addNames) {
        List<LectureAdd> adds = new ArrayList<>();
        for (String name : addNames) {
            adds.add(LectureAdd.builder()
                    .addName(name)
                    .build());
        }
        return adds;
    }

    private List<LectureQual> createLectureQuals(List<String> requiredQuals, List<String> preferredQuals) {
        List<LectureQual> quals = new ArrayList<>();
        for (String text : requiredQuals) {
            quals.add(LectureQual.builder()
                    .type(LectureQualType.REQUIRED)
                    .text(text)
                    .build());
        }
        for (String text : preferredQuals) {
            quals.add(LectureQual.builder()
                    .type(LectureQualType.PREFERRED)
                    .text(text)
                    .build());
        }
        return quals;
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

    /**
     * 시연용 계정 생성 (수료증/리뷰 없음)
     */
    private Long createDemoMember(String batchId, String encodedPassword) {
        Member member = Member.of(
                null,
                "sw.campus2025@gmail.com",
                encodedPassword,
                "SW캠퍼스",
                "캠퍼스지기",
                "010-0000-0000",
                Role.USER,
                null,
                "서울",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Member saved = memberRepository.save(member);
        registerTestData(batchId, "members", saved.getId());
        return saved.getId();
    }

    /**
     * 시연용 계정 설문조사 생성
     */
    private void createDemoSurvey(String batchId, Long memberId) {
        BasicSurvey basicSurvey = BasicSurvey.builder()
                .major("컴퓨터공학")
                .programmingExperience(ProgrammingExperience.noExperience())
                .preferredLearningMethod(LearningMethod.OFFLINE)
                .desiredJobs(List.of(DesiredJob.BACKEND))
                .affordableBudgetRange(BudgetRange.OVER_200)
                .build();
        MemberSurvey survey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
        memberSurveyRepository.save(survey);
        registerTestData(batchId, "member_surveys", memberId);
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

        DesiredJob[][] desiredJobsList = {
                {DesiredJob.BACKEND}, {DesiredJob.FRONTEND}, {DesiredJob.BACKEND, DesiredJob.FRONTEND},
                {DesiredJob.DATA}, {DesiredJob.AI}, {DesiredJob.BACKEND}, {DesiredJob.DATA, DesiredJob.AI},
                {DesiredJob.MOBILE}, {DesiredJob.BACKEND}, {DesiredJob.OTHER}
        };

        LearningMethod[] learningMethods = {
                LearningMethod.OFFLINE, LearningMethod.ONLINE, LearningMethod.MIXED,
                LearningMethod.OFFLINE, LearningMethod.ONLINE, LearningMethod.MIXED,
                LearningMethod.OFFLINE, LearningMethod.ONLINE, LearningMethod.MIXED,
                LearningMethod.OFFLINE
        };

        BudgetRange[] budgetRanges = {
                BudgetRange.UNDER_50, BudgetRange.RANGE_50_100, BudgetRange.RANGE_100_200,
                BudgetRange.OVER_200, BudgetRange.UNDER_50, BudgetRange.RANGE_50_100,
                BudgetRange.RANGE_100_200, BudgetRange.OVER_200, BudgetRange.UNDER_50,
                BudgetRange.RANGE_50_100
        };

        for (int i = 0; i < userMemberIds.size(); i++) {
            Long memberId = userMemberIds.get(i);

            ProgrammingExperience experience = (i % 3 != 0)
                    ? ProgrammingExperience.withExperience("부트캠프 " + (i + 1))
                    : ProgrammingExperience.noExperience();

            BasicSurvey basicSurvey = BasicSurvey.builder()
                    .major(majors[i])
                    .programmingExperience(experience)
                    .preferredLearningMethod(learningMethods[i])
                    .desiredJobs(List.of(desiredJobsList[i]))
                    .affordableBudgetRange(budgetRanges[i])
                    .build();

            MemberSurvey survey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
            memberSurveyRepository.save(survey);
            ids.add(memberId);
            registerTestData(batchId, "member_surveys", memberId);
        }

        return ids;
    }

    private List<Long> createBanners(String batchId, List<Long> lectureIds) {
        List<Long> ids = new ArrayList<>();
        String bucketUrl = defaultImageBaseUrl.replace("/defaults", "");
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
