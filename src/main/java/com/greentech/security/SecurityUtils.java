package com.greentech.security;

import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** 인증 주체에서 사용자·사원 식별자 추출 헬퍼 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName();
    }

    /** 현재 로그인 계정에 연결된 사원 ID */
    public static Long currentEmployeeId() {
        Long employeeId = currentEmployeeIdOrNull();
        if (employeeId == null) {
            throw new BusinessException(ErrorCode.NO_LINKED_EMPLOYEE);
        }
        return employeeId;
    }

    public static Long currentEmployeeIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        Object claim = jwt.getClaim(JwtService.CLAIM_EMPLOYEE_ID);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public static boolean hasRole(String roleCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> roleCode.equals(granted.getAuthority()));
    }
}
