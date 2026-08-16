package com.greentech.employee.dto.res;

import com.greentech.employee.domain.EmployeeContact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사원 연락처")
public record EmployeeContactRes(
        @Schema(description = "연락처 ID", example = "1")
        Long id,

        @Schema(description = "사원 ID", example = "3")
        Long employeeId,

        @Schema(description = "휴대전화", example = "010-1000-1003")
        String mobile,

        @Schema(description = "유선전화")
        String tel,

        @Schema(description = "우편번호", example = "16419")
        String zipCode,

        @Schema(description = "주소", example = "경기도 수원시 영통구 광교로 30")
        String address1,

        @Schema(description = "상세 주소", example = "202호")
        String address2,

        @Schema(description = "비상연락처 성명", example = "이순임")
        String emergencyName,

        @Schema(description = "비상연락처 관계", example = "PARENT")
        String emergencyRelation,

        @Schema(description = "비상연락처 전화", example = "010-2000-1003")
        String emergencyPhone) {

    public static EmployeeContactRes from(EmployeeContact entity) {
        return new EmployeeContactRes(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getMobile(),
                entity.getTel(),
                entity.getZipCode(),
                entity.getAddress1(),
                entity.getAddress2(),
                entity.getEmergencyName(),
                entity.getEmergencyRelation(),
                entity.getEmergencyPhone());
    }
}
