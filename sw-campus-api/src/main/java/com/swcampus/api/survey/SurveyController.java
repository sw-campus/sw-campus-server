
package com.swcampus.api.survey;

import com.swcampus.api.survey.request.CreateSurveyRequest;
import com.swcampus.api.survey.request.UpdateSurveyRequest;
import com.swcampus.api.survey.response.SurveyResponse;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Survey", description = "회원 설문조사 API")
@RestController
@RequestMapping("/api/v1/members/me/survey")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SurveyController {

    private final MemberSurveyService surveyService;

    @Operation(summary = "설문조사 작성", description = "회원 설문조사를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "409", description = "이미 설문조사 존재")
    })
    @PostMapping
    public ResponseEntity<SurveyResponse> createSurvey(@RequestBody CreateSurveyRequest request) {
        Long currentMemberId = getCurrentMemberId();

        MemberSurvey survey = surveyService.createSurvey(
                currentMemberId,
                request.getMajor(),
                request.getBootcampCompleted(),
                request.getWantedJobs(),
                request.getLicenses(),
                request.getHasGovCard(),
                request.getAffordableAmount()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SurveyResponse.from(survey));
    }

    @Operation(summary = "내 설문조사 조회", description = "본인의 설문조사를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "설문조사 없음")
    })
    @GetMapping
    public ResponseEntity<SurveyResponse> getMySurvey() {
        Long currentMemberId = getCurrentMemberId();
        MemberSurvey survey = surveyService.getSurveyByMemberId(currentMemberId);
        return ResponseEntity.ok(SurveyResponse.from(survey));
    }

    @Operation(summary = "내 설문조사 수정", description = "본인의 설문조사를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "설문조사 없음")
    })
    @PutMapping
    public ResponseEntity<SurveyResponse> updateMySurvey(@RequestBody UpdateSurveyRequest request) {
        Long currentMemberId = getCurrentMemberId();

        MemberSurvey survey = surveyService.updateSurvey(
                currentMemberId,
                request.getMajor(),
                request.getBootcampCompleted(),
                request.getWantedJobs(),
                request.getLicenses(),
                request.getHasGovCard(),
                request.getAffordableAmount()
        );

        return ResponseEntity.ok(SurveyResponse.from(survey));
    }

    private Long getCurrentMemberId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
}
