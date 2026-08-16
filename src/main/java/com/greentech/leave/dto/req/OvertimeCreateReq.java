package com.greentech.leave.dto.req;

import com.greentech.leave.domain.OvertimeRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "연장근무 신청 요청")
public record OvertimeCreateReq(
        @Schema(description = "시작 시각", example = "2026-08-17T18:00:00")
        @NotNull(message = "시작 시각은 필수입니다")
        LocalDateTime startAt,

        @Schema(description = "종료 시각", example = "2026-08-17T20:30:00")
        @NotNull(message = "종료 시각은 필수입니다")
        LocalDateTime endAt,

        @Schema(description = "연장근무 구분", example = "EXTENDED")
        @NotNull(message = "연장근무 구분은 필수입니다")
        OvertimeRequest.OvertimeType overtimeType,

        @Schema(description = "사유", example = "월말 정산 마감 대응")
        @Size(max = 500)
        String reason) {
}
