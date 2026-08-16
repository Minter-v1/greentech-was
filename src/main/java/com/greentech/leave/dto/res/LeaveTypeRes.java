package com.greentech.leave.dto.res;

import com.greentech.leave.domain.LeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "휴가 종류")
public record LeaveTypeRes(
        @Schema(description = "휴가 종류 ID", example = "1")
        Long id,

        @Schema(description = "휴가코드", example = "ANNUAL")
        String code,

        @Schema(description = "휴가명", example = "연차")
        String name,

        @Schema(description = "유급 여부", example = "true")
        boolean paid,

        @Schema(description = "연차 잔여 차감 여부", example = "true")
        boolean deductAnnual,

        @Schema(description = "연간 최대 사용일")
        BigDecimal maxDaysPerYear) {

    public static LeaveTypeRes from(LeaveType entity) {
        return new LeaveTypeRes(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.isPaid(),
                entity.isDeductAnnual(),
                entity.getMaxDaysPerYear());
    }
}
