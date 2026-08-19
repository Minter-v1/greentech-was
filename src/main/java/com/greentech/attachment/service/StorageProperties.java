package com.greentech.attachment.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        Type type,
        String root,
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey) {

    public enum Type {
        LOCAL, NCP
    }
}
