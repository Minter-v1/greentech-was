package com.greentech.account.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(description = "계정 발급 요청")
public record AccountCreateReq(
        @Schema(description = "로그인 아이디", example = "minji.kim")
        @NotBlank(message = "아이디는 필수입니다")
        @Pattern(regexp = "^[A-Za-z0-9._-]{4,50}$",
                message = "아이디는 영문, 숫자, 점, 밑줄, 하이픈으로 4~50자여야 합니다")
        String username,

        @Schema(description = "임시 비밀번호")
        @NotBlank(message = "임시 비밀번호는 필수입니다")
        @Size(min = 10, max = 100, message = "임시 비밀번호는 10자 이상이어야 합니다")
        String temporaryPassword,

        @Schema(description = "연결할 사원 ID")
        Long employeeId,

        @Schema(description = "부여할 권한 코드")
        @NotEmpty(message = "권한을 하나 이상 선택해야 합니다")
        Set<String> roleCodes) {
}
