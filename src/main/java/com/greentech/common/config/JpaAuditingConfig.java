package com.greentech.common.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** created_by·updated_by 를 현재 로그인 사용자로 주입 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    private static final String SYSTEM = "system";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(SYSTEM);
            }
            String name = authentication.getName();
            // NOTE: 미인증 요청의 principal 은 anonymousUser
            if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
                return Optional.of(SYSTEM);
            }
            return Optional.of(name);
        };
    }
}
