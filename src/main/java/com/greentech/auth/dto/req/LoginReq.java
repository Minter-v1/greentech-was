package com.greentech.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginReq(
        @Schema(description = "계정 아이디", example = "admin")
        @NotBlank(message = "아이디는 필수입니다")
        @Size(max = 50)
        String username,

        @Schema(description = "비밀번호", example = "********")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(max = 100)
        String password) {
}
