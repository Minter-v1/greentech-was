package com.greentech.payroll.dto.res;

import com.greentech.payroll.domain.PayrollRun;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "급여 정산 실행 정보")
public record PayrollRunRes(
        @Schema(description = "정산 ID", example = "1")
        Long id,

        @Schema(description = "정산월", example = "2026-08")
        String payYearMonth,

        @Schema(description = "정산 상태", example = "CALCULATED")
        PayrollRun.Status status,

        @Schema(description = "지급일", example = "2026-09-10")
        LocalDate payDate,

        @Schema(description = "대상 인원", example = "10")
        int targetCount,

        @Schema(description = "지급 합계", example = "48200000")
        BigDecimal totalGross,

        @Schema(description = "공제 합계", example = "6100000")
        BigDecimal totalDeduction,

        @Schema(description = "실지급 합계", example = "42100000")
        BigDecimal totalNet,

        @Schema(description = "실행자", example = "hr01")
        String executedBy,

        @Schema(description = "실행 시각")
        LocalDateTime executedAt,

        @Schema(description = "확정 시각")
        LocalDateTime confirmedAt) {

    public static PayrollRunRes from(PayrollRun entity) {
        return new PayrollRunRes(
                entity.getId(),
                entity.getPayYearMonth(),
                entity.getStatus(),
                entity.getPayDate(),
                entity.getTargetCount(),
                entity.getTotalGross(),
                entity.getTotalDeduction(),
                entity.getTotalNet(),
                entity.getExecutedBy(),
                entity.getExecutedAt(),
                entity.getConfirmedAt());
    }
}
