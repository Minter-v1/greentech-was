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

    public StorageProperties {
        if (type == null) {
            type = Type.LOCAL;
        }
        if (root == null || root.isBlank()) {
            root = "./uploads";
        }
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://kr.object.private.ncloudstorage.com";
        }
        if (region == null || region.isBlank()) {
            region = "kr-standard";
        }
    }

    public enum Type {
        LOCAL, NCP
    }
}
