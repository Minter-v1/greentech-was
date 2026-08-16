package com.greentech.employee.dto.res;

import com.greentech.employee.domain.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "사원 요약 정보 - 목록 조회용")
public record EmployeeSummaryRes(
        @Schema(description = "사원 ID", example = "3")
        Long id,

        @Schema(description = "사번", example = "20200301")
        String empNo,

        @Schema(description = "성명", example = "이재훈")
        String name,

        @Schema(description = "부서명", example = "인사팀")
        String departmentName,

        @Schema(description = "직위명", example = "대리")
        String positionName,

        @Schema(description = "고용 형태", example = "FULL_TIME")
        Employee.EmploymentType employmentType,

        @Schema(description = "재직 상태", example = "ACTIVE")
        Employee.Status status,

        @Schema(description = "입사일", example = "2020-03-02")
        LocalDate hireDate,

        @Schema(description = "이메일", example = "jh.lee@greentech.co.kr")
        String email) {

    public static EmployeeSummaryRes from(Employee entity) {
        return new EmployeeSummaryRes(
                entity.getId(),
                entity.getEmpNo(),
                entity.getName(),
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.getJobPosition() != null ? entity.getJobPosition().getName() : null,
                entity.getEmploymentType(),
                entity.getStatus(),
                entity.getHireDate(),
                entity.getEmail());
    }
}
