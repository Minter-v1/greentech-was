package com.greentech.leave.dto.res;

import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.domain.OvertimeRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "연장근무 신청")
public record OvertimeRequestRes(
        @Schema(description = "신청 ID", example = "1")
        Long id,

        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "사원 성명", example = "오세훈")
        String employeeName,

        @Schema(description = "근무일", example = "2026-08-17")
        LocalDate workDate,

        @Schema(description = "시작 시각")
        LocalDateTime startAt,

        @Schema(description = "종료 시각")
        LocalDateTime endAt,

        @Schema(description = "연장 분", example = "150")
        int minutes,

        @Schema(description = "연장근무 구분", example = "EXTENDED")
        OvertimeRequest.OvertimeType overtimeType,

        @Schema(description = "사유")
        String reason,

        @Schema(description = "결재 상태", example = "REQUESTED")
        ApprovalStatus status,

        @Schema(description = "결재자 사원 ID")
        Long approverId,

        @Schema(description = "결재 시각")
        LocalDateTime approvedAt) {

    public static OvertimeRequestRes from(OvertimeRequest entity) {
        return new OvertimeRequestRes(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getEmployee().getName(),
                entity.getWorkDate(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getMinutes(),
                entity.getOvertimeType(),
                entity.getReason(),
                entity.getStatus(),
                entity.getApprover() != null ? entity.getApprover().getId() : null,
                entity.getApprovedAt());
    }
}
