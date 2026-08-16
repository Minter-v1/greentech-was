package com.greentech.leave.dto.res;

import com.greentech.leave.domain.LeaveBalance;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "휴가 잔여")
public record LeaveBalanceRes(
        @Schema(description = "잔여 ID", example = "1")
        Long id,

        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "휴가 종류 ID", example = "1")
        Long leaveTypeId,

        @Schema(description = "휴가명", example = "연차")
        String leaveTypeName,

        @Schema(description = "기준 연도", example = "2026")
        int year,

        @Schema(description = "부여일수", example = "15.0")
        BigDecimal grantedDays,

        @Schema(description = "사용일수", example = "4.0")
        BigDecimal usedDays,

        @Schema(description = "잔여일수", example = "11.0")
        BigDecimal remainingDays,

        @Schema(description = "소멸일", example = "2026-12-31")
        LocalDate expiresOn) {

    public static LeaveBalanceRes from(LeaveBalance entity) {
        return new LeaveBalanceRes(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getLeaveType().getId(),
                entity.getLeaveType().getName(),
                entity.getYear(),
                entity.getGrantedDays(),
                entity.getUsedDays(),
                entity.getRemainingDays(),
                entity.getExpiresOn());
    }
}
