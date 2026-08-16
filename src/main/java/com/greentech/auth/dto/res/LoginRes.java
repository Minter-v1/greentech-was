package com.greentech.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "로그인 응답")
public record LoginRes(
        @Schema(description = "액세스 토큰")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "만료까지 남은 초", example = "28800")
        long expiresIn,

        @Schema(description = "만료 시각")
        Instant expiresAt,

        @Schema(description = "계정 아이디", example = "admin")
        String username,

        @Schema(description = "연결된 사원 ID", example = "2")
        Long employeeId,

        @Schema(description = "연결된 사원 성명", example = "박은주")
        String employeeName,

        @Schema(description = "권한 목록")
        List<String> roles) {
}
