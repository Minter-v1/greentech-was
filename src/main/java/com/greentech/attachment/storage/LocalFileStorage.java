package com.greentech.attachment.storage;

import com.greentech.attachment.service.StorageProperties;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final StorageProperties properties;

    @Override
    public StoredFile store(String objectKey, MultipartFile file) {
        Path target = resolve(objectKey);
        MessageDigest digest = sha256();

        try {
            Files.createDirectories(target.getParent());
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredFile(objectKey, HexFormat.of().formatHex(digest.digest()));
        } catch (IOException e) {
            log.error("로컬 파일 저장 실패 key={}", objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public Resource load(String objectKey) {
        Path path = resolve(objectKey);
        if (!Files.exists(path)) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND, "저장된 파일을 찾을 수 없습니다");
        }
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패 key={}", objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private Path resolve(String objectKey) {
        Path root = Path.of(properties.root()).toAbsolutePath().normalize();
        Path resolved = root.resolve(objectKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR, "허용되지 않은 저장 경로입니다");
        }
        return resolved;
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다", e);
        }
    }
}
