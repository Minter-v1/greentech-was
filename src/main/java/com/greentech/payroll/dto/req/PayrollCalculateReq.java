package com.greentech.payroll.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Schema(description = "급여 정산 실행 요청")
public record PayrollCalculateReq(
        @Schema(description = "정산월", example = "2026-08")
        @NotBlank(message = "정산월은 필수입니다")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "정산월 형식은 YYYY-MM 입니다")
        String payYearMonth,

        @Schema(description = "지급일", example = "2026-09-10")
        LocalDate payDate) {
}
