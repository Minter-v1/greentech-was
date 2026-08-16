package com.greentech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * greentech-was 인사관리시스템 백엔드 엔트리 포인트
 *
 * NOTE: JPA Auditing 활성화 위치는 common.config.JpaAuditingConfig
 */
@ConfigurationPropertiesScan
@SpringBootApplication
public class GreentechApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreentechApplication.class, args);
    }
}
