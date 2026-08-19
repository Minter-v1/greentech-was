package com.greentech.employee.dto.res;

import com.greentech.employee.domain.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "사원 상세 정보")
public record EmployeeDetailRes(
        @Schema(description = "사원 ID", example = "3")
        Long id,

        @Schema(description = "사번", example = "20200301")
        String empNo,

        @Schema(description = "성명", example = "이재훈")
        String name,

        @Schema(description = "영문 성명", example = "Lee Jaehoon")
        String nameEn,

        @Schema(description = "마스킹된 주민등록번호", example = "900115-1******")
        String residentNoMasked,

        @Schema(description = "생년월일", example = "1990-01-15")
        LocalDate birthDate,

        @Schema(description = "성별", example = "MALE")
        Employee.Gender gender,

        @Schema(description = "이메일", example = "jh.lee@greentech.co.kr")
        String email,

        @Schema(description = "부서 ID", example = "2")
        Long departmentId,

        @Schema(description = "부서명", example = "인사팀")
        String departmentName,

        @Schema(description = "직위 ID", example = "3")
        Long jobPositionId,

        @Schema(description = "직위명", example = "대리")
        String positionName,

        @Schema(description = "직속 상급자 사원 ID", example = "2")
        Long managerId,

        @Schema(description = "고용 형태", example = "FULL_TIME")
        Employee.EmploymentType employmentType,

        @Schema(description = "재직 상태", example = "ACTIVE")
        Employee.Status status,

        @Schema(description = "입사일", example = "2020-03-02")
        LocalDate hireDate,

        @Schema(description = "퇴사일")
        LocalDate resignDate) {

    /** 주민등록번호는 마스킹 값만 노출 */
    public static EmployeeDetailRes from(Employee entity) {
        return new EmployeeDetailRes(
                entity.getId(),
                entity.getEmpNo(),
                entity.getName(),
                entity.getNameEn(),
                mask(entity.getResidentNo()),
                entity.getBirthDate(),
                entity.getGender(),
                entity.getEmail(),
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.getJobPosition() != null ? entity.getJobPosition().getId() : null,
                entity.getJobPosition() != null ? entity.getJobPosition().getName() : null,
                entity.getManager() != null ? entity.getManager().getId() : null,
                entity.getEmploymentType(),
                entity.getStatus(),
                entity.getHireDate(),
                entity.getResignDate());
    }

    private static String mask(String residentNo) {
        if (residentNo == null || residentNo.length() < 8) {
            return null;
        }
        return residentNo.substring(0, 8) + "******";
    }
}
