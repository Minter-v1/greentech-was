package com.greentech.account.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

@Schema(description = "계정 권한 변경 요청")
public record AccountRolesUpdateReq(
        @Schema(description = "부여할 권한 코드")
        @NotEmpty(message = "권한을 하나 이상 선택해야 합니다")
        Set<String> roleCodes) {
}
