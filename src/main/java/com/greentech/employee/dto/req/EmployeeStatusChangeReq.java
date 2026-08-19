package com.greentech.employee.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "휴직 및 복직 처리 요청")
public record EmployeeStatusChangeReq(
        @Schema(description = "발령일", example = "2026-09-01")
        @NotNull(message = "발령일은 필수입니다")
        LocalDate effectiveDate,

        @Schema(description = "사유", example = "육아휴직")
        @Size(max = 500)
        String reason) {
}
