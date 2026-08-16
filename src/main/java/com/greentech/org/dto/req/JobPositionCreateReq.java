package com.greentech.org.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "직위 등록 요청")
public record JobPositionCreateReq(
        @Schema(description = "직위코드", example = "P80")
        @NotBlank(message = "직위코드는 필수입니다")
        @Size(max = 20)
        String code,

        @Schema(description = "직위명", example = "상무")
        @NotBlank(message = "직위명은 필수입니다")
        @Size(max = 50)
        String name,

        @Schema(description = "직위 서열 - 값이 클수록 상위", example = "8")
        @NotNull(message = "직위 서열은 필수입니다")
        @Min(value = 1, message = "직위 서열은 1 이상이어야 합니다")
        Integer levelNo) {
}
