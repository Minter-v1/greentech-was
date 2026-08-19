package com.greentech.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CORS 허용 출처 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS 환경변수가 필요합니다");
        }
        allowedOrigins = List.copyOf(allowedOrigins);
    }
}
