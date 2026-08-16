package com.greentech.leave.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "휴가 신청 요청")
public record LeaveRequestCreateReq(
        @Schema(description = "휴가 종류 ID", example = "1")
        @NotNull(message = "휴가 종류는 필수입니다")
        Long leaveTypeId,

        @Schema(description = "시작일", example = "2026-08-20")
        @NotNull(message = "시작일은 필수입니다")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2026-08-21")
        @NotNull(message = "종료일은 필수입니다")
        LocalDate endDate,

        @Schema(description = "반차 여부 - true 면 시작일과 종료일이 같아야 함", example = "false")
        Boolean halfDay,

        @Schema(description = "사유", example = "개인 사정")
        @Size(max = 500)
        String reason) {
}
