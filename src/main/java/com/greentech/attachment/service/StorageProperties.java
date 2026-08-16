package com.greentech.attachment.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 첨부파일 스토리지 설정
 *
 * @param root 파일 저장 루트 경로
 *
 * NOTE: 현재는 로컬·볼륨 경로 사용
 * TODO: 첨부 40GB 증가 추세 고려 시 오브젝트 스토리지 전환 검토
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String root) {

    public StorageProperties {
        if (root == null || root.isBlank()) {
            root = "./uploads";
        }
    }
}
