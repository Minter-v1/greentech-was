package com.greentech.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 보안 설정값
 *
 * @param jwtSecret          JWT 서명용 HMAC 비밀키 - HS256 기준 최소 32바이트
 * @param accessTokenTtl     액세스 토큰 유효기간
 * @param fieldEncryptionKey 민감 컬럼 암호화용 Base64 AES 키
 * @param bootstrapPassword  최초 기동 시 생성되는 계정의 초기 비밀번호
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        String fieldEncryptionKey,
        String bootstrapPassword) {

    public SecurityProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofHours(8);
        }
    }
}
