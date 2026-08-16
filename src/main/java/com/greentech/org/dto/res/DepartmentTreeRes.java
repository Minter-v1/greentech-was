package com.greentech.org.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "부서 계층 구조")
public record DepartmentTreeRes(
        @Schema(description = "부서 ID", example = "1")
        Long id,

        @Schema(description = "부서코드", example = "D100")
        String code,

        @Schema(description = "부서명", example = "경영지원본부")
        String name,

        @Schema(description = "사용 여부", example = "true")
        boolean active,

        @Schema(description = "하위 부서")
        List<DepartmentTreeRes> children) {

    public static DepartmentTreeRes of(DepartmentRes source) {
        return new DepartmentTreeRes(
                source.id(), source.code(), source.name(), source.active(), new ArrayList<>());
    }
}
