package com.greentech.employee.dto.req;

import com.greentech.employee.domain.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;

// NOTE: 전송하지 않은 필드는 변경하지 않는다. 명시적 null 은 값 비우기로 처리
@Schema(description = "사원 부분 수정 요청")
public record EmployeePatchReq(
        @Schema(description = "성명", example = "홍길동")
        JsonNullable<@NotBlank(message = "성명은 비울 수 없습니다") @Size(max = 50) String> name,

        @Schema(description = "영문 성명", example = "Hong Gildong")
        JsonNullable<@Size(max = 100) String> nameEn,

        @Schema(description = "생년월일", example = "1990-01-01")
        JsonNullable<LocalDate> birthDate,

        @Schema(description = "성별", example = "MALE")
        JsonNullable<Employee.Gender> gender,

        @Schema(description = "이메일", example = "gd.hong@greentech.co.kr")
        JsonNullable<@Email(message = "이메일 형식이 올바르지 않습니다") @Size(max = 120) String> email,

        @Schema(description = "부서 ID", example = "5")
        JsonNullable<Long> departmentId,

        @Schema(description = "직위 ID", example = "3")
        JsonNullable<Long> jobPositionId,

        @Schema(description = "직속 상급자 사원 ID", example = "6")
        JsonNullable<Long> managerId,

        @Schema(description = "고용 형태", example = "FULL_TIME")
        JsonNullable<Employee.EmploymentType> employmentType,

        @Schema(description = "발령 사유", example = "정기 인사이동")
        JsonNullable<@Size(max = 500) String> reason) {
}
