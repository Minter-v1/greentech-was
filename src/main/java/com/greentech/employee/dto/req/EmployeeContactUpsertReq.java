package com.greentech.employee.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "사원 연락처 등록·수정 요청")
public record EmployeeContactUpsertReq(
        @Schema(description = "휴대전화", example = "010-1234-5678")
        @Size(max = 30)
        String mobile,

        @Schema(description = "유선전화", example = "031-123-4567")
        @Size(max = 30)
        String tel,

        @Schema(description = "우편번호", example = "13529")
        @Size(max = 10)
        String zipCode,

        @Schema(description = "주소", example = "경기도 성남시 분당구 판교로 100")
        @Size(max = 200)
        String address1,

        @Schema(description = "상세 주소", example = "101동 1001호")
        @Size(max = 200)
        String address2,

        @Schema(description = "비상연락처 성명", example = "홍부모")
        @Size(max = 50)
        String emergencyName,

        @Schema(description = "비상연락처 관계", example = "PARENT")
        @Size(max = 30)
        String emergencyRelation,

        @Schema(description = "비상연락처 전화", example = "010-9876-5432")
        @Size(max = 30)
        String emergencyPhone) {
}
