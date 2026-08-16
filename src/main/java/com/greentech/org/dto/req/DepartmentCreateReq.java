package com.greentech.org.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "부서 등록 요청")
public record DepartmentCreateReq(
        @Schema(description = "부서코드", example = "D330")
        @NotBlank(message = "부서코드는 필수입니다")
        @Size(max = 20)
        String code,

        @Schema(description = "부서명", example = "해외영업2팀")
        @NotBlank(message = "부서명은 필수입니다")
        @Size(max = 100)
        String name,

        @Schema(description = "상위 부서 ID", example = "8")
        Long parentId,

        @Schema(description = "정렬 순서", example = "33")
        @PositiveOrZero
        Integer sortOrder) {
}
