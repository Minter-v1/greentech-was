package com.greentech.security;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import com.greentech.account.repository.AppRoleRepository;
import com.greentech.account.repository.AppUserRepository;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초기 로그인 계정 생성
 *
 * NOTE: BCrypt 해시를 시드 SQL 에 하드코딩하지 않기 위해 기동 시점에 생성
 * NOTE: 멱등 동작 - 이미 존재하는 username 은 건너뜀
 * NOTE: 볼륨 삭제로 DB 가 비어도 기동할 때마다 관리자 계정이 다시 만들어진다
 *       마이그레이션 INSERT 와 달리 계정을 실수로 지운 경우에도 복구된다
 * FIXME: 운영 배포 시 admin 비밀번호는 최초 로그인 후 반드시 변경 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBootstrapper implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SecurityProperties.Admin admin = properties.admin();
        createIfAbsent(admin.username(), admin.password(), null, List.of(AppRole.ADMIN));

        // NOTE: 데모·검증용 계정. 운영 반영 시 제거 대상
        String demoPassword = properties.bootstrapPassword();
        createIfAbsent("hr01", demoPassword, "20190201", List.of(AppRole.HR, AppRole.EMPLOYEE));
        createIfAbsent("mgr01", demoPassword, "20210401", List.of(AppRole.MANAGER, AppRole.EMPLOYEE));
        createIfAbsent("emp01", demoPassword, "20220501", List.of(AppRole.EMPLOYEE));
    }

    private void createIfAbsent(
            String username, String rawPassword, String empNo, List<String> roleCodes) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        Set<AppRole> roles = new LinkedHashSet<>();
        for (String code : roleCodes) {
            appRoleRepository.findByCode(code).ifPresent(roles::add);
        }
        if (roles.isEmpty()) {
            log.warn("권한 미존재로 계정 생성 생략: username={}, roles={}", username, roleCodes);
            return;
        }

        Employee employee = null;
        if (empNo != null) {
            employee = employeeRepository.findByEmpNo(empNo).orElse(null);
            if (employee == null) {
                log.warn("사원 미존재로 계정-사원 연결 생략: username={}, empNo={}", username, empNo);
            }
        }

        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .employee(employee)
                .enabled(true)
                .locked(false)
                .passwordChangedAt(LocalDateTime.now())
                .roles(roles)
                .build();

        appUserRepository.save(user);
        log.info("초기 계정 생성: username={}, roles={}, empNo={}", username, roleCodes, empNo);
    }
}
