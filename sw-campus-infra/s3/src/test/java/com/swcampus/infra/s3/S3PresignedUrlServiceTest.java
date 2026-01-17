package com.swcampus.infra.s3;

import com.swcampus.domain.member.Role;
import com.swcampus.domain.storage.PresignedUrlService.PresignedUploadUrl;
import com.swcampus.domain.storage.PresignedUrlService.PresignedUrl;
import com.swcampus.domain.storage.exception.InvalidContentTypeException;
import com.swcampus.domain.storage.exception.InvalidStorageCategoryException;
import com.swcampus.domain.storage.exception.StorageAccessDeniedException;
import com.swcampus.domain.storage.exception.StorageBatchLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("S3PresignedUrlService 테스트")
class S3PresignedUrlServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    private S3PresignedUrlService service;

    @BeforeEach
    void setUp() throws MalformedURLException {
        service = new S3PresignedUrlService(s3Presigner);
        ReflectionTestUtils.setField(service, "publicBucket", "public-bucket");
        ReflectionTestUtils.setField(service, "privateBucket", "private-bucket");

        // Mock presigned GET URL
        PresignedGetObjectRequest mockGetResponse = mock(PresignedGetObjectRequest.class);
        when(mockGetResponse.url()).thenReturn(new URL("https://s3.amazonaws.com/test-presigned-url"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockGetResponse);

        // Mock presigned PUT URL
        PresignedPutObjectRequest mockPutResponse = mock(PresignedPutObjectRequest.class);
        when(mockPutResponse.url()).thenReturn(new URL("https://s3.amazonaws.com/test-upload-url"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPutResponse);
    }

    @Nested
    @DisplayName("getPresignedUrl")
    class GetPresignedUrl {

        @Test
        @DisplayName("Public key는 누구나 접근 가능하다")
        void publicKey_success() {
            // given
            String publicKey = "lectures/2024/01/01/uuid.jpg";

            // when
            PresignedUrl result = service.getPresignedUrl(publicKey, false);

            // then
            assertThat(result.url()).isNotNull();
            assertThat(result.expiresIn()).isEqualTo(900); // 15분 = 900초
        }

        @Test
        @DisplayName("Private key는 관리자만 접근 가능하다")
        void privateKey_admin_success() {
            // given
            String privateKey = "certificates/2024/01/01/uuid.jpg";

            // when
            PresignedUrl result = service.getPresignedUrl(privateKey, true);

            // then
            assertThat(result.url()).isNotNull();
        }

        @Test
        @DisplayName("Private key에 비관리자가 접근하면 예외가 발생한다")
        void privateKey_nonAdmin_throwsException() {
            // given
            String privateKey = "certificates/2024/01/01/uuid.jpg";

            // when & then
            assertThatThrownBy(() -> service.getPresignedUrl(privateKey, false))
                    .isInstanceOf(StorageAccessDeniedException.class);
        }

        @Test
        @DisplayName("employment-certificates도 Private key로 처리된다")
        void employmentCertificates_isPrivate() {
            // given
            String privateKey = "employment-certificates/2024/01/01/uuid.pdf";

            // when & then
            assertThatThrownBy(() -> service.getPresignedUrl(privateKey, false))
                    .isInstanceOf(StorageAccessDeniedException.class);
        }

        @Test
        @DisplayName("members도 Private key로 처리된다")
        void members_isPrivate() {
            // given
            String privateKey = "members/2024/01/01/uuid.jpg";

            // when & then
            assertThatThrownBy(() -> service.getPresignedUrl(privateKey, false))
                    .isInstanceOf(StorageAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getPresignedUrls (배치)")
    class GetPresignedUrls {

        @Test
        @DisplayName("50개 이하의 요청은 성공한다")
        void withinLimit_success() {
            // given
            List<String> keys = List.of(
                    "lectures/2024/01/01/uuid1.jpg",
                    "lectures/2024/01/01/uuid2.jpg"
            );

            // when
            Map<String, String> result = service.getPresignedUrls(keys, false);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.values()).allMatch(url -> url != null);
        }

        @Test
        @DisplayName("50개 초과 요청은 예외가 발생한다")
        void exceedsLimit_throwsException() {
            // given
            List<String> keys = IntStream.range(0, 51)
                    .mapToObj(i -> "lectures/2024/01/01/uuid" + i + ".jpg")
                    .toList();

            // when & then
            assertThatThrownBy(() -> service.getPresignedUrls(keys, false))
                    .isInstanceOf(StorageBatchLimitExceededException.class);
        }

        @Test
        @DisplayName("Mixed keys에서 비관리자는 Private key에 대해 null을 받는다")
        void mixedKeys_nonAdmin_privateKeysAreNull() {
            // given
            List<String> keys = List.of(
                    "lectures/2024/01/01/public.jpg",
                    "certificates/2024/01/01/private.jpg",
                    "banners/2024/01/01/public2.jpg"
            );

            // when
            Map<String, String> result = service.getPresignedUrls(keys, false);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get("lectures/2024/01/01/public.jpg")).isNotNull();
            assertThat(result.get("certificates/2024/01/01/private.jpg")).isNull();
            assertThat(result.get("banners/2024/01/01/public2.jpg")).isNotNull();
        }

        @Test
        @DisplayName("관리자는 모든 키에 대해 URL을 받는다")
        void mixedKeys_admin_allUrlsReturned() {
            // given
            List<String> keys = List.of(
                    "lectures/2024/01/01/public.jpg",
                    "certificates/2024/01/01/private.jpg"
            );

            // when
            Map<String, String> result = service.getPresignedUrls(keys, true);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.values()).allMatch(url -> url != null);
        }
    }

    @Nested
    @DisplayName("getPresignedUploadUrl")
    class GetPresignedUploadUrl {

        @Test
        @DisplayName("유효한 카테고리로 업로드 URL을 발급받는다")
        void validCategory_success() {
            // given
            String category = "lectures";
            String fileName = "test.jpg";
            String contentType = "image/jpeg";

            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl(category, fileName, contentType, Role.ORGANIZATION);

            // then
            assertThat(result.uploadUrl()).isNotNull();
            assertThat(result.key()).startsWith("lectures/");
            assertThat(result.key()).endsWith(".jpg");
            assertThat(result.expiresIn()).isEqualTo(900);
        }

        @Test
        @DisplayName("certificates 카테고리로 업로드 URL을 발급받는다")
        void certificates_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("certificates", "cert.jpg", "image/jpeg", Role.USER);

            // then
            assertThat(result.key()).startsWith("certificates/");
        }

        @Test
        @DisplayName("employment-certificates 카테고리로 업로드 URL을 발급받는다")
        void employmentCertificates_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("employment-certificates", "doc.png", "image/png", Role.USER);

            // then
            assertThat(result.key()).startsWith("employment-certificates/");
        }

        @Test
        @DisplayName("잘못된 카테고리는 예외가 발생한다")
        void invalidCategory_throwsException() {
            // when & then
            assertThatThrownBy(() -> service.getPresignedUploadUrl("invalid-category", "test.jpg", "image/jpeg", Role.USER))
                    .isInstanceOf(InvalidStorageCategoryException.class);
        }

        @Test
        @DisplayName("생성된 key는 UUID를 포함한다")
        void generatedKey_containsUuid() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("lectures", "original-name.jpg", "image/jpeg", Role.ORGANIZATION);

            // then
            String key = result.key();
            // key format: {category}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
            String[] parts = key.split("/");
            assertThat(parts).hasSize(5);
            assertThat(parts[0]).isEqualTo("lectures");
            // UUID is 36 characters + extension
            String fileName = parts[4];
            assertThat(fileName).matches("[a-f0-9-]{36}\\.jpg");
        }

        @Test
        @DisplayName("허용되지 않는 contentType은 예외가 발생한다")
        void invalidContentType_throwsException() {
            // when & then
            assertThatThrownBy(() -> service.getPresignedUploadUrl("lectures", "test.pdf", "application/pdf", Role.ORGANIZATION))
                    .isInstanceOf(InvalidContentTypeException.class);
        }

        @Test
        @DisplayName("null contentType은 예외가 발생한다")
        void nullContentType_throwsException() {
            // when & then
            assertThatThrownBy(() -> service.getPresignedUploadUrl("lectures", "test.jpg", null, Role.ORGANIZATION))
                    .isInstanceOf(InvalidContentTypeException.class);
        }

        @Test
        @DisplayName("image/jpeg contentType은 허용된다")
        void jpegContentType_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("lectures", "test.jpg", "image/jpeg", Role.ORGANIZATION);

            // then
            assertThat(result.uploadUrl()).isNotNull();
        }

        @Test
        @DisplayName("image/png contentType은 허용된다")
        void pngContentType_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("lectures", "test.png", "image/png", Role.ORGANIZATION);

            // then
            assertThat(result.uploadUrl()).isNotNull();
        }

        @Test
        @DisplayName("image/webp contentType은 허용된다")
        void webpContentType_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("lectures", "test.webp", "image/webp", Role.ORGANIZATION);

            // then
            assertThat(result.uploadUrl()).isNotNull();
        }

        @Test
        @DisplayName("image/gif contentType은 허용된다")
        void gifContentType_success() {
            // when
            PresignedUploadUrl result = service.getPresignedUploadUrl("lectures", "test.gif", "image/gif", Role.ORGANIZATION);

            // then
            assertThat(result.uploadUrl()).isNotNull();
        }
    }
}
