package com.greentech.employee.dto.req;

import com.greentech.employee.domain.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "사원 수정 요청")
public record EmployeeUpdateReq(
        @Schema(description = "성명", example = "홍길동")
        @NotBlank(message = "성명은 필수입니다")
        @Size(max = 50)
        String name,

        @Schema(description = "영문 성명", example = "Hong Gildong")
        @Size(max = 100)
        String nameEn,

        @Schema(description = "생년월일", example = "1990-01-01")
        LocalDate birthDate,

        @Schema(description = "성별", example = "MALE")
        Employee.Gender gender,

        @Schema(description = "이메일", example = "gd.hong@greentech.co.kr")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Size(max = 120)
        String email,

        @Schema(description = "부서 ID", example = "5")
        Long departmentId,

        @Schema(description = "직위 ID", example = "3")
        Long jobPositionId,

        @Schema(description = "직속 상급자 사원 ID", example = "6")
        Long managerId,

        @Schema(description = "고용 형태", example = "FULL_TIME")
        Employee.EmploymentType employmentType,

        @Schema(description = "재직 상태", example = "ACTIVE")
        Employee.Status status) {
}
