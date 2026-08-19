package com.greentech.account.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "계정 임시 비밀번호 재설정 요청")
public record AccountPasswordResetReq(
        @Schema(description = "새 임시 비밀번호")
        @NotBlank(message = "임시 비밀번호는 필수입니다")
        @Size(min = 10, max = 100, message = "임시 비밀번호는 10자 이상이어야 합니다")
        String temporaryPassword) {
}
