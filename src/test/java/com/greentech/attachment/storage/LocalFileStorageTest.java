package com.greentech.attachment.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.greentech.attachment.service.StorageProperties;
import com.greentech.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageTest {

    @TempDir
    Path root;

    @Test
    void storeLoadAndDelete() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(properties());
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", "profile-image".getBytes(StandardCharsets.UTF_8));

        FileStorage.StoredFile stored = storage.store("2026/08/19/profile.png", file);

        assertThat(stored.objectKey()).isEqualTo("2026/08/19/profile.png");
        assertThat(stored.checksum()).hasSize(64);
        assertThat(storage.load(stored.objectKey()).getContentAsByteArray())
                .isEqualTo(file.getBytes());

        storage.delete(stored.objectKey());

        assertThat(Files.exists(root.resolve(stored.objectKey()))).isFalse();
    }

    @Test
    void rejectPathTraversal() {
        LocalFileStorage storage = new LocalFileStorage(properties());
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", new byte[] {1});

        assertThatThrownBy(() -> storage.store("../profile.png", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("허용되지 않은 저장 경로");
    }

    private StorageProperties properties() {
        return new StorageProperties(
                StorageProperties.Type.LOCAL,
                root.toString(),
                null,
                null,
                null,
                null,
                null);
    }
}
