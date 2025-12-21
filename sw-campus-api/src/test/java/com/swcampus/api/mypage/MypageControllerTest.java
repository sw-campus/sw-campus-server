package com.swcampus.api.mypage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.mypage.request.UpdateProfileRequest;
import com.swcampus.api.mypage.request.UpsertSurveyRequest;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.mypage.MypageService;
import com.swcampus.domain.mypage.dto.CompletedLectureInfo;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewWithNickname;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(MypageController.class)
@Import(SecurityConfig.class)
class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private LectureService lectureService;

    @MockitoBean
    private MemberSurveyService memberSurveyService;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private MypageService mypageService;

    @MockitoBean
    private ReviewService reviewService;

    private MemberPrincipal memberPrincipal;
    private static final String TEST_PASSWORD = "password";

    private Member createTestMember() {
        return Member.of(1L, "test@example.com", TEST_PASSWORD, "Test User", "nickname", "010-1234-5678", Role.USER, null, "Seoul", LocalDateTime.now(), LocalDateTime.now());
    }

    @BeforeEach
    void setUp() {
        memberPrincipal = new MemberPrincipal(1L, "test@example.com", Role.USER);
        
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("내 정보 조회 - 성공")
    void getProfile_Success() throws Exception {
        // given
        Member member = createTestMember();
        given(memberService.getMember(1L)).willReturn(member);
        given(memberSurveyService.existsByMemberId(1L)).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/profile")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.hasSurvey").value(true));
        
        verify(memberService).getMember(1L);
        verify(memberSurveyService).existsByMemberId(1L);
    }

    @Test
    @DisplayName("내 정보 수정 - 성공")
    void updateProfile_Success() throws Exception {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest("New_Nickname", "01012345678", "New Address");

        // when & then
        mockMvc.perform(patch("/api/v1/mypage/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).updateProfile(1L, "New_Nickname", "01012345678", "New Address");
    }

    @Test
    @DisplayName("내 정보 수정 - 실패 (유효성 검증)")
    void updateProfile_InvalidInput_Returns400() throws Exception {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest("Valid_Nickname", "invalid-phone", "New Address");

        // when & then
        mockMvc.perform(patch("/api/v1/mypage/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 수강 완료 강의 조회 - 성공")
    void getMyCompletedLectures_Success() throws Exception {
        // given
        CompletedLectureInfo lectureInfo = new CompletedLectureInfo(
            1L, 100L, "Test Lecture", "image.jpg", "Test Org", LocalDateTime.now(), true
        );
        given(mypageService.getCompletedLectures(1L)).willReturn(List.of(lectureInfo));

        // when & then
        mockMvc.perform(get("/api/v1/mypage/completed-lectures")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lectureId").value(100))
                .andExpect(jsonPath("$[0].lectureName").value("Test Lecture"))
                .andExpect(jsonPath("$[0].organizationName").value("Test Org"))
                .andExpect(jsonPath("$[0].canWriteReview").value(true));
        
        verify(mypageService).getCompletedLectures(1L);
    }

    @Test
    @DisplayName("내 수강 완료 강의 조회 - 빈 목록")
    void getMyCompletedLectures_Empty() throws Exception {
        // given
        given(mypageService.getCompletedLectures(1L)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/mypage/completed-lectures")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(mypageService).getCompletedLectures(1L);
    }

    @Test
    @DisplayName("수강 완료 강의 후기 상세 조회 - 성공")
    void getMyCompletedLectureReview_Success() throws Exception {
        // given
        Long lectureId = 100L;
        List<ReviewDetail> details = List.of(
            ReviewDetail.create(ReviewCategory.TEACHER, 4.5, "강사님이 좋았습니다"),
            ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "커리큘럼이 좋았습니다"),
            ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.5, "행정이 좋았습니다"),
            ReviewDetail.create(ReviewCategory.FACILITY, 3.5, "시설이 좋았습니다"),
            ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "프로젝트가 좋았습니다")
        );
        Review review = Review.of(
            1L, 1L, lectureId, 1L,
            "좋은 강의였습니다", 4.3,
            ApprovalStatus.APPROVED, false,
            LocalDateTime.now(), LocalDateTime.now(),
            details
        );
        ReviewWithNickname reviewWithNickname = ReviewWithNickname.of(review, "테스터");

        given(reviewService.getMyReviewWithNicknameByLecture(1L, lectureId)).willReturn(reviewWithNickname);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/completed-lectures/{lectureId}/review", lectureId)
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.lectureId").value(100))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.detailScores").isArray())
                .andExpect(jsonPath("$.detailScores.length()").value(5));

        verify(reviewService).getMyReviewWithNicknameByLecture(1L, lectureId);
    }

    @Test
    @DisplayName("수강 완료 강의 후기 상세 조회 - 후기 없음")
    void getMyCompletedLectureReview_NotFound() throws Exception {
        // given
        Long lectureId = 999L;
        given(reviewService.getMyReviewWithNicknameByLecture(1L, lectureId))
                .willThrow(new ReviewNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/mypage/completed-lectures/{lectureId}/review", lectureId)
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("설문조사 조회 - 성공")
    void getSurvey_Success() throws Exception {
        MemberSurvey survey = MemberSurvey.create(1L, "CS", true, "Backend", "None", false, BigDecimal.ZERO);
        given(memberSurveyService.findSurveyByMemberId(1L)).willReturn(Optional.of(survey));

        // when & then
        mockMvc.perform(get("/api/v1/mypage/survey")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.major").value("CS"));
        
        verify(memberSurveyService).findSurveyByMemberId(1L);
    }

    @Test
    @DisplayName("설문조사 조회 - 없음 (빈 응답)")
    void getSurvey_Empty() throws Exception {
        given(memberSurveyService.findSurveyByMemberId(1L)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/mypage/survey")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
        
        verify(memberSurveyService).findSurveyByMemberId(1L);
    }

    @Test
    @DisplayName("설문조사 등록/수정 - 성공")
    void upsertSurvey_Success() throws Exception {
        // given
        UpsertSurveyRequest request = new UpsertSurveyRequest("CS", true, "Backend", "None", false, BigDecimal.ZERO);
        
        // when & then
        mockMvc.perform(put("/api/v1/mypage/survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isOk());
        
        verify(memberSurveyService).upsertSurvey(1L, "CS", true, "Backend", "None", false, BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("내 강의 목록 조회 (기관) - 성공")
    void getMyLectures_Success() throws Exception {
        // given
        MemberPrincipal orgPrincipal = new MemberPrincipal(1L, "org@example.com", Role.ORGANIZATION);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(orgPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Organization org = Organization.of(100L, 1L, "Test Org", "Desc", com.swcampus.domain.organization.ApprovalStatus.APPROVED, "cert.jpg", null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        
        given(organizationService.getOrganizationByUserId(1L)).willReturn(org);
        given(lectureService.findAllByOrgId(100L)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/mypage/lectures")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk());
        
        verify(organizationService).getOrganizationByUserId(1L);
        verify(lectureService).findAllByOrgId(100L);
    }

    @Test
    @DisplayName("기관 정보 조회 - 성공")
    void getOrganization_Success() throws Exception {
        // given
        MemberPrincipal orgPrincipal = new MemberPrincipal(1L, "org@example.com", Role.ORGANIZATION);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(orgPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Organization org = Organization.of(100L, 1L, "Test Org", "Desc", com.swcampus.domain.organization.ApprovalStatus.APPROVED, "cert.jpg", null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        Member member = createTestMember();
        
        given(organizationService.getOrganizationByUserId(1L)).willReturn(org);
        given(memberService.getMember(1L)).willReturn(member);

        // when & then
        mockMvc.perform(get("/api/v1/mypage/organization")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Test Org"));
        
        verify(organizationService).getOrganizationByUserId(1L);
        verify(memberService).getMember(1L);
    }

    @Test
    @DisplayName("기관 정보 수정 - 성공")
    void updateOrganization_Success() throws Exception {
        // given
        MemberPrincipal orgPrincipal = new MemberPrincipal(1L, "org@example.com", Role.ORGANIZATION);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(orgPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Organization org = Organization.of(100L, 1L, "Test Org", "Desc", com.swcampus.domain.organization.ApprovalStatus.APPROVED, "cert.jpg", null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        given(organizationService.getApprovedOrganizationByUserId(1L)).willReturn(org);

        MockMultipartFile file = new MockMultipartFile("businessRegistration", "cert.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile organizationNamePart = new MockMultipartFile("organizationName", "", "text/plain", "Updated Org".getBytes());
        MockMultipartFile phonePart = new MockMultipartFile("phone", "", "text/plain", "02-1234-5678".getBytes());
        MockMultipartFile locationPart = new MockMultipartFile("location", "", "text/plain", "New Address".getBytes());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/mypage/organization")
                        .file(file)
                        .file(organizationNamePart)
                        .file(phonePart)
                        .file(locationPart)
                        .with(csrf())
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk());
        
        verify(memberService).updateProfile(1L, null, "02-1234-5678", "New Address");
        verify(organizationService).updateOrganization(eq(100L), eq(1L), any(com.swcampus.domain.organization.dto.UpdateOrganizationParams.class));
    }

    /*
    @Test
    @DisplayName("내 정보 조회 - 인증 실패 (401)")
    void getProfile_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/mypage/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 강의 목록 조회 - 권한 없음 (403)")
    void getMyLectures_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/mypage/lectures")
                        .principal(new UsernamePasswordAuthenticationToken(memberPrincipal, null)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    */
}
