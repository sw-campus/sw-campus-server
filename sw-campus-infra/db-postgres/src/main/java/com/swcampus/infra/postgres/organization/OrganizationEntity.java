package com.swcampus.infra.postgres.organization;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "organizations")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_seq")
    @SequenceGenerator(name = "organizations_seq", sequenceName = "organizations_org_id_seq", allocationSize = 1)
    @Column(name = "org_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "org_name")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "certificate_url", columnDefinition = "TEXT")  // TODO: DB 마이그레이션 후 certificate_key로 변경
    private String certificateKey;

    @Column(name = "gov_auth", length = 100)
    private String govAuth;

    @Column(name = "facility_image_url", columnDefinition = "TEXT")
    private String facilityImageUrl;

    @Column(name = "facility_image_url2", columnDefinition = "TEXT")
    private String facilityImageUrl2;

    @Column(name = "facility_image_url3", columnDefinition = "TEXT")
    private String facilityImageUrl3;

    @Column(name = "facility_image_url4", columnDefinition = "TEXT")
    private String facilityImageUrl4;

    @Column(name = "org_logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "homepage", columnDefinition = "TEXT")
    private String homepage;

    public static OrganizationEntity from(Organization organization) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.id = organization.getId();
        entity.userId = organization.getUserId();
        entity.name = organization.getName();
        entity.description = organization.getDescription();
        entity.approvalStatus = organization.getApprovalStatus();
        entity.certificateKey = organization.getCertificateKey();
        entity.govAuth = organization.getGovAuth();
        entity.facilityImageUrl = organization.getFacilityImageUrl();
        entity.facilityImageUrl2 = organization.getFacilityImageUrl2();
        entity.facilityImageUrl3 = organization.getFacilityImageUrl3();
        entity.facilityImageUrl4 = organization.getFacilityImageUrl4();
        entity.logoUrl = organization.getLogoUrl();
        entity.homepage = organization.getHomepage();
        return entity;
    }

    public Organization toDomain() {
        return Organization.of(
                id,
                userId,
                name,
                description,
                approvalStatus,
                certificateKey,
                govAuth,
                facilityImageUrl,
                facilityImageUrl2,
                facilityImageUrl3,
                facilityImageUrl4,
                logoUrl,
                homepage,
                getCreatedAt(),
                getUpdatedAt());
    }
}
