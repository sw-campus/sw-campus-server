package com.swcampus.domain.organization;

import com.swcampus.domain.organization.dto.UpdateOrganizationParams;
import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Nested
    @DisplayName("업체 정보 수정")
    class UpdateOrganizationTest {

        @Test
        @DisplayName("정보와 사업자등록증을 함께 수정한다")
        void updateOrganization_withImage() {
            // given
            Long orgId = 1L;
            Long userId = 1L;
            Organization org = Organization.create(userId, "Old Name", "Old Desc", "old_cert.jpg");
            String newName = "New Name";
            String newDesc = "New Desc";
            byte[] fileContent = "image data".getBytes();
            String fileName = "new_cert.jpg";
            String contentType = "image/jpeg";
            String newCertUrl = "http://s3/new_cert.jpg";

            given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));
            given(fileStorageService.uploadPrivate(any(), anyString(), anyString(), anyString())).willReturn(newCertUrl);
            given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

            UpdateOrganizationParams params = new UpdateOrganizationParams(
                newName, newDesc, null, null, 
                new UpdateOrganizationParams.FileUploadData(fileContent, fileName, contentType),
                null, null, null, null, null
            );

            // when
            Organization result = organizationService.updateOrganization(orgId, userId, params);

            // then
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getCertificateUrl()).isEqualTo(newCertUrl);
            verify(fileStorageService).uploadPrivate(eq(fileContent), eq("certificates"), eq(fileName), eq(contentType));
        }

        @Test
        @DisplayName("이미지 없이 정보만 수정한다")
        void updateOrganization_withoutImage() {
            // given
            Long orgId = 1L;
            Long userId = 1L;
            Organization org = Organization.create(userId, "Old Name", "Old Desc", "cert.jpg");
            String newName = "New Name";
            String newDesc = "New Desc";

            given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));
            given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

            UpdateOrganizationParams params = createDefaultUpdateParams(newName, newDesc);

            // when
            Organization result = organizationService.updateOrganization(orgId, userId, params);

            // then
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getCertificateUrl()).isEqualTo("cert.jpg"); // 기존 인증서 유지
        }

        @Test
        @DisplayName("권한 없는 사용자가 수정 시 예외 발생")
        void updateOrganization_accessDenied_throwsException() {
            // given
            Long orgId = 1L;
            Long userId = 1L;
            Long otherUserId = 2L;
            Organization org = Organization.create(userId, "Old Name", "Old Desc", "cert.jpg");
            String newName = "New Name";
            String newDesc = "New Desc";

            given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));

            UpdateOrganizationParams params = createDefaultUpdateParams(newName, newDesc);

            // when & then
            assertThatThrownBy(() -> organizationService.updateOrganization(orgId, otherUserId, params))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("존재하지 않는 업체 수정 시 예외 발생")
        void updateOrganization_notFound_throwsException() {
            // given
            Long orgId = 999L;
            Long userId = 1L;
            String newName = "New Name";
            String newDesc = "New Desc";

            given(organizationRepository.findById(orgId)).willReturn(Optional.empty());

            UpdateOrganizationParams params = createDefaultUpdateParams(newName, newDesc);

            // when & then
            assertThatThrownBy(() -> organizationService.updateOrganization(orgId, userId, params))
                    .isInstanceOf(com.swcampus.domain.common.ResourceNotFoundException.class);
        }

        private UpdateOrganizationParams createDefaultUpdateParams(String name, String description) {
            return new UpdateOrganizationParams(name, description, null, null, null, null, null, null, null, null);
        }
    }
}
