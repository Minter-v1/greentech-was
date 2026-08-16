package com.greentech.leave.dto.res;

import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.domain.LeaveRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "휴가 신청")
public record LeaveRequestRes(
        @Schema(description = "신청 ID", example = "1")
        Long id,

        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "사원 성명", example = "오세훈")
        String employeeName,

        @Schema(description = "휴가 종류 ID", example = "1")
        Long leaveTypeId,

        @Schema(description = "휴가명", example = "연차")
        String leaveTypeName,

        @Schema(description = "시작일", example = "2026-08-20")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2026-08-21")
        LocalDate endDate,

        @Schema(description = "신청일수", example = "2.0")
        BigDecimal days,

        @Schema(description = "반차 여부", example = "false")
        boolean halfDay,

        @Schema(description = "사유")
        String reason,

        @Schema(description = "결재 상태", example = "REQUESTED")
        ApprovalStatus status,

        @Schema(description = "결재자 사원 ID")
        Long approverId,

        @Schema(description = "결재 시각")
        LocalDateTime approvedAt,

        @Schema(description = "반려 사유")
        String rejectReason) {

    public static LeaveRequestRes from(LeaveRequest entity) {
        return new LeaveRequestRes(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getEmployee().getName(),
                entity.getLeaveType().getId(),
                entity.getLeaveType().getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDays(),
                entity.isHalfDay(),
                entity.getReason(),
                entity.getStatus(),
                entity.getApprover() != null ? entity.getApprover().getId() : null,
                entity.getApprovedAt(),
                entity.getRejectReason());
    }
}
