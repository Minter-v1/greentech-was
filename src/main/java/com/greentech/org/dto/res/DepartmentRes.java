package com.greentech.org.dto.res;

import com.greentech.org.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "부서 정보")
public record DepartmentRes(
        @Schema(description = "부서 ID", example = "2")
        Long id,

        @Schema(description = "부서코드", example = "D110")
        String code,

        @Schema(description = "부서명", example = "인사팀")
        String name,

        @Schema(description = "상위 부서 ID", example = "1")
        Long parentId,

        @Schema(description = "정렬 순서", example = "11")
        int sortOrder,

        @Schema(description = "사용 여부", example = "true")
        boolean active) {

    public static DepartmentRes from(Department entity) {
        return new DepartmentRes(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getSortOrder(),
                entity.isActive());
    }
}
