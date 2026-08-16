package com.greentech.employee.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "퇴사 처리 요청")
public record EmployeeResignReq(
        @Schema(description = "퇴사일", example = "2026-08-31")
        @NotNull(message = "퇴사일은 필수입니다")
        LocalDate resignDate,

        @Schema(description = "퇴사 사유", example = "개인 사정")
        @Size(max = 500)
        String reason) {
}
