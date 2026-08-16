package com.greentech.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "현재 로그인 사용자 정보")
public record MeRes(
        @Schema(description = "계정 ID", example = "1")
        Long userId,

        @Schema(description = "계정 아이디", example = "hr01")
        String username,

        @Schema(description = "연결된 사원 ID", example = "2")
        Long employeeId,

        @Schema(description = "사번", example = "20190201")
        String empNo,

        @Schema(description = "성명", example = "박은주")
        String name,

        @Schema(description = "부서명", example = "인사팀")
        String departmentName,

        @Schema(description = "직위명", example = "부장")
        String positionName,

        @Schema(description = "권한 목록")
        List<String> roles) {
}
