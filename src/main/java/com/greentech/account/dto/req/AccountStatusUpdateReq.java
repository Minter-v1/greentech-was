package com.greentech.account.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계정 상태 변경 요청")
public record AccountStatusUpdateReq(
        @Schema(description = "사용 여부", example = "true")
        @NotNull(message = "사용 여부는 필수입니다")
        Boolean enabled,

        @Schema(description = "잠금 여부", example = "false")
        @NotNull(message = "잠금 여부는 필수입니다")
        Boolean locked) {
}
