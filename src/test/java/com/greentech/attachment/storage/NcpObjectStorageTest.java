package com.greentech.attachment.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.greentech.attachment.service.StorageProperties;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class NcpObjectStorageTest {

    @Mock
    private S3Client s3Client;

    private NcpObjectStorage storage;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties(
                StorageProperties.Type.NCP,
                null,
                "https://kr.object.private.ncloudstorage.com",
                "kr-standard",
                "greentech-files",
                "access-key",
                "secret-key");
        storage = new NcpObjectStorage(s3Client, properties);
    }

    @Test
    void storeWithBucketAndObjectKey() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", "profile-image".getBytes(StandardCharsets.UTF_8));

        FileStorage.StoredFile stored = storage.store("2026/08/19/profile.png", file);

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("greentech-files");
        assertThat(request.getValue().key()).isEqualTo("2026/08/19/profile.png");
        assertThat(request.getValue().contentType()).isEqualTo("image/png");
        assertThat(stored.checksum()).hasSize(64);
    }

    @Test
    void mapMissingObjectToAttachmentNotFound() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());

        assertThatThrownBy(() -> storage.load("missing.png"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ATTACHMENT_NOT_FOUND));
    }
}
