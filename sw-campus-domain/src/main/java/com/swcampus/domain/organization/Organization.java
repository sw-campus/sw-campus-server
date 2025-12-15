package com.swcampus.domain.organization;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private ApprovalStatus approvalStatus;
    private String certificateUrl;
    private String govAuth;
    private String facilityImageUrl;
    private String facilityImageUrl2;
    private String facilityImageUrl3;
    private String facilityImageUrl4;
    private String logoUrl;
    private String homepage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Organization create(Long userId, String name, String description, String certificateUrl) {
        Organization org = new Organization();
        org.userId = userId;
        org.name = name;
        org.description = description;
        org.approvalStatus = ApprovalStatus.PENDING;
        org.certificateUrl = certificateUrl;
        org.createdAt = LocalDateTime.now();
        org.updatedAt = LocalDateTime.now();
        return org;
    }

    public static Organization of(Long id, Long userId, String name, String description,
            ApprovalStatus approvalStatus, String certificateUrl,
            String govAuth, String facilityImageUrl,
            String facilityImageUrl2, String facilityImageUrl3,
            String facilityImageUrl4, String logoUrl, String homepage,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        Organization org = new Organization();
        org.id = id;
        org.userId = userId;
        org.name = name;
        org.description = description;
        org.approvalStatus = approvalStatus;
        org.certificateUrl = certificateUrl;
        org.govAuth = govAuth;
        org.facilityImageUrl = facilityImageUrl;
        org.facilityImageUrl2 = facilityImageUrl2;
        org.facilityImageUrl3 = facilityImageUrl3;
        org.facilityImageUrl4 = facilityImageUrl4;
        org.logoUrl = logoUrl;
        org.homepage = homepage;
        org.createdAt = createdAt;
        org.updatedAt = updatedAt;
        return org;
    }

    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFacilityImages(String url1, String url2, String url3, String url4) {
        this.facilityImageUrl = url1;
        this.facilityImageUrl2 = url2;
        this.facilityImageUrl3 = url3;
        this.facilityImageUrl4 = url4;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void setGovAuth(String govAuth) {
        this.govAuth = govAuth;
        this.updatedAt = LocalDateTime.now();
    }
}
