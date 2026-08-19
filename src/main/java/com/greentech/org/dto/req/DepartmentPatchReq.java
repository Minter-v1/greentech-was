package com.greentech.org.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "부서 부분 수정 요청")
public record DepartmentPatchReq(
        @Schema(description = "부서명", example = "해외영업2팀")
        JsonNullable<@NotBlank(message = "부서명은 비울 수 없습니다") @Size(max = 100) String> name,

        @Schema(description = "상위 부서 ID. null 전송 시 최상위로 변경", example = "8")
        JsonNullable<Long> parentId,

        @Schema(description = "정렬 순서", example = "33")
        JsonNullable<@PositiveOrZero Integer> sortOrder,

        @Schema(description = "사용 여부", example = "true")
        JsonNullable<Boolean> active) {
}
