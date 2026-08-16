package com.greentech.attendance.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "월별 근태 집계")
public record AttendanceMonthlyRes(
        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "정산월", example = "2026-08")
        String yearMonth,

        @Schema(description = "출근 일수", example = "20")
        int workedDays,

        @Schema(description = "지각 일수", example = "1")
        int lateDays,

        @Schema(description = "결근 일수", example = "0")
        int absentDays,

        @Schema(description = "총 정규 근무 분", example = "9600")
        int totalWorkMinutes,

        @Schema(description = "총 연장 근무 분", example = "480")
        int totalOvertimeMinutes,

        @Schema(description = "총 야간 근무 분", example = "0")
        int totalNightMinutes,

        @Schema(description = "일별 상세")
        List<AttendanceRes> records) {
}
