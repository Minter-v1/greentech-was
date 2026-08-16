package com.greentech.leave.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "휴가 반려 요청")
public record LeaveRejectReq(
        @Schema(description = "반려 사유", example = "해당 기간 생산 일정 집중")
        @NotBlank(message = "반려 사유는 필수입니다")
        @Size(max = 500)
        String rejectReason) {
}
