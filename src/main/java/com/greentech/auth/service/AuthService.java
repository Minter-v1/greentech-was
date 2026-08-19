package com.greentech.auth.service;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import com.greentech.account.domain.AuditLog;
import com.greentech.account.repository.AppUserRepository;
import com.greentech.account.repository.AuditLogRepository;
import com.greentech.auth.dto.req.LoginReq;
import com.greentech.auth.dto.res.LoginRes;
import com.greentech.auth.dto.res.MeRes;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 및 현재 사용자 조회 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtService jwtService;

    @Transactional
    public LoginRes login(LoginReq request, String clientIp) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException e) {
            recordFailure(request.username(), clientIp);
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        } catch (DisabledException | LockedException e) {
            recordFailure(request.username(), clientIp);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS));

        JwtService.IssuedToken issued = jwtService.issue(user);
        user.recordSuccessfulLogin(LocalDateTime.now());

        auditLogRepository.save(AuditLog.builder()
                .actor(user.getUsername())
                .action("LOGIN")
                .targetType("APP_USER")
                .targetId(String.valueOf(user.getId()))
                .ip(clientIp)
                .build());

        Employee employee = user.getEmployee();
        return new LoginRes(
                issued.token(),
                "Bearer",
                issued.expiresInSeconds(),
                issued.expiresAt(),
                user.getUsername(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getName() : null,
                issued.roles());
    }

    @Transactional(readOnly = true)
    public MeRes me(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        List<String> roles = user.getRoles().stream().map(AppRole::getCode).toList();
        Employee employee = user.getEmployee();

        return new MeRes(
                user.getId(),
                user.getUsername(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getEmpNo() : null,
                employee != null ? employee.getName() : null,
                employee != null && employee.getDepartment() != null
                        ? employee.getDepartment().getName() : null,
                employee != null && employee.getJobPosition() != null
                        ? employee.getJobPosition().getName() : null,
                roles);
    }

    private void recordFailure(String username, String clientIp) {
        appUserRepository.findByUsername(username).ifPresent(AppUser::recordFailedLogin);
        auditLogRepository.save(AuditLog.builder()
                .actor(username)
                .action("LOGIN_FAILED")
                .targetType("APP_USER")
                .ip(clientIp)
                .build());
    }
}
