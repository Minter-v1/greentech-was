package com.greentech.attachment.config;

import com.greentech.attachment.service.StorageProperties;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "ncp")
public class ObjectStorageConfig {

    @Bean
    public S3Client objectStorageClient(StorageProperties properties) {
        requireSettings(properties);
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .forcePathStyle(true)
                .build();
    }

    private void requireSettings(StorageProperties properties) {
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            throw new IllegalStateException("NCP_STORAGE_BUCKET 환경변수가 필요합니다");
        }
        if (properties.accessKey() == null || properties.accessKey().isBlank()) {
            throw new IllegalStateException("NCP_STORAGE_ACCESS_KEY 환경변수가 필요합니다");
        }
        if (properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("NCP_STORAGE_SECRET_KEY 환경변수가 필요합니다");
        }
    }
}
