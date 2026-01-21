package com.swcampus.infra.postgres.organization;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.infra.postgres.TestApplication;
import com.swcampus.infra.postgres.TestJpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestApplication.class)
@Import({OrganizationEntityRepository.class, TestJpaConfig.class})
@ActiveProfiles("test")
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    @DisplayName("Organization 저장 및 조회")
    void saveAndFindById() {
        // given
        Organization organization = Organization.create(1L, "소프트웨어 캠퍼스", "IT 교육 전문 기관", "https://s3.../cert.jpg");

        // when
        Organization saved = organizationRepository.save(organization);
        Optional<Organization> found = organizationRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("소프트웨어 캠퍼스");
        assertThat(found.get().getDescription()).isEqualTo("IT 교육 전문 기관");
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getCertificateKey()).isEqualTo("https://s3.../cert.jpg");
        assertThat(found.get().getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("userId로 Organization 조회")
    void findByUserId() {
        // given
        Organization organization = Organization.create(100L, "테스트 기관", "설명", "https://s3.../cert.jpg");
        organizationRepository.save(organization);

        // when
        Optional<Organization> found = organizationRepository.findByUserId(100L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("userId 존재 여부 확인")
    void existsByUserId() {
        // given
        Organization organization = Organization.create(200L, "테스트 기관", "설명", "https://s3.../cert.jpg");
        organizationRepository.save(organization);

        // when & then
        assertThat(organizationRepository.existsByUserId(200L)).isTrue();
        assertThat(organizationRepository.existsByUserId(999L)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 Organization 조회")
    void findByIdNotFound() {
        // when
        Optional<Organization> found = organizationRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }
}
