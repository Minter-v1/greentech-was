package com.greentech.security;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import com.greentech.account.repository.AppRoleRepository;
import com.greentech.account.repository.AppUserRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 계정 생성. 없으면 생성, 있으면 유지 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBootstrapper implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SecurityProperties.Admin admin = properties.admin();

        // NOTE: 계정이 있으면 비밀번호를 덮지 않는다. 앱에서 바꾼 값이 재기동마다 되돌아가기 때문
        //       설정값으로 강제 초기화하려면 reset-password-on-start 를 켠다
        var existing = appUserRepository.findByUsername(admin.username());
        if (existing.isPresent()) {
            if (Boolean.TRUE.equals(admin.resetPasswordOnStart())) {
                AppUser user = existing.get();
                user.setPasswordHash(passwordEncoder.encode(admin.password()));
                user.setPasswordChangedAt(LocalDateTime.now());
                log.warn("관리자 비밀번호를 설정값으로 초기화: username={}", admin.username());
            }
            return;
        }

        Set<AppRole> roles = new LinkedHashSet<>();
        appRoleRepository.findByCode(AppRole.ADMIN).ifPresent(roles::add);
        if (roles.isEmpty()) {
            log.warn("ROLE_ADMIN 미존재로 관리자 계정 생성 생략");
            return;
        }

        appUserRepository.save(AppUser.builder()
                .username(admin.username())
                .passwordHash(passwordEncoder.encode(admin.password()))
                .enabled(true)
                .locked(false)
                .passwordChangedAt(LocalDateTime.now())
                .roles(roles)
                .build());

        log.info("관리자 계정 생성: username={}", admin.username());
    }
}
