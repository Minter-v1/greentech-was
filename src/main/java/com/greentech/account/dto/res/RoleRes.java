package com.greentech.account.dto.res;

import com.greentech.account.domain.AppRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시스템 권한")
public record RoleRes(
        String code,
        String name,
        String description) {

    public static RoleRes from(AppRole role) {
        return new RoleRes(role.getCode(), role.getName(), role.getDescription());
    }
}
