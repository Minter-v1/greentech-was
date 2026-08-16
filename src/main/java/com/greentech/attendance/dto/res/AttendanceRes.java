package com.greentech.attendance.dto.res;

import com.greentech.attendance.domain.Attendance;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "일별 출퇴근 기록")
public record AttendanceRes(
        @Schema(description = "근태 ID", example = "1")
        Long id,

        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "근무일", example = "2026-08-17")
        LocalDate workDate,

        @Schema(description = "출근 시각")
        LocalDateTime checkInAt,

        @Schema(description = "퇴근 시각")
        LocalDateTime checkOutAt,

        @Schema(description = "정규 근무 분", example = "480")
        int workMinutes,

        @Schema(description = "연장 근무 분", example = "60")
        int overtimeMinutes,

        @Schema(description = "야간 근무 분", example = "0")
        int nightMinutes,

        @Schema(description = "근태 상태", example = "NORMAL")
        Attendance.Status status,

        @Schema(description = "비고")
        String note) {

    public static AttendanceRes from(Attendance entity) {
        return new AttendanceRes(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getWorkDate(),
                entity.getCheckInAt(),
                entity.getCheckOutAt(),
                entity.getWorkMinutes(),
                entity.getOvertimeMinutes(),
                entity.getNightMinutes(),
                entity.getStatus(),
                entity.getNote());
    }
}
