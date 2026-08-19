package com.greentech.account.dto.res;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "로그인 계정")
public record AccountRes(
        @Schema(description = "계정 ID", example = "1")
        Long id,

        @Schema(description = "계정 아이디", example = "greentech_admin")
        String username,

        @Schema(description = "연결된 사원 ID", example = "1")
        Long employeeId,

        @Schema(description = "연결된 사원 성명", example = "김성호")
        String employeeName,

        @Schema(description = "사용 여부", example = "true")
        boolean enabled,

        @Schema(description = "잠금 여부", example = "false")
        boolean locked,

        @Schema(description = "권한 목록")
        List<String> roles,

        @Schema(description = "마지막 로그인 시각")
        LocalDateTime lastLoginAt) {

    public static AccountRes from(AppUser user) {
        return new AccountRes(
                user.getId(),
                user.getUsername(),
                user.getEmployee() != null ? user.getEmployee().getId() : null,
                user.getEmployee() != null ? user.getEmployee().getName() : null,
                user.isEnabled(),
                user.isLocked(),
                user.getRoles().stream().map(AppRole::getCode).toList(),
                user.getLastLoginAt());
    }
}
