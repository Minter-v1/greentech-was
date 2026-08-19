package com.greentech.account.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "계정과 사원 연결 요청")
public record AccountEmployeeLinkReq(
        @Schema(description = "연결할 사원 ID. null 전송 시 연결 해제", example = "1")
        JsonNullable<Long> employeeId) {
}
