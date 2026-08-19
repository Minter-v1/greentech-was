package com.greentech.attachment.storage;

import com.greentech.attachment.service.StorageProperties;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "ncp")
public class NcpObjectStorage implements FileStorage {

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public StoredFile store(String objectKey, MultipartFile file) {
        String checksum = checksum(file);
        String contentType = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream");
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromContentProvider(
                    () -> openStream(file), file.getSize(), contentType));
            return new StoredFile(objectKey, checksum);
        } catch (SdkException | UncheckedIOException e) {
            log.error("NCP Object Storage 업로드 실패 bucket={} key={}", properties.bucket(), objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public Resource load(String objectKey) {
        try {
            ResponseInputStream<GetObjectResponse> input = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
            return new InputStreamResource(input);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND, "저장된 파일을 찾을 수 없습니다");
            }
            log.error("NCP Object Storage 조회 실패 bucket={} key={}", properties.bucket(), objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        } catch (SdkException e) {
            log.error("NCP Object Storage 연결 실패 bucket={} key={}", properties.bucket(), objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException e) {
            log.error("NCP Object Storage 삭제 실패 bucket={} key={}", properties.bucket(), objectKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private InputStream openStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String checksum(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.error("업로드 파일 체크섬 계산 실패 name={}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}
