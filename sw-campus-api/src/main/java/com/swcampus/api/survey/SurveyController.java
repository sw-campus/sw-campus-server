package com.swcampus.api.survey;

import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.survey.request.SaveBasicSurveyRequest;
import com.swcampus.api.survey.request.SubmitAptitudeTestRequest;
import com.swcampus.api.survey.response.QuestionSetResponse;
import com.swcampus.api.survey.response.SurveyResponse;
import com.swcampus.api.survey.response.SurveyResultsResponse;
import com.swcampus.api.survey.response.SurveyStatusResponse;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.survey.AdminSurveyQuestionService;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import com.swcampus.domain.survey.QuestionSetType;
import com.swcampus.domain.survey.SurveyQuestionSet;
import com.swcampus.domain.survey.SurveyResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Survey", description = "회원 설문조사 API")
@RestController
@RequestMapping("/api/v1/members/me/survey")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class SurveyController {

    private final MemberSurveyService surveyService;
    private final AdminSurveyQuestionService questionService;

    @Operation(summary = "내 설문조사 조회", description = "본인의 설문조사를 조회합니다. 설문이 없으면 null을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (설문이 없으면 null)"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<SurveyResponse> getMySurvey(@CurrentMember MemberPrincipal member) {
        Long currentMemberId = member.memberId();
        return surveyService.findSurveyByMemberId(currentMemberId)
                .map(survey -> ResponseEntity.ok(SurveyResponse.from(survey)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    @Operation(summary = "기초 설문 저장", description = "기초 설문(5문항)을 저장합니다. 이미 존재하면 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    @PostMapping("/basic")
    public ResponseEntity<SurveyResponse> saveBasicSurvey(
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody SaveBasicSurveyRequest request) {
        Long currentMemberId = member.memberId();
        MemberSurvey survey = surveyService.saveBasicSurvey(currentMemberId, request.toDomain());
        return ResponseEntity.ok(SurveyResponse.from(survey));
    }

    @Operation(summary = "성향 테스트 제출", description = "성향 테스트(15문항)를 제출하고 결과를 받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 기초 설문 미완료"),
            @ApiResponse(responseCode = "404", description = "설문조사 없음")
    })
    @PostMapping("/aptitude-test")
    public ResponseEntity<SurveyResponse> submitAptitudeTest(
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody SubmitAptitudeTestRequest request) {
        Long currentMemberId = member.memberId();
        MemberSurvey survey = surveyService.submitAptitudeTest(
                currentMemberId,
                request.toDomain(),
                request.getQuestionSetVersion()
        );
        return ResponseEntity.ok(SurveyResponse.from(survey));
    }

    @Operation(summary = "설문 결과 조회", description = "성향 테스트 결과(추천 직무)만 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "성향 테스트 미완료"),
            @ApiResponse(responseCode = "404", description = "설문조사 없음")
    })
    @GetMapping("/results")
    public ResponseEntity<SurveyResultsResponse> getResults(@CurrentMember MemberPrincipal member) {
        Long currentMemberId = member.memberId();
        SurveyResults results = surveyService.getResultsByMemberId(currentMemberId);
        return ResponseEntity.ok(SurveyResultsResponse.from(results));
    }

    @Operation(summary = "설문 상태 조회", description = "설문 완료 상태와 AI 추천 사용 가능 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/status")
    public ResponseEntity<SurveyStatusResponse> getStatus(@CurrentMember MemberPrincipal member) {
        Long currentMemberId = member.memberId();
        return surveyService.findSurveyByMemberId(currentMemberId)
                .map(survey -> ResponseEntity.ok(SurveyStatusResponse.from(survey)))
                .orElseGet(() -> ResponseEntity.ok(SurveyStatusResponse.from(null)));
    }

    @Operation(summary = "발행된 문항 세트 조회", description = "발행된 설문 문항 세트를 타입별로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (발행된 세트가 없으면 null)"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/question-sets/{type}")
    public ResponseEntity<QuestionSetResponse> getPublishedQuestionSet(
            @CurrentMember MemberPrincipal member,
            @PathVariable(name = "type") QuestionSetType type) {
        return questionService.findPublishedQuestionSet(type)
                .map(questionSet -> ResponseEntity.ok(QuestionSetResponse.from(questionSet)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }
}
