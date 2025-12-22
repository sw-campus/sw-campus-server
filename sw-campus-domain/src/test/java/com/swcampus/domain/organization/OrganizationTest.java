package com.swcampus.domain.organization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationTest {

    @Test
    @DisplayName("Organization 생성 - create")
    void create() {
        // given
        Long userId = 1L;
        String name = "소프트웨어 캠퍼스";
        String description = "IT 교육 전문 기관";
        String certificateKey = "employment-certificates/2024/01/01/certificate.jpg";

        // when
        Organization organization = Organization.create(userId, name, description, certificateKey);

        // then
        assertThat(organization.getUserId()).isEqualTo(userId);
        assertThat(organization.getName()).isEqualTo(name);
        assertThat(organization.getDescription()).isEqualTo(description);
        assertThat(organization.getCertificateKey()).isEqualTo(certificateKey);
        assertThat(organization.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(organization.getCreatedAt()).isNotNull();
        assertThat(organization.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Organization 정보 수정")
    void updateInfo() {
        // given
        Organization organization = Organization.create(1L, "기존 이름", "기존 설명", "employment-certificates/cert.jpg");

        // when
        organization.updateInfo("새로운 이름", "새로운 설명");

        // then
        assertThat(organization.getName()).isEqualTo("새로운 이름");
        assertThat(organization.getDescription()).isEqualTo("새로운 설명");
    }

    @Test
    @DisplayName("시설 이미지 수정")
    void updateFacilityImages() {
        // given
        Organization organization = Organization.create(1L, "테스트 기관", "설명", "employment-certificates/cert.jpg");

        // when
        organization.updateFacilityImages(
                "http://image1.jpg",
                "http://image2.jpg",
                "http://image3.jpg",
                "http://image4.jpg"
        );

        // then
        assertThat(organization.getFacilityImageUrl()).isEqualTo("http://image1.jpg");
        assertThat(organization.getFacilityImageUrl2()).isEqualTo("http://image2.jpg");
        assertThat(organization.getFacilityImageUrl3()).isEqualTo("http://image3.jpg");
        assertThat(organization.getFacilityImageUrl4()).isEqualTo("http://image4.jpg");
    }

    @Test
    @DisplayName("로고 URL 수정")
    void updateLogoUrl() {
        // given
        Organization organization = Organization.create(1L, "테스트 기관", "설명", "employment-certificates/cert.jpg");

        // when
        organization.updateLogoUrl("http://logo.png");

        // then
        assertThat(organization.getLogoUrl()).isEqualTo("http://logo.png");
    }

    @Test
    @DisplayName("정부 인증 설정")
    void setGovAuth() {
        // given
        Organization organization = Organization.create(1L, "테스트 기관", "설명", "employment-certificates/cert.jpg");

        // when
        organization.setGovAuth("정부인증-2024-001");

        // then
        assertThat(organization.getGovAuth()).isEqualTo("정부인증-2024-001");
    }

    @Test
    @DisplayName("기관 승인")
    void approve() {
        // given
        Organization organization = Organization.create(1L, "테스트 기관", "설명", "employment-certificates/cert.jpg");

        // when
        organization.approve();

        // then
        assertThat(organization.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("기관 반려")
    void reject() {
        // given
        Organization organization = Organization.create(1L, "테스트 기관", "설명", "employment-certificates/cert.jpg");

        // when
        organization.reject();

        // then
        assertThat(organization.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
    }
}
