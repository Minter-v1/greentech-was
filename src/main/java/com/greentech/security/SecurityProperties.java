package com.greentech.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 보안 설정값
 *
 * @param jwtSecret          JWT 서명용 HMAC 비밀키 - HS256 기준 최소 32바이트
 * @param accessTokenTtl     액세스 토큰 유효기간
 * @param fieldEncryptionKey 민감 컬럼 암호화용 Base64 AES 키
 * @param bootstrapPassword  데모용 계정(hr01/mgr01/emp01)의 초기 비밀번호
 * @param admin              관리자 계정 설정
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        String fieldEncryptionKey,
        String bootstrapPassword,
        Admin admin) {

    public SecurityProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofHours(8);
        }
    }

    /**
     * 관리자 계정
     *
     * NOTE: 값은 환경변수로만 주입한다
     *       BCrypt 해시를 마이그레이션 SQL 에 넣으면 자격증명이 저장소에 남고
     *       마이그레이션은 1회만 실행되어 계정 삭제 시 복구되지 않는다
     *
     * @param username 관리자 계정 아이디
     * @param password 관리자 초기 비밀번호 - 기동 시 BCrypt 해싱되어 저장
     */
    public record Admin(String username, String password) {
    }
}
