package com.greentech.attachment.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

// TODO: 용량 증가 시 오브젝트 스토리지 전환 검토
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String root) {

    public StorageProperties {
        if (root == null || root.isBlank()) {
            root = "./uploads";
        }
    }
}
