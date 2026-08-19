package com.greentech.org.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "직위 부분 수정 요청")
public record JobPositionPatchReq(
        @Schema(description = "직위명", example = "상무")
        JsonNullable<@NotBlank(message = "직위명은 비울 수 없습니다") @Size(max = 50) String> name,

        @Schema(description = "직위 서열. 값이 클수록 상위", example = "8")
        JsonNullable<@Min(value = 1, message = "직위 서열은 1 이상이어야 합니다") Integer> levelNo,

        @Schema(description = "사용 여부", example = "true")
        JsonNullable<Boolean> active) {
}
