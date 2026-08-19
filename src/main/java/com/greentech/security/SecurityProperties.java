package com.greentech.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 보안 설정값 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        String fieldEncryptionKey,
        Admin admin) {

    /** 관리자 계정 */
    public record Admin(String username, String password, Boolean resetPasswordOnStart) {}
}
